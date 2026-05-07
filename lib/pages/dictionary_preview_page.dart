import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/table_entry.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/dictionary_storage.dart';
import '../services/native_service.dart';
import 'ime_required_page.dart';
import 'install_progress_page.dart';
import 'uninstall_progress_page.dart';

class DictionaryPreviewPage extends HookConsumerWidget {
  const DictionaryPreviewPage({
    required this.words,
    required this.dictionaryName,
    this.category = 'local',
    super.key,
  });

  final List<TableEntry> words;
  final String dictionaryName;
  final String category;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isInstalledAsync = ref.watch(isDictionaryInstalledProvider(dictionaryName));

    return Scaffold(
      appBar: AppBar(
        title: Text('词库预览 (${words.length})'),
      ),
      body: ListView.builder(
        itemCount: words.length,
        itemBuilder: (context, index) {
          final entry = words[index];
          return ListTile(
            leading: Icon(
              Icons.text_fields,
              color: Theme.of(context).colorScheme.primary,
            ),
            title: Text(entry.word),
            subtitle: Text(entry.shortcut ?? ""),
          );
        },
      ),
      floatingActionButton: isInstalledAsync.when(
        data: (isInstalled) {
          if (isInstalled) {
            return FloatingActionButton.extended(
              icon: const Icon(Icons.delete_outline),
              label: const Text('卸载'),
              backgroundColor: Theme.of(context).colorScheme.errorContainer,
              foregroundColor: Theme.of(context).colorScheme.onErrorContainer,
              onPressed: () async {
                final confirmed = await showDialog<bool>(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('确认卸载'),
                    content: Text('确定要卸载「$dictionaryName」词典吗？'),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context, false),
                        child: const Text('取消'),
                      ),
                      TextButton(
                        onPressed: () => Navigator.pop(context, true),
                        child: const Text('卸载'),
                      ),
                    ],
                  ),
                );

                if (confirmed == true && context.mounted) {
                  final dictionary = await DictionaryStorage.getDictionary(dictionaryName);
                  if (dictionary != null && context.mounted) {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => UninstallProgressPage(dictionary: dictionary),
                      ),
                    );
                  }
                }
              },
            );
          }

          return FloatingActionButton.extended(
            icon: const Icon(Icons.download),
            label: const Text('安装'),
            onPressed: () async {
              final isEnabled = await NativeService.isImeEnabled();
              if (!isEnabled) {
                if (context.mounted) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const ImeRequiredPage()),
                  );
                }
                return;
              }

              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => InstallProgressPage(
                      words: words,
                      dictionaryName: dictionaryName,
                      category: category,
                    ),
                  ),
                );
              }
            },
          );
        },
        loading: () => FloatingActionButton.extended(
          icon: const SizedBox(
            width: 16,
            height: 16,
            child: CircularProgressIndicator(strokeWidth: 2),
          ),
          label: const Text('检查中'),
          onPressed: null,
        ),
        error: (error, stackTrace) => FloatingActionButton.extended(
          icon: const Icon(Icons.download),
          label: const Text('安装'),
          onPressed: () async {
            final isEnabled = await NativeService.isImeEnabled();
            if (!isEnabled) {
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const ImeRequiredPage()),
                );
              }
              return;
            }

            if (context.mounted) {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => InstallProgressPage(
                    words: words,
                    dictionaryName: dictionaryName,
                    category: category,
                  ),
                ),
              );
            }
          },
        ),
      ),
    );
  }
}
