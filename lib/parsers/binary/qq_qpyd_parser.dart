import 'dart:convert';
import 'dart:typed_data';

import 'package:archive/archive.dart';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class QQQpydParser extends BinaryParser {
  QQQpydParser() : super(ImeFormat.qqQpyd);

  @override
  Future<List<TableEntryData>> parseBinary(Uint8List bytes) async {
    final reader = BinaryReader(bytes);

    reader.position = 0x38;
    final startAddress = reader.readInt32();

    reader.position = 0x44;
    final wordCount = reader.readInt32();

    reader.position = startAddress;
    final compressedData = reader.readBytes(bytes.length - startAddress);
    final decompressedData = _inflateData(compressedData);

    return _parseDecompressedData(decompressedData, wordCount);
  }

  Uint8List _inflateData(Uint8List compressed) {
    final inflater = ZLibDecoder();
    return Uint8List.fromList(inflater.decodeBytes(compressed));
  }

  List<TableEntryData> _parseDecompressedData(
    Uint8List data,
    int wordCount,
  ) {
    final entries = <TableEntryData>[];
    int idx = 0;
    int unzippedDictStartAddr = -1;

    while (idx + 10 <= data.length) {
      final pinyinLength = data[idx] & 0xff;
      final wordLength = data[idx + 1] & 0xff;

      if (idx + 10 > data.length) break;

      final pinyinStartAddr = ByteData.view(data.buffer)
          .getInt32(idx + 0x6, Endian.little);

      if (unzippedDictStartAddr == -1) {
        unzippedDictStartAddr = pinyinStartAddr;
      }

      if (idx >= unzippedDictStartAddr) break;

      final wordStartAddr = pinyinStartAddr + pinyinLength;

      if (pinyinStartAddr + pinyinLength + wordLength > data.length) break;

      final pinyinBytes = data.sublist(
        pinyinStartAddr,
        pinyinStartAddr + pinyinLength,
      );
      final pinyin = utf8.decode(pinyinBytes);

      final wordBytes = data.sublist(
        wordStartAddr,
        wordStartAddr + wordLength,
      );
      final word = EncodingHelper.utf16le.decode(wordBytes);

      entries.add(TableEntryData(
        word: word,
        shortcut: pinyin,
        frequency: 1,
        locale: 'zh_CN',
      ));

      idx += 0xa;
    }

    return entries;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.qpyd');
  }
}
