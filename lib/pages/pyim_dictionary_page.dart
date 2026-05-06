import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'dictionary_preview_page.dart';

class PyimDictionaryPage extends HookConsumerWidget {
  const PyimDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dictionaries = [
      {'name': '艺术', 'icon': Icons.palette_outlined},
      {'name': '科技', 'icon': Icons.computer_outlined},
      {'name': '医学', 'icon': Icons.medical_services_outlined},
      {'name': '法律', 'icon': Icons.gavel_outlined},
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Pyim 词库'),
        centerTitle: true,
      ),
      body: ListView.builder(
        itemCount: dictionaries.length,
        itemBuilder: (context, index) {
          final dict = dictionaries[index];
          return ListTile(
            leading: Icon(
              dict['icon'] as IconData,
              color: Theme.of(context).colorScheme.primary,
            ),
            title: Text(dict['name'] as String),
            subtitle: const Text('暂无词库'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {},
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => const DictionaryPreviewPage(),
            ),
          );
        },
        icon: const Icon(Icons.folder_open),
        label: const Text('本地导入'),
      ),
    );
  }
}
