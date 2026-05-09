import 'dart:io';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/base_parser.dart';

class MacOsPlistParser extends BaseParser {
  MacOsPlistParser() : super(ImeFormat.macOsPlist);

  @override
  Future<ParseResult> parseFile(String filePath) async {
    final stopwatch = Stopwatch()..start();
    final entries = <TableEntryData>[];

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
