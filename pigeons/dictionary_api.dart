import 'package:pigeon/pigeon.dart';
// dart run pigeon --input pigeons/dictionary_api.dart
@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/generated/dictionary_api.g.dart',
    dartPackageName: 'puredict',
    kotlinOut:
        'android/app/src/main/kotlin/com/halifox/puredict/DictionaryApi.g.kt',
    kotlinOptions: KotlinOptions(package: 'com.halifox.puredict'),
  ),
)
class TableEntryData {
  final int? id;
  final String word;
  final int? frequency;
  final String? locale;
  final String? shortcut;
  final int? appId;

  TableEntryData({
    this.id,
    required this.word,
    this.frequency,
    this.locale,
    this.shortcut,
    this.appId,
  });
}

@HostApi()
abstract class DictionaryApi {
  bool checkImeStatus();

  void openImeSettings();

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  List<TableEntryData> queryWords();

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  List<int> addWords(List<TableEntryData> words);

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  void clearDictionary();

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  int removeWords(List<int> ids);

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  List<TableEntryData> findWords(List<int> ids);

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  int addWord(TableEntryData word);

  @TaskQueue(type: TaskQueueType.serialBackgroundThread)
  int modifyWord(int id, TableEntryData word);
}
