import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class BaiduBdictParser extends BinaryParser {
  static const List<String> shengmu = [
    'c', 'd', 'b', 'f', 'g', 'h', 'ch', 'j', 'k', 'l', 'm', 'n',
    '', 'p', 'q', 'r', 's', 't', 'sh', 'zh', 'w', 'x', 'y', 'z'
  ];

  static const List<String> yunmu = [
    'uang', 'iang', 'iong', 'ang', 'eng', 'ian', 'iao', 'ing',
    'ong', 'uai', 'uan', 'ai', 'an', 'ao', 'ei', 'en', 'er',
    'ua', 'ie', 'in', 'iu', 'ou', 'ia', 'ue', 'ui', 'un', 'uo',
    'a', 'e', 'i', 'o', 'u', 'v'
  ];

  BaiduBdictParser() : super(ImeFormat.baiduBdict);

  @override
  Future<List<TableEntryData>> parseBinary(
    Uint8List bytes, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    reader.position = 0x60;
    final endPosition = reader.readInt32();

    reader.position = 0x350;
    int count = 0;

    while (reader.position < endPosition && reader.position < bytes.length - 20) {
      try {
        final entry = _parseWord(reader);
        if (entry != null) {
          entries.add(entry);
          count++;

          if (count % 100 == 0) {
            onProgress?.call(ParseProgress(
              current: reader.position,
              total: endPosition,
              message: '已解析 $count 个词条...',
            ));
          }
        } else {
          break;
        }
      } catch (e) {
        print('百度BDICT解析词条失败: $e');
        break;
      }
    }

    return entries;
  }

  TableEntryData? _parseWord(BinaryReader reader) {
    final length = reader.readInt32();

    if (length == 0 || length > 1000 || reader.position + length * 4 > reader.length) {
      return null;
    }

    final pinyinList = <String>[];
    for (int i = 0; i < length; i++) {
      final smIndex = reader.readByte();
      final ymIndex = reader.readByte();

      if (smIndex < shengmu.length && ymIndex < yunmu.length) {
        final pinyin = shengmu[smIndex] + yunmu[ymIndex];
        pinyinList.add(pinyin);
      } else {
        return null;
      }
    }

    if (reader.position + length * 2 > reader.length) {
      return null;
    }

    final wordBytes = reader.readBytes(length * 2);
    final word = EncodingHelper.utf16le.decode(wordBytes);

    return TableEntryData(
      word: word,
      shortcut: pinyinList.join("'"),
      frequency: 1,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.bdict');
  }
}
