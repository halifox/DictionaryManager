import 'dart:typed_data';

import '../../models/ime_format.dart';
import '../../models/parse_result.dart';
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
  Future<List<TableEntryData>> parseBinary(
    Uint8List bytes, {
    void Function(ParseProgress)? onProgress,
  }) async {
    final reader = BinaryReader(bytes);
    final entries = <TableEntryData>[];

    // 设置指针到标题区起始位置
    reader.position = 0x130;

    // 读取标题区字节
    reader.skip(0x338 - 0x130);
    // 读取分类区字节
    reader.skip(0x540 - 0x338);
    // 读取描述区字节
    reader.skip(0xd40 - 0x540);
    // 读取示例词区字节
    reader.skip(0x1540 - 0xd40);

    // 读取拼音表总数
    final pinyinCount = reader.readInt16();
    // 跳过一个无用的 short（可能是保留字段）
    reader.readInt16();

    // 创建拼音表数组，索引对应拼音索引，内容是拼音字符串
    final pinyinArray = <String>[];
    for (int i = 0; i < pinyinCount; i++) {
      final index = reader.readInt16();
      final length = reader.readInt16();
      final pinyinBytes = reader.readBytes(length);
      final pinyin = EncodingHelper.utf16le.decode(pinyinBytes);

      // 确保数组足够大
      while (pinyinArray.length <= index) {
        pinyinArray.add('');
      }
      pinyinArray[index] = pinyin;
    }

    // 解析词语数据区，直到缓冲区无剩余字节
    int processedGroups = 0;
    while (reader.position < bytes.length) {
      try {
        // 词语同音词数量
        final sameWordCount = reader.readInt16();
        // 拼音索引字节长度
        final pinyinIndexBytesLength = reader.readInt16();

        // 根据拼音索引构建拼音字符串
        final pinyinBuilder = StringBuffer();
        for (int i = 0; i < pinyinIndexBytesLength ~/ 2; i++) {
          final pinyinIndex = reader.readInt16();
          if (pinyinIndex >= 0 && pinyinIndex < pinyinArray.length) {
            pinyinBuilder.write(pinyinArray[pinyinIndex]);
          }
        }
        final pinyin = pinyinBuilder.toString();

        // 读取每个同音词的具体内容
        for (int i = 0; i < sameWordCount; i++) {
          final wordByteLength = reader.readInt16();
          final wordBytes = reader.readBytes(wordByteLength);
          final word = EncodingHelper.utf16le.decode(wordBytes);

          final extLength = reader.readInt16();
          final wordFrequency = reader.readInt16();
          final extBytes = reader.readBytes(extLength - 2);

          entries.add(TableEntryData(
            word: word,
            shortcut: pinyin,
            frequency: wordFrequency,
            locale: 'zh_CN',
          ));
        }

        processedGroups++;
        if (processedGroups % 100 == 0) {
          onProgress?.call(ParseProgress(
            current: entries.length,
            total: entries.length,
            message: '正在解析词条...',
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
