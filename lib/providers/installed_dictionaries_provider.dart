import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../generated/dictionary_api.g.dart';
import '../models/installed_dictionary.dart';
import '../services/dictionary_storage.dart';

part 'installed_dictionaries_provider.g.dart';

@riverpod
class InstalledDictionaries extends _$InstalledDictionaries {
  @override
  Future<List<InstalledDictionary>> build() async {
    return await DictionaryStorage.getInstalledDictionaries();
  }

  Future<void> refresh() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      return await DictionaryStorage.getInstalledDictionaries();
    });
  }

  Future<void> uninstall(String name, String source, String category) async {
    final dictionary = await DictionaryStorage.getDictionary(name, source, category);
    if (dictionary == null) return;

    final api = DictionaryApi();
    await api.removeWords(dictionary.wordIds);
    await DictionaryStorage.removeDictionary(name, source, category);
    await refresh();
  }
}

@riverpod
Future<bool> isDictionaryInstalled(Ref ref, String name, String source, String category) async {
  return await DictionaryStorage.isDictionaryInstalled(name, source, category);
}
