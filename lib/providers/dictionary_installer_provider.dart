import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../generated/dictionary_api.g.dart';
import '../generated/dictionary_api.g.dart';

part 'dictionary_installer_provider.g.dart';

@riverpod
Future<List<int>> startInstallation(
  Ref ref,
  List<TableEntryData> words,
) {
  final api = DictionaryApi();
  return api.addWords(words);
}
