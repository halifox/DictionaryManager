import 'package:flutter/foundation.dart';
import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../models/table_entry.dart';
import '../base/text_parser.dart';

class PyimTableParser extends TextParser {
  PyimTableParser() : super(ImeFormat.pyimTable);

  @override
  Future<List<TableEntryData>> parseText(String content) async {
    final entries = <TableEntryData>[];
    final lines = content.split('\n');

    for (int i = 0; i < lines.length; i++) {
      final line = lines[i].trim();
      if (line.isEmpty) continue;

      try {
        final entry = parseLine(line);
        if (entry != null) {
          entries.add(entry);
        }
      } catch (e) {
        if (kDebugMode) {
          print('Error parsing line ${i + 1}: $e');
        }
      }
    }

    return entries;
  }

  @override
  TableEntryData? parseLine(String line) {
    try {
      return TableEntryDataExt.fromLine(line);
    } catch (e) {
      return null;
    }
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.table');
  }
}
