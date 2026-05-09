import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class BaiduBcdParser extends BinaryParser {
  BaiduBcdParser() : super(ImeFormat.baiduBcd);

  static const List<String> shengmu = [
    'c', 'd', 'b', 'f', 'g', 'h', 'ch', 'j', 'k', 'l', 'm', 'n',
    '', 'p', 'q', 'r', 's', 't', 'sh', 'zh', 'w', 'x', 'y', 'z'
  ];

  static const List<String> yunmu = [
    'uang', 'iang', 'iong', 'ang', 'eng', 'ian', 'iao', 'ing', 'ong', 'uai', 'uan',
    'ai', 'an', 'ao', 'ei', 'en', 'er', 'ua', 'ie', 'in', 'iu', 'ou', 'ia',
    'ue', 'ui', 'un', 'uo', 'a', 'e', 'i', 'o', 'u', 'v'
  ];

  @override
  Future<List<TableEntryData>> parseBinary(Uint8List bytes) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    reader.position = 0x350;

    while (reader.position < bytes.length - 10) {
      try {
        final entry = _importWord(reader);
        if (entry != null) {
          entries.add(entry);
        }
      } catch (e) {
        print('百度BCD解析词条失败: $e');
        break;
      }
    }

    return entries;
  }

  TableEntryData? _importWord(BinaryReader reader) {
    final temp = reader.readBytes(2);
    final len = ByteData.view(temp.buffer).getInt16(0, Endian.little);

    reader.skip(2); // unknown 2 bytes

    final pinyinList = <String>[];
    for (int i = 0; i < len; i++) {
      final smIndex = reader.readByte();
      final ymIndex = reader.readByte();

      if (smIndex < shengmu.length && ymIndex < yunmu.length) {
        pinyinList.add(shengmu[smIndex] + yunmu[ymIndex]);
      }
    }

    final wordBytes = reader.readBytes(2 * len);
    final word = EncodingHelper.utf16le.decode(wordBytes);

    if (word.isEmpty || pinyinList.isEmpty) {
      return null;
    }

    return TableEntryData(
      word: word,
      shortcut: pinyinList.join("'"),
      frequency: 1,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.bcd');
  }
}
