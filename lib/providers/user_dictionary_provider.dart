import 'package:puredict/models/table_entry.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../generated/dictionary_api.g.dart';

part 'user_dictionary_provider.g.dart';

@riverpod
Future<List<TableEntryData>> loadUserDictionary(Ref ref) async {
  final api = DictionaryApi();
  return await api.queryWords();
}
