class TableEntry {
  final String word;
  final String? shortcut;
  final int frequency;
  final String? locale;

  TableEntry({
    required this.word,
    this.shortcut,
    required this.frequency,
    this.locale,
  });

  factory TableEntry.fromLine(String line) {
    final parts = line.split('\t');
    if (parts.length != 4) {
      throw FormatException('Invalid line format: $line');
    }

    return TableEntry(
      word: parts[1],
      shortcut: parts[0],
      frequency: int.parse(parts[3]),
      locale: 'zh_CN',
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'word': word,
      'shortcut': shortcut,
      'frequency': frequency,
      'locale': locale ?? 'zh_CN',
    };
  }

  @override
  String toString() {
    return 'TableEntry(word: $word, shortcut: $shortcut, frequency: $frequency, locale: $locale)';
  }
}
