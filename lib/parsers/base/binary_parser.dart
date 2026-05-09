import 'dart:io';
import 'dart:typed_data';

import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import 'base_parser.dart';

abstract class BinaryParser extends BaseParser {
  BinaryParser(super.format);

  @override
  Future<ParseResult> parseFile(String filePath) async {
    final stopwatch = Stopwatch()..start();
    final file = File(filePath);
    final bytes = await file.readAsBytes();

    final entries = await parseBinary(bytes);

    stopwatch.stop();
    return ParseResult(
      entries: entries,
      format: format,
      totalCount: entries.length,
      parseTime: stopwatch.elapsed,
    );
  }

  Future<List<TableEntryData>> parseBinary(Uint8List bytes);
}
