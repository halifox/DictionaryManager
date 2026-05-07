import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../models/table_entry.dart';
import '../services/native_service.dart';

part 'dictionary_installer_provider.g.dart';

@riverpod
Future<List<int>> startInstallation(
  Ref ref,
  List<TableEntry> words,
) {
  return NativeService.insertBatch(words);
}
