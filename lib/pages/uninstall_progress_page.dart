import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../generated/dictionary_api.g.dart';
import '../models/installed_dictionary.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/dictionary_storage.dart';
import '../view/state_view.dart';

part 'uninstall_progress_page.g.dart';

@riverpod
Future<void> uninstallDictionary(Ref ref, InstalledDictionary dictionary) async {
  final api = DictionaryApi();
  await api.removeWords(dictionary.wordIds);
  await DictionaryStorage.removeDictionary(dictionary.name);
}

class UninstallProgressPage extends HookConsumerWidget {
  const UninstallProgressPage({
    required this.dictionary,
    super.key,
  });

  final InstalledDictionary dictionary;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uninstallState = ref.watch(uninstallDictionaryProvider(dictionary));

    return Scaffold(
      appBar: AppBar(
        title: const Text("卸载词库"),
      ),
      body: uninstallState.when(
        data: (_) {
          // 卸载完成后刷新状态
          Future.microtask(() {
            ref.invalidate(installedDictionariesProvider);
            ref.invalidate(isDictionaryInstalledProvider(dictionary.name));
          });

          return StateView.finish(
            title: '卸载完成',
            message: '已成功卸载 ${dictionary.wordIds.length} 条',
          );
        },
        error: (error, stackTrace) {
          return StateView.error(message: error.toString());
        },
        loading: () {
          return StateView.loading(title: "卸载中", message: "正在卸载词库...");
        },
      ),
      floatingActionButton: uninstallState.hasValue
          ? FloatingActionButton.extended(
              onPressed: () => Navigator.of(context).pop(),
              icon: const Icon(Icons.arrow_back),
              label: const Text('返回'),
            )
          : null,
    );
  }
}
