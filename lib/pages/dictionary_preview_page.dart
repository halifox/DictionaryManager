import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/table_entry.dart';
import '../services/native_service.dart';
import 'ime_required_page.dart';
import 'install_progress_page.dart';

class DictionaryPreviewPage extends HookConsumerWidget {
  const DictionaryPreviewPage(this.words, {super.key});

  final List<TableEntry> words;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('词库预览'), centerTitle: true),
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
      floatingActionButton: FloatingActionButton.extended(
        icon: const Icon(Icons.download),
        label: const Text('安装'),
        onPressed: () async {
          final isEnabled = await NativeService.isImeEnabled();
          if (!isEnabled) {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => const ImeRequiredPage()),
            );
            return;
          }

          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => InstallProgressPage(words)),
          );
        },
      ),
    );
  }
}
