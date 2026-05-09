import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/ime_format.dart';
import '../parsers/text/pyim_table_parser.dart';
import '../providers/installed_dictionaries_provider.dart';
import 'dictionary_download_page.dart';
import 'dictionary_preview_page.dart';

class PyimDictionaryPage extends HookConsumerWidget {
  const PyimDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colorScheme = Theme.of(context).colorScheme;

    final dictionaries = [
      {'name': '艺术', 'icon': Icons.palette_outlined, 'file': 'art.table'},
      {
        'name': '文化',
        'icon': Icons.auto_stories_outlined,
        'file': 'culture.table',
      },
      {
        'name': '经济',
        'icon': Icons.attach_money_outlined,
        'file': 'economy.table',
      },
      {'name': '地质', 'icon': Icons.terrain_outlined, 'file': 'geology.table'},
      {
        'name': '历史',
        'icon': Icons.history_edu_outlined,
        'file': 'history.table',
      },
      {'name': '生活', 'icon': Icons.home_outlined, 'file': 'life.table'},
      {'name': '自然', 'icon': Icons.nature_outlined, 'file': 'nature.table'},
      {'name': '人物', 'icon': Icons.person_outline, 'file': 'people.table'},
      {'name': '科学', 'icon': Icons.science_outlined, 'file': 'science.table'},
      {'name': '社会', 'icon': Icons.groups_outlined, 'file': 'society.table'},
      {
        'name': '体育',
        'icon': Icons.sports_soccer_outlined,
        'file': 'sport.table',
      },
      {
        'name': '科技',
        'icon': Icons.computer_outlined,
        'file': 'technology.table',
      },
    ];

    return Scaffold(
      appBar: AppBar(title: const Text('Pyim 词库')),
      body: ListView.builder(
        itemCount: dictionaries.length,
        itemBuilder: (context, index) {
          final dict = dictionaries[index];
          final fileName = dict['file'] as String;
          final dictName = dict['name'] as String;

          final isInstalledAsync = ref.watch(
            isDictionaryInstalledProvider(dictName, 'online', 'pyim'),
          );

          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
            child: ListTile(
              contentPadding: const EdgeInsets.symmetric(
                vertical: 4,
                horizontal: 12,
              ),
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
                    dict['icon'] as IconData,
                    color: colorScheme.onPrimaryContainer,
                  ),
                ),
              ),
              title: Text(
                dictName,
                style: TextStyle(color: colorScheme.onSurface),
              ),
              trailing: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  isInstalledAsync.when(
                    data: (data) {
                      if (!data) {
                        return const SizedBox.shrink();
                      }
                      return Container(
                        margin: .symmetric(horizontal: 8),
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: colorScheme.primaryContainer,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          '已安装',
                          style: Theme.of(context).textTheme.labelMedium
                              ?.copyWith(color: colorScheme.onPrimaryContainer),
                        ),
                      );
                    },
                    loading: () {
                      return const SizedBox.shrink();
                    },
                    error: (error, stackTrace) {
                      return const SizedBox.shrink();
                    },
                  ),
                  DecoratedBox(
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
                ],
              ),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => DictionaryDownloadPage(
                      fileName: fileName,
                      dictionaryName: dictName,
                      format: ImeFormat.pyimTable,
                    ),
                  ),
                );
              },
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        icon: const Icon(Icons.folder_open),
        label: const Text('本地导入'),
        onPressed: () async {
          final result = await FilePicker.platform.pickFiles(
            type: FileType.any,
          );

          if (result != null && result.files.single.path != null) {
            final filePath = result.files.single.path!;
            final fileName = result.files.single.name;

            if (!fileName.endsWith('.table')) {
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text("请选择 .table 格式的文件"),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              }
              return;
            }

            if (context.mounted) {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => DictionaryPreviewPage(
                    loadData: () async {
                      final parser = PyimTableParser();
                      final result = await parser.parseFile(filePath);
                      return result.entries;
                    },
                    dictionaryName: fileName.replaceAll('.table', ''),
                    category: 'pyim',
                    source: 'local',
                  ),
                ),
              );
            }
          }
        },
      ),
    );
  }
}
