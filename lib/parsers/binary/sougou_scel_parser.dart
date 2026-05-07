import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class SougouScelParser extends BinaryParser {
  SougouScelParser() : super(ImeFormat.sougouScel);

  @override
  Future<List<TableEntry>> parseBinary(
    Uint8List bytes, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntry>[];

    final pinyinMap = _readPinyinTable(reader);

    reader.position = 0x120;
    final wordCount = reader.readInt32();

    reader.position = 0x1540 + 4;
    final pinyinTableLength = reader.readInt32();

    for (int i = 0; i < pinyinTableLength; i++) {
      reader.readInt16();
      final size = reader.readInt16();
      reader.skip(size);
    }

    int processedGroups = 0;
    while (reader.position < bytes.length - 20) {
      try {
        final wordEntries = _readWordGroup(reader, pinyinMap);
        entries.addAll(wordEntries);

        processedGroups++;
        if (processedGroups % 100 == 0) {
          onProgress?.call(ParseProgress(
            current: entries.length,
            total: wordCount,
            message: '正在解析词条...',
          ));
        }
      } catch (e) {
        break;
      }
    }

    return entries;
  }

  Map<int, String> _readPinyinTable(BinaryReader reader) {
    final pinyinMap = <int, String>{};

    reader.position = 0x1540 + 4;
    final pinyinTableLength = reader.readInt32();

    for (int i = 0; i < pinyinTableLength; i++) {
      final index = reader.readInt16();
      final size = reader.readInt16();
      final pinyinBytes = reader.readBytes(size);
      final pinyin = EncodingHelper.utf16le.decode(pinyinBytes);
      pinyinMap[index] = pinyin;
    }

    return pinyinMap;
  }

  List<TableEntry> _readWordGroup(
    BinaryReader reader,
    Map<int, String> pinyinMap,
  ) {
    final entries = <TableEntry>[];

    final samePinyinCount = reader.readInt16();
    final pinyinLength = reader.readInt16();

    final pinyinIndices = <int>[];
    for (int i = 0; i < pinyinLength ~/ 2; i++) {
      pinyinIndices.add(reader.readInt16());
    }

    final pinyinList = pinyinIndices
        .map((idx) => pinyinMap[idx] ?? '')
        .where((py) => py.isNotEmpty)
        .toList();
    final pinyin = pinyinList.join("'");

    for (int i = 0; i < samePinyinCount; i++) {
      final wordLength = reader.readInt16();
      final wordBytes = reader.readBytes(wordLength);
      final word = EncodingHelper.utf16le.decode(wordBytes);

      reader.skip(12);

      entries.add(TableEntry(
        word: word,
        shortcut: pinyin,
        frequency: 1,
        locale: 'zh_CN',
      ));
    }

    return entries;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.scel');
  }
}
