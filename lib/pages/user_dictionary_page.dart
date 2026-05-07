import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:puredict/view/state_view.dart';

import '../providers/user_dictionary_provider.dart';
import '../services/native_service.dart';

class UserDictionaryPage extends HookConsumerWidget {
  const UserDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userDictState = ref.watch(loadUserDictionaryProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('用户词库'),
        centerTitle: true,
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
              const PopupMenuItem(value: 'clear', child: Text('清空词库')),
            ],
          ),
        ],
      ),
      body: userDictState.when(
        data: (words) {
          if (words.isEmpty) {
            return StateView.empty();
          }
          return ListView.builder(
            itemCount: words.length,
            itemBuilder: (context, index) {
              final word = words[index];
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
