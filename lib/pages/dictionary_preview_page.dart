import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:puredict/view/state_view.dart';

import '../generated/dictionary_api.g.dart';
import '../generated/dictionary_api.g.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/dictionary_storage.dart';
import 'ime_required_page.dart';
import 'install_progress_page.dart';
import 'uninstall_progress_page.dart';

class DictionaryPreviewPage extends HookConsumerWidget {
  const DictionaryPreviewPage({
    required this.loadData,
    required this.dictionaryName,
    this.category = 'local',
    this.source = 'local',
    super.key,
  });

  final Future<List<TableEntryData>> Function() loadData;
  final String dictionaryName;
  final String category;
  final String source;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colorScheme = Theme.of(context).colorScheme;
    final isInstalledAsync = ref.watch(isDictionaryInstalledProvider(dictionaryName));

    final loadedWords = useState<List<TableEntryData>?>(null);
    final isLoading = useState(true);
    final error = useState<String?>(null);

    useEffect(() {
      loadData().then((data) {
        loadedWords.value = data;
        isLoading.value = false;
      }).catchError((e) {
        error.value = e.toString();
        isLoading.value = false;
      });
      return null;
    }, []);

    final displayWords = loadedWords.value;

    return Scaffold(
      appBar: AppBar(
        title: Text(displayWords != null ? '词库预览 (${displayWords.length} 条)' : '词库预览'),
      ),
      body: isLoading.value
          ? StateView.loading()
          : error.value != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.error_outline, size: 64, color: colorScheme.error),
                      const SizedBox(height: 16),
                      Text('加载失败: ${error.value}'),
                    ],
                  ),
                )
              : displayWords == null || displayWords.isEmpty
                  ? const Center(child: Text('暂无词条'))
                  : ListView.builder(
                      itemCount: displayWords.length,
                      itemBuilder: (context, index) {
                        final entry = displayWords[index];
                        return Padding(
                          padding: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                          child: ListTile(
                            contentPadding: const EdgeInsets.symmetric(vertical: 4, horizontal: 12),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(16),
                            ),
                            tileColor: colorScheme.primaryContainer.withAlpha(100),
                            leading: DecoratedBox(
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                color: colorScheme.primaryContainer,
                              ),
                              child: Padding(
                                padding: const EdgeInsets.all(8),
                                child: Icon(
                                  Icons.text_fields,
                                  color: colorScheme.onPrimaryContainer,
                                ),
                              ),
                            ),
                            title: Text(
                              entry.word,
                              style: TextStyle(color: colorScheme.onSurface),
                            ),
                            subtitle: Text(entry.shortcut ?? ""),
                          ),
                        );
                      },
                    ),
      floatingActionButton: displayWords == null
          ? null
          : isInstalledAsync.when(
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
                    content: Text('确定要卸载"$dictionaryName"词库吗？'),
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
                    final api = DictionaryApi();
                    final isEnabled = await api.checkImeStatus();
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
                            words: displayWords,
                            dictionaryName: dictionaryName,
                            category: category,
                            source: source,
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
                  final api = DictionaryApi();
                  final isEnabled = await api.checkImeStatus();
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
                          words: displayWords,
                          dictionaryName: dictionaryName,
                          category: category,
                          source: source,
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
