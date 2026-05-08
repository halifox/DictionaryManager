import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class BaiduPhonePinyinParser extends TextParser {
  BaiduPhonePinyinParser() : super(ImeFormat.baiduPhonePinyin);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntryData>> parseText(
    String content, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final entries = <TableEntryData>[];
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
  TableEntryData? parseLine(String line) {
    final match = RegExp(r'(.+?)\((.+?)\)').firstMatch(line);
    if (match == null) return null;

    final word = match.group(1) ?? '';
    final pinyin = (match.group(2) ?? '').replaceAll('|', '');

    if (word.isEmpty) return null;

    return TableEntryData(
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
