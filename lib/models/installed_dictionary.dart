class InstalledDictionary {
  final String name;
  final String category;
  final String source;
  final List<int> wordIds;
  final DateTime installedAt;

  InstalledDictionary({
    required this.name,
    required this.category,
    this.source = 'local',
    required this.wordIds,
    required this.installedAt,
  });

  String get displayName => '[$category]$name';

  Map<String, dynamic> toJson() => {
        'name': name,
        'category': category,
        'source': source,
        'wordIds': wordIds,
        'installedAt': installedAt.toIso8601String(),
      };

  factory InstalledDictionary.fromJson(Map<String, dynamic> json) {
    return InstalledDictionary(
      name: json['name'] as String,
      category: json['category'] as String? ?? 'local',
      source: json['source'] as String? ?? 'local',
      wordIds: (json['wordIds'] as List).cast<int>(),
      installedAt: DateTime.parse(json['installedAt'] as String),
    );
  }
}
