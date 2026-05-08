import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../generated/dictionary_api.g.dart';
import '../generated/dictionary_api.g.dart';
import '../providers/user_dictionary_provider.dart';

class UserDictionaryEditPage extends HookConsumerWidget {
  final TableEntryData? entry;

  const UserDictionaryEditPage({super.key, this.entry});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isNewEntry = entry == null;

    final wordController = useTextEditingController(text: entry?.word ?? '');
    final shortcutController = useTextEditingController(
      text: entry?.shortcut ?? '',
    );
    final frequencyController = useTextEditingController(
      text: entry?.frequency.toString() ?? '',
    );
    final selectedLocale = useState<String?>(entry?.locale);
    final appIdController = useTextEditingController(
      text: entry?.appId?.toString() ?? '',
    );

    final isSaving = useState(false);

    Future<void> handleSave() async {
      if (wordController.text.trim().isEmpty) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('词语不能为空')));
        return;
      }

      final frequencyText = frequencyController.text.trim();
      int? frequency;
      if (frequencyText.isNotEmpty) {
        frequency = int.tryParse(frequencyText);
        if (frequency == null || frequency < 1 || frequency > 255) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(const SnackBar(content: Text('频率必须在 1-255 之间或留空')));
          return;
        }
      }

      isSaving.value = true;

      try {
        final api = DictionaryApi();
        final newEntry = TableEntryData(
          id: entry?.id,
          word: wordController.text.trim(),
          shortcut: shortcutController.text.trim().isEmpty
              ? null
              : shortcutController.text.trim(),
          frequency: frequency ?? 1,
          locale: selectedLocale.value,
          appId: appIdController.text.trim().isEmpty
              ? null
              : int.tryParse(appIdController.text.trim()),
        );

        if (isNewEntry) {
          await api.addWord(newEntry);
        } else {
          await api.modifyWord(entry!.id!, newEntry);
        }

        ref.invalidate(loadUserDictionaryProvider);

        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(isNewEntry ? '词条已添加' : '词条已更新')),
          );
          Navigator.pop(context);
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(SnackBar(content: Text('保存失败: $e')));
        }
      } finally {
        isSaving.value = false;
      }
    }

    Future<void> handleDelete() async {
      if (isNewEntry) return;

      final confirmed = await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('确认删除'),
          content: const Text('确定要删除这个词条吗？此操作不可恢复。'),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('删除'),
            ),
          ],
        ),
      );

      if (confirmed != true) return;

      try {
        final api = DictionaryApi();
        await api.removeWords([entry!.id!]);
        ref.invalidate(loadUserDictionaryProvider);

        if (context.mounted) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(const SnackBar(content: Text('词条已删除')));
          Navigator.pop(context);
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(
            context,
          ).showSnackBar(SnackBar(content: Text('删除失败: $e')));
        }
      }
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(isNewEntry ? '添加词条' : '编辑词条'),
        actions: [
          if (!isNewEntry)
            IconButton(
              icon: const Icon(Icons.delete_outline),
              onPressed: handleDelete,
              tooltip: '删除词条',
            ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          if (!isNewEntry)
            Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: TextField(
                enabled: false,
                decoration: InputDecoration(
                  labelText: 'ID',
                  hintText: '${entry?.id}',
                  helperText: '词条的唯一标识符，由系统自动生成，不可修改。',
                  helperMaxLines: 2,
                  border: const OutlineInputBorder(),
                ),
                controller: TextEditingController(text: '${entry?.id}'),
              ),
            ),
          Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: TextField(
              controller: wordController,
              decoration: const InputDecoration(
                labelText: '词语',
                hintText: '请输入词语',
                helperText: '要添加到用户词典的词语。这是必填字段。',
                helperMaxLines: 2,
                border: OutlineInputBorder(),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: TextField(
              controller: shortcutController,
              decoration: const InputDecoration(
                labelText: '快捷方式',
                hintText: '请输入快捷方式',
                helperText: '当输入这个快捷方式时，支持的输入法会将此行中的词语作为备选拼写建议。例如，输入拼音时会提示对应的词语。',
                helperMaxLines: 3,
                border: OutlineInputBorder(),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: TextField(
              controller: frequencyController,
              decoration: const InputDecoration(
                labelText: '频率',
                hintText: '请输入频率 (1-255，可留空)',
                helperText: '介于 1 到 255 之间的值。数值越大表示使用频率越高，在输入法候选列表中的优先级也越高。留空时默认为 1。',
                helperMaxLines: 3,
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
              inputFormatters: [
                FilteringTextInputFormatter.digitsOnly,
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: DropdownButtonFormField<String?>(
              value: selectedLocale.value,
              decoration: const InputDecoration(
                labelText: '语言区域',
                helperText: '此词条所属的语言区域。如果为空，则适用于所有区域。区域格式由 Locale.toString() 返回的字符串定义。',
                helperMaxLines: 3,
                border: OutlineInputBorder(),
              ),
              items: const [
                DropdownMenuItem(value: null, child: Text('所有区域')),
                DropdownMenuItem(value: 'zh_CN', child: Text('简体中文 (zh_CN)')),
              ],
              onChanged: (value) {
                selectedLocale.value = value;
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: TextField(
              controller: appIdController,
              decoration: const InputDecoration(
                labelText: '应用 ID',
                hintText: '请输入应用 ID（可留空）',
                helperText: '插入该词条的应用程序的 UID（用户标识符）。通常由系统自动设置，一般情况下无需手动修改。',
                helperMaxLines: 3,
                border: OutlineInputBorder(),
              ),
              keyboardType: TextInputType.number,
              inputFormatters: [
                FilteringTextInputFormatter.digitsOnly,
              ],
            ),
          ),
          const SizedBox(height: 64),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: isSaving.value ? null : handleSave,
        icon: isSaving.value
            ? const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              )
            : const Icon(Icons.save),
        label: Text(isSaving.value ? '保存中...' : '保存'),
      ),
    );
  }
}
