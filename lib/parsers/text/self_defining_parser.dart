import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class SelfDefiningParser extends TextParser {
  final String separator;
  final int wordIndex;
  final int pinyinIndex;
  final int? frequencyIndex;

  SelfDefiningParser({
    this.separator = '\t',
    this.wordIndex = 0,
    this.pinyinIndex = 1,
    this.frequencyIndex,
  }) : super(ImeFormat.selfDefining);

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
    final parts = line.split(separator);

    final maxIndex = [wordIndex, pinyinIndex, frequencyIndex ?? -1].reduce((a, b) => a > b ? a : b);
    if (parts.length <= maxIndex) return null;

    final word = parts[wordIndex];
    final pinyin = parts[pinyinIndex];
    final frequency = frequencyIndex != null && frequencyIndex! < parts.length
        ? int.tryParse(parts[frequencyIndex!]) ?? 1
        : 1;

    if (word.isEmpty) return null;

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
