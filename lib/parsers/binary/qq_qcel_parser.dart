import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class QqQcelParser extends BinaryParser {
  QqQcelParser() : super(ImeFormat.qqQcel);

  @override
  Future<List<TableEntryData>> parseBinary(Uint8List bytes) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    // QQ拼音 .qcel 格式与 .qpyd 类似，但有细微差异
    // 文件头部结构
    reader.position = 0x120;
    final wordCount = reader.readInt32();

    // 跳过拼音表
    reader.position = 0x1540 + 4;
    final pinyinTableLength = reader.readInt32();

    for (int i = 0; i < pinyinTableLength; i++) {
      reader.readInt16();
      final size = reader.readInt16();
      reader.skip(size);
    }

    while (reader.position < bytes.length - 20) {
      try {
        final wordEntries = _readWordGroup(reader);
        entries.addAll(wordEntries);
      } catch (e) {
        break;
      }
    }

    return entries;
  }

  List<TableEntryData> _readWordGroup(BinaryReader reader) {
    final entries = <TableEntryData>[];

    final samePinyinCount = reader.readInt16();
    final pinyinLength = reader.readInt16();

    // 读取拼音
    final pinyinBytes = reader.readBytes(pinyinLength);
    final pinyin = EncodingHelper.utf16le.decode(pinyinBytes);

    for (int i = 0; i < samePinyinCount; i++) {
      final wordLength = reader.readInt16();
      final wordBytes = reader.readBytes(wordLength);
      final word = EncodingHelper.utf16le.decode(wordBytes);

      reader.skip(12); // 跳过扩展信息

      entries.add(TableEntryData(
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
    return filePath.toLowerCase().endsWith('.qcel');
  }
}
