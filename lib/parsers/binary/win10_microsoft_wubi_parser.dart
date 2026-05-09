import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../generated/dictionary_api.g.dart';
import '../../utils/encoding_helper.dart';
import '../../utils/binary_reader.dart';
import '../base/binary_parser.dart';

class Win10MicrosoftWubiParser extends BinaryParser {
  Win10MicrosoftWubiParser() : super(ImeFormat.win10MicrosoftWubi);

  @override
  Future<List<TableEntryData>> parseBinary(Uint8List bytes) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    // 验证文件头
    reader.position = 0;
    final header = reader.readBytes(8);
    final headerStr = String.fromCharCodes(header);
    if (headerStr != 'mschxudp') {
      throw Exception('不是有效的 Win10 微软五笔词库文件');
    }

    // 读取文件结构信息
    reader.position = 0x10;
    final phraseOffsetStart = reader.readInt32();
    final phraseStart = reader.readInt32();
    final phraseEnd = reader.readInt32();
    final phraseCount = reader.readInt32();

    // 读取偏移表
    reader.position = phraseOffsetStart;
    final offsets = <int>[];
    for (int i = 0; i < phraseCount; i++) {
      offsets.add(reader.readInt32());
    }
    offsets.add(phraseEnd - phraseStart);

    // 读取词条
    reader.position = phraseStart;
    for (int i = 0; i < phraseCount; i++) {
      try {
        final nextStartPosition = phraseStart + offsets[i + 1];
        final entry = _readOnePhrase(reader, nextStartPosition);
        if (entry != null) {
          entries.add(entry);
        }
      } catch (e) {
        break;
      }
    }

    return entries;
  }

  TableEntryData? _readOnePhrase(BinaryReader reader, int nextStartPosition) {
    final magic = reader.readInt32();
    final hanziOffset = reader.readInt16();
    final rank = reader.readByte();
    reader.readByte(); // unknown
    reader.skip(8); // unknown8

    // 读取五笔编码
    final wubiBytesLen = hanziOffset - 18;
    final wubiBytes = reader.readBytes(wubiBytesLen);
    final wubiStr = EncodingHelper.utf16le.decode(wubiBytes);

    reader.readInt16(); // 00 00 分隔符

    // 读取汉字
    final wordBytesLen = nextStartPosition - reader.position - 2;
    if (wordBytesLen <= 0) {
      return null;
    }

    final wordBytes = reader.readBytes(wordBytesLen);
    reader.readInt16(); // 00 00 分隔符

    final word = EncodingHelper.utf16le.decode(wordBytes);

    if (word.isEmpty || wubiStr.isEmpty) {
      return null;
    }

    return TableEntryData(
      word: word,
      shortcut: wubiStr,
      frequency: rank,
      locale: 'zh_CN',
    );
  }

  @override
  Future<bool> canParse(String filePath) async {
    final fileName = filePath.toLowerCase();
    return fileName.endsWith('.dat') && fileName.contains('wubi');
  }
}
