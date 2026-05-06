import 'dart:io';
import 'package:flutter/foundation.dart';
import '../models/table_entry.dart';

class TableParser {
  static Future<List<TableEntry>> parseFile(String filePath) async {
    final file = File(filePath);
    final lines = await file.readAsLines();

    final entries = <TableEntry>[];

    for (var i = 0; i < lines.length; i++) {
      final line = lines[i].trim();
      if (line.isEmpty) continue;

      try {
        final entry = TableEntry.fromLine(line);
        entries.add(entry);
      } catch (e) {
        if (kDebugMode) {
          print('Error parsing line ${i + 1}: $e');
        }
      }
    }

    return entries;
  }
}
