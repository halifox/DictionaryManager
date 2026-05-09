import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class MicrosoftPinyin2010Parser extends TextParser {
  MicrosoftPinyin2010Parser() : super(ImeFormat.microsoftPinyin2010);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntryData>> parseText(String content) async {
    final entries = <TableEntryData>[];

    final entryRegex = RegExp(
      r'<ns1:DictionaryEntry>.*?<ns1:InputString>(.*?)</ns1:InputString>.*?<ns1:OutputString>(.*?)</ns1:OutputString>.*?</ns1:DictionaryEntry>',
      dotAll: true,
    );

    final matches = entryRegex.allMatches(content);

    for (final match in matches) {
      final pinyinWithTone = match.group(1) ?? '';
      final word = match.group(2) ?? '';

      if (word.isNotEmpty) {
        final pinyin = pinyinWithTone
            .replaceAll(RegExp(r'[1-4]'), '')
            .replaceAll(' ', '');

        entries.add(TableEntryData(
          word: word,
          shortcut: pinyin,
          frequency: 1,
          locale: 'zh_CN',
        ));
      }
    }

    return entries;
  }

  @override
  TableEntryData? parseLine(String line) {
    return null;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.xml');
  }
}
