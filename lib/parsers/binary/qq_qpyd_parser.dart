import 'dart:convert';
import 'dart:typed_data';

import 'package:archive/archive.dart';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
import '../../models/table_entry.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class QQQpydParser extends BinaryParser {
  QQQpydParser() : super(ImeFormat.qqQpyd);

  @override
  Future<List<TableEntry>> parseBinary(
    Uint8List bytes, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final reader = BinaryReader(bytes);

    reader.position = 0x38;
    final startAddress = reader.readInt32();

    reader.position = 0x44;
    final wordCount = reader.readInt32();

    reader.position = startAddress;
    final compressedData = reader.readBytes(bytes.length - startAddress);
    final decompressedData = _inflateData(compressedData);

    return _parseDecompressedData(
      decompressedData,
      wordCount,
      onProgress,
    );
  }

  Uint8List _inflateData(Uint8List compressed) {
    final inflater = ZLibDecoder();
    return Uint8List.fromList(inflater.decodeBytes(compressed));
  }

  List<TableEntry> _parseDecompressedData(
    Uint8List data,
    int wordCount,
    void Function(ParseProgress)? onProgress,
  ) {
    final entries = <TableEntry>[];
    int idx = 0;
    int unzippedDictStartAddr = -1;
    int count = 0;

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

      entries.add(TableEntry(
        word: word,
        shortcut: pinyin,
        frequency: 1,
        locale: 'zh_CN',
      ));

      count++;
      if (count % 100 == 0) {
        onProgress?.call(ParseProgress(
          current: count,
          total: wordCount,
          message: '正在解析词条...',
        ));
      }

      idx += 0xa;
    }

    return entries;
  }

  @override
  Future<bool> canParse(String filePath) async {
    return filePath.toLowerCase().endsWith('.qpyd');
  }
}
