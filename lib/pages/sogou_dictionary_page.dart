import 'dart:convert';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import '../models/ime_format.dart';
import 'dictionary_download_page.dart';
import 'dictionary_preview_page.dart';
import '../parsers/parser_factory.dart';

class SogouDictionaryPage extends HookConsumerWidget {
  const SogouDictionaryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colorScheme = Theme.of(context).colorScheme;
    final categories = useState<List<dynamic>>([]);
    final selectedPath = useState<List<String>>([]);
    final isLoading = useState(true);

    useEffect(() {
      Future<void> loadData() async {
        final jsonString = await rootBundle.loadString('assets/sogou.json');
        final data = jsonDecode(jsonString) as List<dynamic>;
        categories.value = data;
        isLoading.value = false;
      }
      loadData();
      return null;
    }, []);

    List<dynamic> flattenDicts(List<dynamic> items) {
      final result = <dynamic>[];
      for (final item in items) {
        if (item['type'] == 'dict') {
          result.add(item);
        } else if (item['type'] == 'category' && item['children'] != null) {
          result.addAll(flattenDicts(item['children'] as List));
        }
      }
      return result;
    }

    dynamic getCurrentCategory() {
      if (selectedPath.value.isEmpty) return null;

      dynamic current;
      List<dynamic> searchList = categories.value;

      for (final id in selectedPath.value) {
        current = searchList.firstWhere(
          (c) => c['id'] == id,
          orElse: () => null,
        );
        if (current == null) return null;
        searchList = (current['children'] as List?) ?? [];
      }

      return current;
    }

    List<dynamic> getDictList() {
      if (selectedPath.value.isEmpty) {
        // 没有选择分类时，显示所有词库
        return flattenDicts(categories.value);
      }

      final current = getCurrentCategory();
      if (current == null) return [];

      final children = (current['children'] as List?) ?? [];
      return flattenDicts(children);
    }

    if (isLoading.value) {
      return Scaffold(
        appBar: AppBar(title: const Text('搜狗词库')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    List<List<dynamic>> getCategoryLevels() {
      final levels = <List<dynamic>>[];
      levels.add(categories.value);

      List<dynamic> currentList = categories.value;
      for (int i = 0; i < selectedPath.value.length; i++) {
        final selectedId = selectedPath.value[i];
        final selected = currentList.firstWhere(
          (c) => c['id'] == selectedId,
          orElse: () => null,
        );
        if (selected == null || selected['children'] == null) break;

        final children = (selected['children'] as List)
            .where((c) => c['type'] == 'category')
            .toList();
        if (children.isEmpty) break;

        levels.add(children);
        currentList = selected['children'] as List;
      }

      return levels;
    }

    final categoryLevels = getCategoryLevels();
    final dictList = getDictList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('搜狗词库'),
      ),
      body: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                for (int level = 0; level < categoryLevels.length; level++) ...[
                  if (level > 0) const SizedBox(height: 12),
                  SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: categoryLevels[level].map((cat) {
                        final catId = cat['id'] as String;
                        final isSelected = selectedPath.value.length > level &&
                                          selectedPath.value[level] == catId;
                        return ChoiceChip(
                          label: Text(cat['name'] as String),
                          selected: isSelected,
                          onSelected: (selected) {
                            if (selected) {
                              selectedPath.value = [
                                ...selectedPath.value.sublist(0, level),
                                catId,
                              ];
                            } else {
                              selectedPath.value = selectedPath.value.sublist(0, level);
                            }
                          },
                          selectedColor: level == 0
                              ? colorScheme.primaryContainer
                              : colorScheme.secondaryContainer,
                          labelStyle: TextStyle(
                            color: isSelected
                                ? (level == 0
                                    ? colorScheme.onPrimaryContainer
                                    : colorScheme.onSecondaryContainer)
                                : colorScheme.onSurface,
                          ),
                        );
                      }).toList(),
                    ),
                  ),
                ],
              ],
            ),
          ),
          const Divider(height: 1),
          Expanded(
            child: dictList.isEmpty
                ? Center(
                    child: Text(
                      selectedPath.value.isEmpty
                          ? '请选择分类'
                          : '该分类下暂无词库',
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                            color: colorScheme.onSurface.withAlpha(150),
                          ),
                    ),
                  )
                : ListView.builder(
                    itemCount: dictList.length,
                    itemBuilder: (context, index) {
                      final dict = dictList[index];
                      final dictName = dict['name'] as String;
                      final count = dict['count'] as String?;
                      final example = dict['example'] as String?;

                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                        child: ListTile(
                          contentPadding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(16),
                          ),
                          tileColor: colorScheme.primaryContainer.withAlpha(100),
                          title: Text(
                            dictName,
                            style: TextStyle(
                              color: colorScheme.onSurface,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              if (count != null) ...[
                                const SizedBox(height: 4),
                                Text(
                                  '词条数：$count',
                                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                        color: colorScheme.onSurface.withAlpha(180),
                                      ),
                                ),
                              ],
                              if (example != null) ...[
                                const SizedBox(height: 4),
                                Text(
                                  example,
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                        color: colorScheme.onSurface.withAlpha(150),
                                      ),
                                ),
                              ],
                            ],
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
                            final downloadUrl = dict['download'] as String?;
                            if (downloadUrl == null) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('下载链接不可用'),
                                  behavior: SnackBarBehavior.floating,
                                ),
                              );
                              return;
                            }

                            final fileName = '${dictName.replaceAll(RegExp(r'[^\w一-龥]'), '_')}.scel';

                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => DictionaryDownloadPage(
                                  fileName: fileName,
                                  dictionaryName: dictName,
                                  format: ImeFormat.sougouScel,
                                  downloadUrl: downloadUrl,
                                  category: 'sogou',
                                ),
                              ),
                            );
                          },
                        ),
                      );
                    },
                  ),
          ),
        ],
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

            if (!fileName.endsWith('.scel')) {
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text("请选择 .scel 格式的文件"),
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
                      final parser = ParserFactory.createParserByFormat(ImeFormat.sougouScel);
                      var parseFile =await parser.parseFile(filePath);
                      return parseFile.entries;
                    },
                    dictionaryName: fileName.replaceAll('.scel', ''),
                    category: 'sogou',
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
