import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../providers/dictionary_download_provider.dart';
import '../providers/installed_dictionaries_provider.dart';
import '../services/table_parser.dart';
import 'dictionary_download_page.dart';
import 'dictionary_preview_page.dart';

class PyimDictionaryPage extends HookConsumerWidget {
  const PyimDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dictionaries = [
      {'name': '艺术', 'icon': Icons.palette_outlined, 'file': 'art.table'},
      {'name': '文化', 'icon': Icons.auto_stories_outlined, 'file': 'culture.table'},
      {'name': '经济', 'icon': Icons.attach_money_outlined, 'file': 'economy.table'},
      {'name': '地质', 'icon': Icons.terrain_outlined, 'file': 'geology.table'},
      {'name': '历史', 'icon': Icons.history_edu_outlined, 'file': 'history.table'},
      {'name': '生活', 'icon': Icons.home_outlined, 'file': 'life.table'},
      {'name': '自然', 'icon': Icons.nature_outlined, 'file': 'nature.table'},
      {'name': '人物', 'icon': Icons.person_outline, 'file': 'people.table'},
      {'name': '科学', 'icon': Icons.science_outlined, 'file': 'science.table'},
      {'name': '社会', 'icon': Icons.groups_outlined, 'file': 'society.table'},
      {'name': '体育', 'icon': Icons.sports_soccer_outlined, 'file': 'sport.table'},
      {'name': '科技', 'icon': Icons.computer_outlined, 'file': 'technology.table'},
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Pyim 词库'),
      ),
      body: ListView.builder(
        itemCount: dictionaries.length,
        itemBuilder: (context, index) {
          final dict = dictionaries[index];
          final fileName = dict['file'] as String;
          final dictName = dict['name'] as String;

          final isDownloadedAsync = ref.watch(isDictionaryDownloadedProvider(fileName));
          final isInstalledAsync = ref.watch(isDictionaryInstalledProvider(dictName));

          return isDownloadedAsync.when(
            data: (isDownloaded) {
              return isInstalledAsync.when(
                data: (isInstalled) {
                  return ListTile(
                    leading: Icon(
                      dict['icon'] as IconData,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                    title: Text(dictName),
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (isInstalled)
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: Theme.of(context).colorScheme.primaryContainer,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Text(
                              '已安装',
                              style: Theme.of(context).textTheme.labelMedium?.copyWith(
                                color: Theme.of(context).colorScheme.onPrimaryContainer,
                              ),
                            ),
                          ),
                        const SizedBox(width: 8),
                        const Icon(Icons.chevron_right),
                      ],
                    ),
                    onTap: () async {
                      if (isDownloaded) {
                        final path = await ref.read(getDictionaryPathProvider(fileName).future);
                        final words = await TableParser.parseFile(path);

                        if (context.mounted) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => DictionaryPreviewPage(
                                words: words,
                                dictionaryName: dictName,
                                category: 'pyim',
                              ),
                            ),
                          );
                        }
                      } else {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => DictionaryDownloadPage(
                              fileName: fileName,
                              dictionaryName: dictName,
                            ),
                          ),
                        );
                      }
                    },
                  );
                },
                loading: () => ListTile(
                  leading: Icon(
                    dict['icon'] as IconData,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  title: Text(dictName),
                  trailing: const Icon(Icons.chevron_right),
                ),
                error: (error, stackTrace) => ListTile(
                  leading: Icon(
                    dict['icon'] as IconData,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  title: Text(dictName),
                  trailing: const Icon(Icons.chevron_right),
                ),
              );
            },
            loading: () {
              return ListTile(
                leading: Icon(
                  dict['icon'] as IconData,
                  color: Theme.of(context).colorScheme.primary,
                ),
                title: Text(dictName),
                subtitle: const Text('检查中...'),
                trailing: const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
              );
            },
            error: (error, _) {
              return ListTile(
                leading: Icon(
                  dict['icon'] as IconData,
                  color: Theme.of(context).colorScheme.primary,
                ),
                title: Text(dictName),
                subtitle: const Text('检查失败'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => DictionaryDownloadPage(
                        fileName: fileName,
                        dictionaryName: dictName,
                      ),
                    ),
                  );
                },
              );
            },
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
                  SnackBar(
                    content: Text("请选择 .table 格式的文件"),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              }
              return;
            }

            final words = await TableParser.parseFile(filePath);
            if (context.mounted) {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => DictionaryPreviewPage(
                    words: words,
                    dictionaryName: fileName.replaceAll('.table', ''),
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
