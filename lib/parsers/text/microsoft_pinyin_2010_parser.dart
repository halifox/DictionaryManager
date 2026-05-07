import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../base/text_parser.dart';

class MicrosoftPinyin2010Parser extends TextParser {
  MicrosoftPinyin2010Parser() : super(ImeFormat.microsoftPinyin2010);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntry>> parseText(
    String content, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final entries = <TableEntry>[];

    final entryRegex = RegExp(
      r'<ns1:DictionaryEntry>.*?<ns1:InputString>(.*?)</ns1:InputString>.*?<ns1:OutputString>(.*?)</ns1:OutputString>.*?</ns1:DictionaryEntry>',
      dotAll: true,
    );

    final matches = entryRegex.allMatches(content);
    final total = matches.length;
    int current = 0;

    for (final match in matches) {
      final pinyinWithTone = match.group(1) ?? '';
      final word = match.group(2) ?? '';

      if (word.isNotEmpty) {
        final pinyin = pinyinWithTone
            .replaceAll(RegExp(r'[1-4]'), '')
            .replaceAll(' ', '');

        entries.add(TableEntry(
          word: word,
          shortcut: pinyin,
          frequency: 1,
          locale: 'zh_CN',
        ));
      }

      current++;
      if (current % 100 == 0) {
        onProgress?.call(ParseProgress(
          current: current,
          total: total,
          message: '正在解析...',
        ));
      }
    }

    return entries;
  }

  @override
  TableEntry? parseLine(String line) {
    return null;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.xml');
  }
}
