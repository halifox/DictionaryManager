import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../base/text_parser.dart';

class SougouTextParser extends TextParser {
  SougouTextParser() : super(ImeFormat.sougouText);

  @override
  Encoding get encoding => EncodingHelper.gbk;

  @override
  Future<List<TableEntryData>> parseText(String content) async {
    final entries = <TableEntryData>[];
    final lines = content.split('\n');

    for (final line in lines) {
      final trimmed = line.trim();
      if (trimmed.isEmpty) continue;

      final entry = parseLine(trimmed);
      if (entry != null) {
        entries.add(entry);
      }
    }

    return entries;
  }

  @override
  TableEntryData? parseLine(String line) {
    if (!line.startsWith("'")) return null;

    final parts = line.split(' ');
    if (parts.length < 2) return null;

    final pinyin = parts[0].replaceAll("'", "");
    final word = parts[1];

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
