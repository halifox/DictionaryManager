import 'dart:convert';
import 'dart:typed_data';
import 'package:gbk_codec/gbk_codec.dart' as gbk_codec;

class EncodingHelper {
  static final utf16le = _Utf16LECodec();

  static Encoding get gbk => gbk_codec.gbk;

  static Encoding get gb18030 => gbk_codec.gbk; // GB18030 兼容 GBK
}

class _Utf16LECodec extends Encoding {
  @override
  Converter<List<int>, String> get decoder => const _Utf16LEDecoder();

  @override
  Converter<String, List<int>> get encoder => const _Utf16LEEncoder();

  @override
  String get name => 'utf-16le';
}

class _Utf16LEDecoder extends Converter<List<int>, String> {
  const _Utf16LEDecoder();

  @override
  String convert(List<int> input) {
    if (input.isEmpty) return '';

    // 确保字节数是偶数
    final length = input.length - (input.length % 2);
    if (length == 0) return '';

    final bytes = input.length == length
        ? input
        : input.sublist(0, length);

    try {
      final uint16List = Uint16List.view(Uint8List.fromList(bytes).buffer);

      // 过滤掉无效的 UTF-16 字符
      final validCodes = <int>[];
      for (int i = 0; i < uint16List.length; i++) {
        final code = uint16List[i];

        // 跳过空字符
        if (code == 0) continue;

        // 检查是否是高代理项 (0xD800-0xDBFF)
        if (code >= 0xD800 && code <= 0xDBFF) {
          // 需要有低代理项
          if (i + 1 < uint16List.length) {
            final low = uint16List[i + 1];
            if (low >= 0xDC00 && low <= 0xDFFF) {
              validCodes.add(code);
              validCodes.add(low);
              i++; // 跳过低代理项
              continue;
            }
          }
          // 无效的高代理项，跳过
          continue;
        }

        // 检查是否是孤立的低代理项 (0xDC00-0xDFFF)
        if (code >= 0xDC00 && code <= 0xDFFF) {
          // 孤立的低代理项，跳过
          continue;
        }

        validCodes.add(code);
      }

      return String.fromCharCodes(validCodes);
    } catch (e) {
      // 解码失败，返回空字符串
      return '';
    }
  }
}

class _Utf16LEEncoder extends Converter<String, List<int>> {
  const _Utf16LEEncoder();

  @override
  List<int> convert(String input) {
    final units = input.codeUnits;
    final bytes = Uint8List(units.length * 2);
    final byteData = ByteData.view(bytes.buffer);
    for (int i = 0; i < units.length; i++) {
      byteData.setUint16(i * 2, units[i], Endian.little);
    }
    return bytes;
  }
}

