import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/native_service.dart';
import 'dictionary_preview_page.dart';

class InstalledDictionariesPage extends HookConsumerWidget {
  const InstalledDictionariesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colorScheme = Theme.of(context).colorScheme;
    final dictionariesAsync = ref.watch(installedDictionariesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('已安装词库'),
      ),
      body: dictionariesAsync.when(
        data: (dictionaries) {
          if (dictionaries.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.library_books_outlined,
                    size: 64,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    '暂无已安装词库，请先导入词库文件',
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            itemCount: dictionaries.length,
            itemBuilder: (context, index) {
              final dict = dictionaries[index];
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
                        Icons.book_outlined,
                        color: colorScheme.onPrimaryContainer,
                      ),
                    ),
                  ),
                  title: Text(
                    dict.displayName,
                    style: TextStyle(color: colorScheme.onSurface),
                  ),
                  trailing: Row(
                    mainAxisSize: .min,
                    children: [
                      if(dict.source=="local")
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: colorScheme.primaryContainer,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          '本地',
                          style: Theme.of(context).textTheme.labelMedium?.copyWith(
                            color: colorScheme.onPrimaryContainer,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      DecoratedBox(
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: colorScheme.primaryContainer,
                        ),
                        child: Padding(
                          padding: const EdgeInsets.all(8),
                          child: Icon(
                            Icons.arrow_forward,
                            color: colorScheme.onPrimaryContainer,
                          ),
                        ),
                      ),
                    ],
                  ),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => DictionaryPreviewPage(
                          loadData: () => NativeService.queryWordsByIds(dict.wordIds),
                          dictionaryName: dict.name,
                          category: dict.category,
                          source: dict.source,
                        ),
                      ),
                    );
                  },
                ),
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(
          child: Text('加载失败，请稍后重试'),
        ),
      ),
    );
  }
}
