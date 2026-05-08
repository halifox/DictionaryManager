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
    final colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('PureDict'),
        actions: [
          PopupMenuButton<String>(
            icon: Icon(Icons.more_vert),
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
              PopupMenuItem(
                value: 'installed',
                child: Row(
                  children: [
                    Icon(Icons.download_done_outlined),
                    const SizedBox(width: 12),
                    Text('已安装词库'),
                  ],
                ),
              ),
              PopupMenuItem(
                value: 'user',
                child: Row(
                  children: [
                    Icon(Icons.person_outline),
                    const SizedBox(width: 12),
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
          return Padding(
            padding: .symmetric(vertical: 4, horizontal: 8),
            child: ListTile(
              contentPadding: .symmetric(vertical: 4, horizontal: 12),
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
                    formatInfo.icon,
                    color: colorScheme.onPrimaryContainer,
                  ),
                ),
              ),
              title: Text(
                formatInfo.displayName,
                style: TextStyle(color: colorScheme.onSurface),
              ),
              trailing: DecoratedBox(
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
            ),
          );
        },
      ),
    );
  }
}
