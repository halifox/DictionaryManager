import 'dart:typed_data';
import 'dart:io';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../base/base_parser.dart';

class MacOsPlistParser extends BaseParser {
  MacOsPlistParser() : super(ImeFormat.macOsPlist);

  @override
  Future<ParseResult> parseFile(
    String filePath, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final stopwatch = Stopwatch()..start();
    final entries = <TableEntry>[];

    // 读取文件内容
    final file = File(filePath);
    final content = await file.readAsString();

    // 简单的 XML 解析（plist 格式）
    // 查找所有 <dict> 标签
    final dictPattern = RegExp(
      r'<dict>.*?<key>phrase</key>\s*<string>(.*?)</string>\s*<key>shortcut</key>\s*<string>(.*?)</string>.*?</dict>',
      multiLine: true,
      dotAll: true,
    );

    final matches = dictPattern.allMatches(content);
    int count = 0;

    for (final match in matches) {
      final word = match.group(1) ?? '';
      final shortcut = match.group(2) ?? '';

      if (word.isNotEmpty && shortcut.isNotEmpty) {
        entries.add(TableEntry(
          word: word,
          shortcut: shortcut,
          frequency: 1,
          locale: 'zh_CN',
        ));
      }

      count++;
      if (count % 100 == 0) {
        onProgress?.call(ParseProgress(
          current: count,
          total: -1,
          message: '正在解析词条...',
        ));
      }
    }

    stopwatch.stop();
    return ParseResult(
      entries: entries,
      format: format,
      totalCount: entries.length,
      parseTime: stopwatch.elapsed,
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.plist');
  }
}
