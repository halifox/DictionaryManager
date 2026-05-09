import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

/// Libpinyin文本格式解析器
/// 格式：word pin'yin
/// 编码：UTF-8
class LibpinyinParser extends TextParser {
  LibpinyinParser() : super(ImeFormat.libpinyin);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntryData>> parseText(String content) async {
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
    }

    return entries;
  }

  @override
  TableEntryData? parseLine(String line) {
    line = line.trim();
    if (line.isEmpty) {
      return null;
    }

    final parts = line.split(' ');
    if (parts.length < 2) {
      return null;
    }

    final word = parts[0];
    final pinyin = parts[1].replaceAll("'", '');

    return TableEntryData(
      word: word,
      shortcut: pinyin,
      frequency: 0,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
