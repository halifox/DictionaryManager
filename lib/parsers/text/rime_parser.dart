import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class RimeParser extends TextParser {
  RimeParser() : super(ImeFormat.rime);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntryData>> parseText(String content) async {
    final entries = <TableEntryData>[];
    final lines = content.split('\n');

    bool inDataSection = false;

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i].trim();

      if (line == '---') {
        continue;
      }
      if (line.startsWith('name:') ||
          line.startsWith('version:') ||
          line.startsWith('sort:') ||
          line.startsWith('columns:')) {
        continue;
      }
      if (line == '...') {
        inDataSection = true;
        continue;
      }

      if (line.isEmpty || !inDataSection) {
        continue;
      }
      if (line.startsWith('#')) {
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
    if (parts.length < 2) return null;

    final word = parts[0];
    final code = parts[1].replaceAll(' ', "'");
    final frequency = parts.length >= 3 ? int.tryParse(parts[2]) ?? 1 : 1;

    return TableEntryData(
      word: word,
      shortcut: code,
      frequency: frequency,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.dict.yaml') ||
        filePath.toLowerCase().endsWith('.yaml');
  }
}
