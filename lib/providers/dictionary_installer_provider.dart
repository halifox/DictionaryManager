import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../models/table_entry.dart';
import '../services/native_service.dart';

part 'dictionary_installer_provider.g.dart';

@riverpod
Future<void> startInstallation(Ref ref, List<TableEntry> words) async {
  return await NativeService.insertBatch(words);
}
