import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class LingoesLd2Parser extends BinaryParser {
  LingoesLd2Parser() : super(ImeFormat.lingoesLd2);

  @override
  Future<List<TableEntryData>> parseBinary(Uint8List bytes) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    // 读取文件头
    reader.position = 0x18;
    reader.readInt16(); // version major
    reader.readInt16(); // version minor
    reader.skip(8); // ID

    reader.position = 0x5c;
    final offsetData = reader.readInt32() + 0x60;

    if (bytes.length <= offsetData) {
      return entries;
    }

    reader.position = offsetData;
    final type = reader.readInt32();

    reader.position = offsetData + 4;
    final offsetWithInfo = reader.readInt32() + offsetData + 12;

    int offsetWithIndex;
    if (type == 3) {
      offsetWithIndex = offsetData;
    } else {
      if (bytes.length <= offsetWithInfo - 0x1C) {
        return entries;
      }
      offsetWithIndex = offsetWithInfo;
    }

    // 读取词典数据
    reader.position = offsetWithIndex;
    reader.readInt32(); // dict type
    final limit = reader.readInt32() + offsetWithIndex + 8;
    final offsetIndex = offsetWithIndex + 0x1C;
    final offsetCompressedDataHeader = reader.readInt32() + offsetIndex;
    final inflatedWordsIndexLength = reader.readInt32();
    final inflatedWordsLength = reader.readInt32();
    final inflatedXmlLength = reader.readInt32();

    final definitions = (offsetCompressedDataHeader - offsetIndex) ~/ 4;

    // 读取压缩流偏移
    final deflateStreams = <int>[];
    reader.position = offsetCompressedDataHeader + 8;
    while (reader.position < limit - 4) {
      final offset = reader.readInt32();
      if (offset + reader.position >= limit) break;
      deflateStreams.add(offset);
    }

    final offsetCompressedData = reader.position;

    // TODO: 实现解压缩功能
    // 灵格斯格式使用 DEFLATE 压缩，需要实现解压缩
    // 解压后的数据包含：词组索引、词组、XML解释
    // 由于 Dart 的 zlib 解压较复杂，这里标记为 TODO

    return entries;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.ld2');
  }
}
