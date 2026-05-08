import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

class MacOsNativeParser extends TextParser {
  MacOsNativeParser() : super(ImeFormat.macosNative);

  @override
  Encoding get encoding => utf8;

  @override
  Future<List<TableEntryData>> parseText(
    String content, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final entries = <TableEntryData>[];

    final entryRegex = RegExp(
      r'<dict>.*?<key>phrase</key>\s*<string>(.*?)</string>.*?<key>shortcut</key>\s*<string>(.*?)</string>.*?</dict>',
      dotAll: true,
    );

    final matches = entryRegex.allMatches(content);
    final total = matches.length;
    int current = 0;

    for (final match in matches) {
      final word = match.group(1) ?? '';
      final shortcut = match.group(2) ?? '';

      if (word.isNotEmpty && shortcut.isNotEmpty) {
        entries.add(TableEntryData(
          word: word,
          shortcut: shortcut,
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
  TableEntryData? parseLine(String line) {
    return null;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.plist');
  }
}
