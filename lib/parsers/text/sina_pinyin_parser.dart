import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../../utils/encoding_helper.dart';
import '../base/text_parser.dart';

class SinaPinyinParser extends TextParser {
  SinaPinyinParser() : super(ImeFormat.sinaPinyin);

  @override
  Encoding get encoding => EncodingHelper.gbk;

  @override
  Future<List<TableEntry>> parseText(
    String content, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final entries = <TableEntry>[];
    final lines = content.split('\n');

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i].trim();
      if (line.isEmpty) {
        continue;
      }

      final entry = parseLine(line);
      if (entry != null) {
        entries.add(entry);
      }

      if (i % 100 == 0) {
        onProgress?.call(ParseProgress(
          current: i,
          total: lines.length,
          message: '正在解析...',
        ));
      }
    }

    return entries;
  }

  @override
  TableEntry? parseLine(String line) {
    final parts = line.split(' ');
    if (parts.length < 2) return null;

    final pinyin = parts[0].replaceAll("'", "");
    final word = parts[1];

    return TableEntry(
      word: word,
      shortcut: pinyin,
      frequency: 1,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
