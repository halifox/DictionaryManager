import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class JidianZhengmaParser extends TextParser {
  JidianZhengmaParser() : super(ImeFormat.jidianZhengma);

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
      if (line.isEmpty || line.startsWith('#')) {
        continue;
      }

      final lineEntries = parseLineToEntries(line);
      if (lineEntries != null) {
        entries.addAll(lineEntries);
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

  List<TableEntryData>? parseLineToEntries(String line) {
    final parts = line.split(' ');
    if (parts.length < 2) return null;

    final code = parts[0];
    final entries = <TableEntryData>[];

    for (int i = 1; i < parts.length; i++) {
      final word = parts[i].trim().replaceAll('，', '');
      if (word.isEmpty) continue;

      entries.add(TableEntryData(
        word: word,
        shortcut: code,
        frequency: 1,
        locale: 'zh_CN',
      ));
    }

    return entries.isEmpty ? null : entries;
  }

  @override
  TableEntryData? parseLine(String line) {
    final entries = parseLineToEntries(line);
    return entries?.first;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
