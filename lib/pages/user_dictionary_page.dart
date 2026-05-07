import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:puredict/view/state_view.dart';

import '../providers/user_dictionary_provider.dart';
import '../services/native_service.dart';

class UserDictionaryPage extends HookConsumerWidget {
  const UserDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userDictState = ref.watch(loadUserDictionaryProvider);
    final searchController = useTextEditingController();
    final searchQuery = useState('');

    return Scaffold(
      appBar: AppBar(
        title: const Text('用户词库'),
        actions: [
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
                    await NativeService.clearUserDictionary();
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
                      ).showSnackBar(SnackBar(content: Text('清空失败: $e')));
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
                          '未找到匹配结果',
                          style: Theme.of(context).textTheme.bodyLarge,
                        ),
                      )
                    : ListView.builder(
                        itemCount: filteredWords.length,
                        itemBuilder: (context, index) {
                          final word = filteredWords[index];
                          return ListTile(
                            leading: Icon(
                              Icons.text_fields,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                            title: Text(
                              word.word,
                              style: const TextStyle(fontWeight: FontWeight.w500),
                            ),
                            subtitle: Text(word.shortcut ?? ""),
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
