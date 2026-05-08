import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/text_parser.dart';

/// 谷歌拼音文本格式解析器
/// 格式：word\tfrequency\tpinyin1 pinyin2 ...
/// 编码：UTF-8
class GooglePinyinParser extends TextParser {
  GooglePinyinParser() : super(ImeFormat.googlePinyin);

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
      if (line.isEmpty) {
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
    line = line.trim();
    if (line.isEmpty) {
      return null;
    }

    final parts = line.split('\t');
    if (parts.length < 3) {
      return null;
    }

    final word = parts[0];
    final frequency = int.tryParse(parts[1]) ?? 0;
    final pinyin = parts[2].trim();

    return TableEntryData(
      word: word,
      shortcut: pinyin.replaceAll(' ', ''),
      frequency: frequency,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
