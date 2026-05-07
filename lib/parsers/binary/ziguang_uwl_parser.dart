import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class ZiguangUwlParser extends BinaryParser {
  ZiguangUwlParser() : super(ImeFormat.ziguangUwl);

  static const List<String> shengmu = [
    '', 'b', 'c', 'ch', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm',
    'n', 'p', 'q', 'r', 's', 'sh', 't', 'w', 'x', 'y', 'z', 'zh'
  ];

  static const List<String> yunmu = [
    'ang', 'a', 'ai', 'an', 'ang', 'ao', 'e', 'ei', 'en', 'eng', 'er',
    'i', 'ia', 'ian', 'iang', 'iao', 'ie', 'in', 'ing', 'iong', 'iu',
    'o', 'ong', 'ou', 'u', 'ua', 'uai', 'uan', 'uang', 'ue', 'ui', 'un', 'uo', 'v'
  ];

  @override
  Future<List<TableEntry>> parseBinary(
    Uint8List bytes, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntry>[];

    reader.position = 0x02;
    final enc = reader.readByte();
    final isUnicode = enc == 0x09;

    reader.position = 0x44;
    final wordCount = reader.readInt32();
    final segmentCount = reader.readInt32();

    for (int i = 0; i < segmentCount; i++) {
      try {
        reader.position = 0xC00 + 1024 * i;
        final segmentEntries = _parseSegment(reader, isUnicode);
        entries.addAll(segmentEntries);

        onProgress?.call(ParseProgress(
          current: entries.length,
          total: wordCount,
          message: '正在解析词条...',
        ));
      } catch (e) {
        break;
      }
    }

    return entries;
  }

  List<TableEntry> _parseSegment(BinaryReader reader, bool isUnicode) {
    final entries = <TableEntry>[];

    reader.readInt32(); // index number
    reader.readInt32(); // ff
    reader.readInt32(); // word len enums
    final wordByteLen = reader.readInt32();

    final startPos = reader.position;
    int lenByte = 0;

    while (lenByte < wordByteLen) {
      try {
        final entry = _parseWord(reader, isUnicode);
        if (entry != null) {
          entries.add(entry);
        }
        lenByte = reader.position - startPos;
      } catch (e) {
        break;
      }
    }

    return entries;
  }

  TableEntry? _parseWord(BinaryReader reader, bool isUnicode) {
    int lenWord = reader.readByte();
    int lenCode = reader.readByte();

    lenCode = (lenCode % 0x10) * 2 + (lenWord ~/ 0x80);
    lenWord = (lenWord % 0x80) - 1;

    final rank = reader.readByte() + (reader.readByte() << 8);

    final pinyinList = <String>[];
    for (int i = 0; i < lenCode; i++) {
      final smB = reader.readByte();
      final ymB = reader.readByte();

      final smIndex = smB & 31;
      final ymIndex = (smB >> 5) + (ymB << 3);

      if (smIndex < shengmu.length && ymIndex < yunmu.length) {
        pinyinList.add(shengmu[smIndex] + yunmu[ymIndex]);
      }
    }

    final wordBytes = reader.readBytes(lenWord);
    final word = isUnicode
        ? EncodingHelper.utf16le.decode(wordBytes)
        : EncodingHelper.gb18030.decode(wordBytes);

    if (word.isEmpty || pinyinList.isEmpty) {
      return null;
    }

    return TableEntry(
      word: word,
      shortcut: pinyinList.join("'"),
      frequency: rank,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.uwl');
  }
}
