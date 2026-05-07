import 'package:flutter/material.dart';

import '../models/ime_format.dart';
import 'file_import_page.dart';
import 'installed_dictionaries_page.dart';
import 'pyim_dictionary_page.dart';
import 'user_dictionary_page.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('PureDict'),
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'installed') {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const InstalledDictionariesPage(),
                  ),
                );
              } else if (value == 'user') {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const UserDictionaryPage(),
                  ),
                );
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'installed',
                child: Row(
                  children: [
                    Icon(Icons.download_done_outlined),
                    SizedBox(width: 12),
                    Text('已安装词典'),
                  ],
                ),
              ),
              const PopupMenuItem(
                value: 'user',
                child: Row(
                  children: [
                    Icon(Icons.person_outline),
                    SizedBox(width: 12),
                    Text('用户词库'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: ListView.builder(
        itemCount: ImeFormatInfo.allFormats.length,
        itemBuilder: (context, index) {
          final formatInfo = ImeFormatInfo.allFormats[index];
          return ListTile(
            leading: Icon(
              formatInfo.icon,
              color: Theme.of(context).colorScheme.primary,
            ),
            title: Text(formatInfo.displayName),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {
              if (formatInfo.format == ImeFormat.pyimTable) {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const PyimDictionaryPage(),
                  ),
                );
              } else {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) =>
                        FileImportPage(formatInfo: formatInfo),
                  ),
                );
              }
            },
          );
        },
      ),
    );
  }
}
