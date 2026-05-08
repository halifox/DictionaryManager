import '../generated/dictionary_api.g.dart';

extension TableEntryDataExt on TableEntryData {
  static TableEntryData fromLine(String line) {
    final parts = line.split('\t');
    if (parts.length != 4) {
      throw FormatException('Invalid line format: $line');
    }

    return TableEntryData(
      word: parts[1],
      shortcut: parts[0],
      frequency: int.parse(parts[3]),
      locale: 'zh_CN',
    );
  }
}
