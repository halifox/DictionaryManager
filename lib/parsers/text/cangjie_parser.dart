import 'dart:convert';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../../utils/encoding_helper.dart';
import '../base/text_parser.dart';

/// 仓颉输入法解析器
/// 格式：code word
/// 编码：GBK
/// 示例：日月 明
class CangjieParser extends TextParser {
  CangjieParser() : super(ImeFormat.cangjiePlatform);

  @override
  Encoding get encoding => EncodingHelper.gbk;

  @override
  Future<List<TableEntry>> parseText(
    String content, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final entries = <TableEntry>[];
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
  TableEntry? parseLine(String line) {
    line = line.trim();
    if (line.isEmpty) {
      return null;
    }

    // 格式：code word
    final parts = line.split(' ');
    if (parts.length < 2) {
      return null;
    }

    final code = parts[0];
    final word = parts[1];

    return TableEntry(
      word: word,
      shortcut: code,
      frequency: 0,
      locale: 'zh_TW',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.txt');
  }
}
