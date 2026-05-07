import 'dart:convert';
import 'dart:io';

import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import 'base_parser.dart';

abstract class TextParser extends BaseParser {
  TextParser(super.format);

  Encoding get encoding => utf8;

  @override
  Future<ParseResult> parseFile(
    String filePath, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final stopwatch = Stopwatch()..start();
    final file = File(filePath);
    final content = await file.readAsString(encoding: encoding);

    final entries = await parseText(content, onProgress: onProgress);

    stopwatch.stop();
    return ParseResult(
      entries: entries,
      format: format,
      totalCount: entries.length,
      parseTime: stopwatch.elapsed,
    );
  }

  Future<List<TableEntry>> parseText(
    String content, {
    void Function(ParseProgress)? onProgress,
  });

  TableEntry? parseLine(String line);
}
