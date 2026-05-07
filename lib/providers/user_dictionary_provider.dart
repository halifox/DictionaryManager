import 'package:puredict/models/table_entry.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../services/native_service.dart';

part 'user_dictionary_provider.g.dart';

@riverpod
Future<List<TableEntry>> loadUserDictionary(Ref ref) async {
  return await NativeService.queryUserDictionary();
}
