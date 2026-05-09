import 'dart:convert';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

/// 雅虎奇摩注音输入法解析器
/// 格式：word\tzhuyin\t-1.0\t0.0
/// 编码：UTF-8
/// 文件头：MJSR version 1.0.0
class YahooKeyKeyParser extends TextParser {
  YahooKeyKeyParser() : super(ImeFormat.yahooKeyKey);

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

    // 跳过文件头
    if (line.startsWith('MJSR version')) {
      return null;
    }

    // 跳过数据库部分
    if (line.startsWith('#') || line.startsWith('<database>') || line.startsWith('</database>')) {
      return null;
    }

    // 格式：word\tzhuyin\t-1.0\t0.0
    final parts = line.split('\t');
    if (parts.length < 2) {
      return null;
    }

    final word = parts[0];
    final zhuyin = parts[1]; // 注音符号，如：ㄅㄧㄥ,ㄍㄢ

    return TableEntryData(
      word: word,
      shortcut: zhuyin.replaceAll(',', ''),
      frequency: 0,
      locale: 'zh_TW',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
