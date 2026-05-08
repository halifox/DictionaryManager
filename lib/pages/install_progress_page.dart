import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/installed_dictionary.dart';
import '../generated/dictionary_api.g.dart';
import '../providers/dictionary_installer_provider.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/dictionary_storage.dart';
import '../view/state_view.dart';

class InstallProgressPage extends HookConsumerWidget {
  const InstallProgressPage({
    required this.words,
    required this.dictionaryName,
    this.category = 'local',
    this.source = 'local',
    super.key,
  });

  final List<TableEntryData> words;
  final String dictionaryName;
  final String category;
  final String source;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final installState = ref.watch(startInstallationProvider(words));
    final hasSaved = useState(false);

    useEffect(() {
      if (installState.hasValue && !hasSaved.value) {
        final ids = installState.value!;
        DictionaryStorage.saveDictionary(
          InstalledDictionary(
            name: dictionaryName,
            category: category,
            source: source,
            wordIds: ids,
            installedAt: DateTime.now(),
          ),
        ).then((_) {
          hasSaved.value = true;
          ref.invalidate(installedDictionariesProvider);
          ref.invalidate(isDictionaryInstalledProvider(dictionaryName));
        });
      }
      return null;
    }, [installState.hasValue]);

    return Scaffold(
      appBar: AppBar(
        title: const Text("安装词库"),
      ),
      body: installState.when(
        data: (ids) {
          return StateView.finish(title: '安装完成', message: '成功安装 ${ids.length} 条');
        },
        error: (error, stackTrace) {
          return StateView.error(message: error.toString());
        },
        loading: () {
          return StateView.loading(title: "安装中", message: "正在安装词库...");
        },
      ),
      floatingActionButton: installState.hasValue
          ? FloatingActionButton.extended(
              onPressed: () => Navigator.of(context).pop(),
              icon: const Icon(Icons.arrow_back),
              label: const Text('返回'),
            )
          : null,
    );
  }
}
