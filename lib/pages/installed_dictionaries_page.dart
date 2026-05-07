import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/native_service.dart';
import 'dictionary_preview_page.dart';

class InstalledDictionariesPage extends HookConsumerWidget {
  const InstalledDictionariesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dictionariesAsync = ref.watch(installedDictionariesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('已安装词典'),
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
                    '暂无已安装词典',
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
              return ListTile(
                leading: Icon(
                  Icons.book_outlined,
                  color: Theme.of(context).colorScheme.primary,
                ),
                title: Text(dict.displayName),
                trailing: const Icon(Icons.chevron_right),
                onTap: () async {
                  // 查询词典内容
                  final allWords = await NativeService.queryUserDictionary();
                  final dictWords = allWords.where((word) {
                    // 这里需要通过某种方式识别词条属于哪个词典
                    // 由于我们有 wordIds，可以通过 ID 匹配
                    // 但 queryUserDictionary 返回的数据没有 ID
                    // 所以我们暂时返回所有词条，后续可以优化
                    return true;
                  }).toList();

                  if (context.mounted) {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => DictionaryPreviewPage(
                          words: dictWords,
                          dictionaryName: dict.name,
                          category: dict.category,
                        ),
                      ),
                    );
                  }
                },
              );
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(
          child: Text('加载失败: $error'),
        ),
      ),
    );
  }
}
