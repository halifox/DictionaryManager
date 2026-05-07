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
    final uint16List = Uint16List.view(Uint8List.fromList(input).buffer);
    return String.fromCharCodes(uint16List);
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

