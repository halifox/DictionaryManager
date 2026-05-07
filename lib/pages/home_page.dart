import 'package:flutter/material.dart';
import 'pyim_dictionary_page.dart';
import 'user_dictionary_page.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('词典管理器'),
        centerTitle: true,
      ),
      body: ListView(
        children: [
          ListTile(
            leading: Icon(
              Icons.library_books_outlined,
              color: Theme.of(context).colorScheme.primary,
            ),
            title: const Text('Pyim 词库管理'),
            subtitle: const Text('管理和导入 Pyim 词库'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const PyimDictionaryPage(),
                ),
              );
            },
          ),
          ListTile(
            leading: Icon(
              Icons.person_outline,
              color: Theme.of(context).colorScheme.primary,
            ),
            title: const Text('用户词典'),
            subtitle: const Text('查看已安装的用户词典'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const UserDictionaryPage(),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}
