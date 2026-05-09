import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class ShouxinParser extends TextParser {
  ShouxinParser() : super(ImeFormat.shouxin);

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
    if (parts.length < 3) return null;

    final word = parts[0];
    final pinyin = parts[1].replaceAll("'", "");
    final frequency = int.tryParse(parts[2]) ?? 1;

    return TableEntryData(
      word: word,
      shortcut: pinyin,
      frequency: frequency,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
