import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

/// 搜狗词库规则：
/// - 文件头固定偏移0x130，数据依次为标题区、分类区、描述区、示例词区。
/// - 字符编码UTF-16LE（小端Unicode）。
/// - 拼音表由多条拼音片段组成，包含索引和拼音字符串，用于构造词语拼音。
/// - 词语数据结构：
///   - 同音词数量(short)，拼音索引字节长度(short)。
///   - 拼音索引数组（对应拼音表索引）。
///   - 词字节长度(short)，词内容(UTF-16LE)，扩展长度(short)，词频(short)，扩展数据。
/// - 词频为short，扩展数据长度为扩展长度减2，通常包含词性信息。
/// - 使用小端序读取数据，数据连续存储，按偏移依次解析。
class SougouScelParser extends BinaryParser {
  SougouScelParser() : super(ImeFormat.sougouScel);

  @override
  Future<List<TableEntryData>> parseBinary(Uint8List bytes) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    reader.position = 0x130;
    reader.skip(0x338 - 0x130);
    reader.skip(0x540 - 0x338);
    reader.skip(0xd40 - 0x540);
    reader.skip(0x1540 - 0xd40);

    final pinyinCount = reader.readInt16();
    reader.readInt16();

    final pinyinArray = <String>[];
    for (int i = 0; i < pinyinCount; i++) {
      final index = reader.readInt16();
      final length = reader.readInt16();
      final pinyinBytes = reader.readBytes(length);
      final pinyin = EncodingHelper.utf16le.decode(pinyinBytes);

      while (pinyinArray.length <= index) {
        pinyinArray.add('');
      }
      pinyinArray[index] = pinyin;
    }

    while (reader.position < bytes.length) {
      try {
        final sameWordCount = reader.readInt16();
        final pinyinIndexBytesLength = reader.readInt16();

        final pinyinBuilder = StringBuffer();
        for (int i = 0; i < pinyinIndexBytesLength ~/ 2; i++) {
          final pinyinIndex = reader.readInt16();
          if (pinyinIndex >= 0 && pinyinIndex < pinyinArray.length) {
            pinyinBuilder.write(pinyinArray[pinyinIndex]);
          }
        }
        final pinyin = pinyinBuilder.toString();

        for (int i = 0; i < sameWordCount; i++) {
          final wordByteLength = reader.readInt16();
          final wordBytes = reader.readBytes(wordByteLength);
          final word = EncodingHelper.utf16le.decode(wordBytes);

          final extLength = reader.readInt16();
          final wordFrequency = reader.readInt16();
          reader.readBytes(extLength - 2);

          entries.add(TableEntryData(
            word: word,
            shortcut: pinyin,
            frequency: wordFrequency,
            locale: 'zh_CN',
          ));
        }
      } catch (e) {
        break;
      }
    }

    return entries;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.scel');
  }
}
