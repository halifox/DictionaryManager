import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:puredict/view/state_view.dart';

import '../generated/dictionary_api.g.dart';
import '../providers/user_dictionary_provider.dart';
import 'user_dictionary_edit_page.dart';

class UserDictionaryPage extends HookConsumerWidget {
  const UserDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colorScheme = Theme.of(context).colorScheme;
    final userDictState = ref.watch(loadUserDictionaryProvider);
    final searchController = useTextEditingController();
    final searchQuery = useState('');

    return Scaffold(
      appBar: AppBar(
        title: const Text('用户词库'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const UserDictionaryEditPage(),
                ),
              );
            },
            tooltip: '添加词条',
          ),
          PopupMenuButton<String>(
            onSelected: (value) async {
              if (value == 'clear') {
                final confirmed = await showDialog<bool>(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('确认清空'),
                    content: const Text('确定要清空所有用户词库吗？此操作不可恢复。'),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context, false),
                        child: const Text('取消'),
                      ),
                      TextButton(
                        onPressed: () => Navigator.pop(context, true),
                        child: const Text('确定'),
                      ),
                    ],
                  ),
                );
                if (confirmed == true) {
                  try {
                    final api = DictionaryApi();
                    await api.clearDictionary();
                    ref.invalidate(loadUserDictionaryProvider);
                    if (context.mounted) {
                      ScaffoldMessenger.of(
                        context,
                      ).showSnackBar(const SnackBar(content: Text('用户词库已清空')));
                    }
                  } catch (e) {
                    if (context.mounted) {
                      ScaffoldMessenger.of(
                        context,
                      ).showSnackBar(const SnackBar(content: Text('清空失败，请稍后重试')));
                    }
                  }
                }
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'clear',
                child: Row(
                  children: [
                    Icon(Icons.delete_outline),
                    SizedBox(width: 12),
                    Text('清空词库'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: userDictState.when(
        data: (words) {
          if (words.isEmpty) {
            return StateView.empty();
          }

          final filteredWords = searchQuery.value.isEmpty
              ? words
              : words.where((word) {
                  final query = searchQuery.value.toLowerCase();
                  final wordText = word.word.toLowerCase();
                  final shortcut = (word.shortcut ?? '').toLowerCase();
                  return wordText.contains(query) || shortcut.contains(query);
                }).toList();

          return Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: TextField(
                  controller: searchController,
                  decoration: InputDecoration(
                    hintText: '搜索词语或拼音',
                    prefixIcon: const Icon(Icons.search),
                    suffixIcon: searchQuery.value.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear),
                            onPressed: () {
                              searchController.clear();
                              searchQuery.value = '';
                            },
                          )
                        : null,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  onChanged: (value) {
                    searchQuery.value = value;
                  },
                ),
              ),
              Expanded(
                child: filteredWords.isEmpty
                    ? Center(
                        child: Text(
                          '未找到匹配的词条',
                          style: Theme.of(context).textTheme.bodyLarge,
                        ),
                      )
                    : ListView.builder(
                        itemCount: filteredWords.length,
                        itemBuilder: (context, index) {
                          final word = filteredWords[index];
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
                                word.word,
                                style: TextStyle(
                                  fontWeight: FontWeight.w500,
                                  color: colorScheme.onSurface,
                                ),
                              ),
                              subtitle: Text(word.shortcut ?? ""),
                              onTap: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => UserDictionaryEditPage(entry: word),
                                  ),
                                );
                              },
                            ),
                          );
                        },
                      ),
              ),
            ],
          );
        },
        error: (error, stackTrace) {
          return StateView.error(message: error.toString());
        },
        loading: () {
          return StateView.loading();
        },
      ),
    );
  }
}
