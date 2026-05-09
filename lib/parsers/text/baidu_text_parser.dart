import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class BaiduTextParser extends TextParser {
  BaiduTextParser() : super(ImeFormat.baiduText);

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
    final parts = line.split('\t');
    if (parts.isEmpty) return null;

    final word = parts[0];

    if (parts.length == 2) {
      final frequency = int.tryParse(parts[1]) ?? 1;
      return TableEntryData(
        word: word,
        shortcut: null,
        frequency: frequency,
        locale: 'en_US',
      );
    } else if (parts.length >= 3) {
      final pinyin = parts[1].replaceAll("'", "");
      final frequency = int.tryParse(parts[2]) ?? 1;
      return TableEntryData(
        word: word,
        shortcut: pinyin,
        frequency: frequency,
        locale: 'zh_CN',
      );
    }

    return null;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
