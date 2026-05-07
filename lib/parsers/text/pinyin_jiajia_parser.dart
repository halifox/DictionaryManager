import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../base/text_parser.dart';

class PinyinJiaJiaParser extends TextParser {
  PinyinJiaJiaParser() : super(ImeFormat.pinyinJiajia);

  @override
  Encoding get encoding => utf8;

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
    final word = StringBuffer();
    final pinyin = StringBuffer();

    int i = 0;
    while (i < line.length) {
      final char = line[i];
      word.write(char);
      i++;

      if (i < line.length && line.codeUnitAt(i) <= 'z'.codeUnitAt(0)) {
        while (i < line.length && line.codeUnitAt(i) <= 'z'.codeUnitAt(0)) {
          pinyin.write(line[i]);
          i++;
        }
      }
    }

    if (word.isEmpty) return null;

    return TableEntry(
      word: word.toString(),
      shortcut: pinyin.toString(),
      frequency: 1,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
