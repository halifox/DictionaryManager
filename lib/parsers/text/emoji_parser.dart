import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

/// Emoji表情解析器
/// 格式：emoji\tword
/// 编码：UTF-8
/// 示例：😀\t笑脸
class EmojiParser extends TextParser {
  EmojiParser() : super(ImeFormat.emoji);

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

    // 格式：emoji\tword
    final parts = line.split('\t');
    if (parts.length < 2) {
      return null;
    }

    final emoji = parts[0];
    final word = parts[1];

    return TableEntryData(
      word: word,
      shortcut: emoji,
      frequency: 0,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
