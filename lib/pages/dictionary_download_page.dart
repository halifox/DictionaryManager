import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import '../providers/dictionary_download_provider.dart';
import '../services/table_parser.dart';
import 'dictionary_preview_page.dart';

class DictionaryDownloadPage extends HookConsumerWidget {
  const DictionaryDownloadPage({
    super.key,
    required this.fileName,
    required this.dictionaryName,
  });

  final String fileName;
  final String dictionaryName;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final downloadState = ref.watch(dictionaryDownloaderProvider(fileName));

    return Scaffold(
      appBar: AppBar(
        title: Text('下载词库 - $dictionaryName'),
      ),
      body: downloadState.when(
        data: (progress) {
          if (progress.progress >= 1.0) {
            // 下载完成，解析并跳转
            WidgetsBinding.instance.addPostFrameCallback((_) async {
              // 刷新下载状态
              ref.invalidate(isDictionaryDownloadedProvider(fileName));

              if (context.mounted) {
                Navigator.pushReplacement(
                  context,
                  MaterialPageRoute(
                    builder: (context) => DictionaryPreviewPage(
                      loadData: () async {
                        final path = await ref.read(getDictionaryPathProvider(fileName).future);
                        return TableParser.parseFile(path);
                      },
                      dictionaryName: dictionaryName,
                      category: 'pyim',
                      source: 'online',
                    ),
                  ),
                );
              }
            });

            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.check_circle_outline,
                    size: 64,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  const SizedBox(height: 24),
                  Text(
                    '下载完成',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 16),
                  const Text('正在解析词库...'),
                ],
              ),
            );
          }

          final downloadedMB = (progress.downloaded / 1024 / 1024).toStringAsFixed(2);
          final totalMB = (progress.total / 1024 / 1024).toStringAsFixed(2);

          return Center(
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircularProgressIndicator(
                    value: progress.progress,
                    strokeWidth: 6,
                  ),
                  const SizedBox(height: 32),
                  Text(
                    '${(progress.progress * 100).toStringAsFixed(1)}%',
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    '$downloadedMB MB / $totalMB MB',
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    '正在下载 $dictionaryName 词库...',
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          );
        },
        error: (error, stackTrace) {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.error_outline,
                    size: 64,
                    color: Theme.of(context).colorScheme.error,
                  ),
                  const SizedBox(height: 24),
                  Text(
                    '下载失败',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    error.toString(),
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 32),
                  FilledButton.icon(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.arrow_back),
                    label: const Text('返回'),
                  ),
                ],
              ),
            ),
          );
        },
        loading: () {
          return const Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                CircularProgressIndicator(),
                SizedBox(height: 24),
                Text('准备下载...'),
              ],
            ),
          );
        },
      ),
    );
  }
}
