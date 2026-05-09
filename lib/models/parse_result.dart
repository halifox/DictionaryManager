import 'ime_format.dart';
import '../generated/dictionary_api.g.dart';

class ParseResult {
  final List<TableEntryData> entries;
  final ImeFormat format;
  final int totalCount;
  final Duration parseTime;
  final List<String> warnings;

  ParseResult({
    required this.entries,
    required this.format,
    required this.totalCount,
    required this.parseTime,
    this.warnings = const [],
  });
}
