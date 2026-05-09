import 'dart:io';
import 'package:archive/archive.dart';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../base/base_parser.dart';

class GboardParser extends BaseParser {
  GboardParser() : super(ImeFormat.gboard);

  @override
  Future<ParseResult> parseFile(String filePath) async {
    final stopwatch = Stopwatch()..start();
    final entries = <TableEntryData>[];

    // 读取 ZIP 文件
    final file = File(filePath);
    final bytes = await file.readAsBytes();
    final archive = ZipDecoder().decodeBytes(bytes);

    // 查找 dictionary.txt 文件
    ArchiveFile? dictionaryFile;
    for (final file in archive) {
      if (file.name == 'dictionary.txt') {
        dictionaryFile = file;
        break;
      }
    }

    if (dictionaryFile == null) {
      throw Exception('ZIP 文件中未找到 dictionary.txt');
    }

    // 解压并解析文本内容
    final content = String.fromCharCodes(dictionaryFile.content as List<int>);
    final lines = content.split('\n');

    for (int i = 1; i < lines.length; i++) {
      final line = lines[i].trim();
      if (line.isEmpty || line.startsWith('#')) continue;

      try {
        final parts = line.split('\t');
        if (parts.length >= 2) {
          final pinyin = parts[0];
          final word = parts[1];

          entries.add(TableEntryData(
            word: word,
            shortcut: pinyin,
            frequency: 1,
            locale: 'zh_CN',
          ));
        }
      } catch (e) {
        print('Gboard解析词条失败 (行 $i): $e');
        continue;
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
    return filePath.toLowerCase().endsWith('.zip');
  }
}
