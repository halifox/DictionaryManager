import 'dart:typed_data';

class BinaryReader {
  final Uint8List _data;
  int position = 0;

  BinaryReader(this._data);

  int get length => _data.length;

  int readByte() {
    return _data[position++];
  }

  int readInt16() {
    final value = ByteData.view(_data.buffer)
        .getInt16(position, Endian.little);
    position += 2;
    return value;
  }

  int readInt32() {
    final value = ByteData.view(_data.buffer)
        .getInt32(position, Endian.little);
    position += 4;
    return value;
  }

  int readInt32At(int offset) {
    return ByteData.view(_data.buffer)
        .getInt32(offset, Endian.little);
  }

  Uint8List readBytes(int count) {
    final bytes = _data.sublist(position, position + count);
    position += count;
    return bytes;
  }

  void skip(int count) {
    position += count;
  }

  String readUtf16String(int maxLength) {
    final bytes = <int>[];
    for (int i = 0; i < maxLength * 2; i += 2) {
      if (position + i + 1 >= _data.length) break;
      final b1 = _data[position + i];
      final b2 = _data[position + i + 1];
      if (b1 == 0 && b2 == 0) break;
      bytes.addAll([b1, b2]);
    }
    position += maxLength * 2;
    return String.fromCharCodes(
      Uint16List.view(Uint8List.fromList(bytes).buffer),
    );
  }
}
