import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:path/path.dart' as path;
import 'package:url_launcher/url_launcher.dart';

import '../models/ime_format.dart';
import '../parsers/parser_factory.dart';
import 'dictionary_preview_page.dart';

class FileImportPage extends HookConsumerWidget {
  final ImeFormatInfo formatInfo;

  const FileImportPage({required this.formatInfo, super.key});

  Future<void> _openWiki(BuildContext context) async {
    final wikiUrl = formatInfo.wikiUrl;
    if (wikiUrl != null) {
      final uri = Uri.parse(wikiUrl);
      try {
        await launchUrl(uri, mode: LaunchMode.externalApplication);
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('无法打开链接: $e'),
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
      }
    }
  }

  Future<void> _pickAndParseFile(
    BuildContext context,
    ValueNotifier<bool> isLoading,
    ValueNotifier<String> statusMessage,
  ) async {
    final result = await FilePicker.platform.pickFiles(type: FileType.any);

    if (result != null && result.files.single.path != null) {
      final filePath = result.files.single.path!;

      final fileExt = path.extension(filePath).toLowerCase();
      final allowedExts = formatInfo.extensions
          .map((e) => e.toLowerCase())
          .toList();

      if (!allowedExts.contains(fileExt)) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                '不支持的文件格式。期望: ${allowedExts.join(", ")}，实际: $fileExt',
              ),
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
        return;
      }

      isLoading.value = true;
      statusMessage.value = '正在解析 ${path.basename(filePath)}...';

      try {
        ImeFormat targetFormat = formatInfo.format;

        if (formatInfo.format == ImeFormat.qqText && fileExt == '.qpyd') {
          targetFormat = ImeFormat.qqQpyd;
        } else if (formatInfo.format == ImeFormat.ziguangText &&
            fileExt == '.uwl') {
          targetFormat = ImeFormat.ziguangUwl;
        } else if (formatInfo.format == ImeFormat.baiduText &&
            fileExt == '.bdict') {
          targetFormat = ImeFormat.baiduBdict;
        } else if (formatInfo.format == ImeFormat.sougouText &&
            fileExt == '.scel') {
          targetFormat = ImeFormat.sougouScel;
        } else if (formatInfo.format == ImeFormat.baiduPhonePinyin &&
            fileExt == '.bcd') {
          targetFormat = ImeFormat.baiduBcd;
        }

        final parser = ParserFactory.createParserByFormat(targetFormat);
        final parseResult = await parser.parseFile(filePath);

        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => DictionaryPreviewPage(
                loadData: () async => parseResult.entries,
                dictionaryName: path.basenameWithoutExtension(filePath),
                category: formatInfo.format.name,
                source: 'local',
              ),
            ),
          );
        }
      } catch (e) {
        print('解析文件失败: $e');
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('解析失败: $e'),
              behavior: SnackBarBehavior.floating,
            ),
          );
        }
      } finally {
        if (context.mounted) {
          isLoading.value = false;
          statusMessage.value = '';
        }
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isLoading = useState(false);
    final statusMessage = useState('');

    return Scaffold(
      appBar: AppBar(
        title: Text(formatInfo.displayName),
        actions: [
          if (formatInfo.wikiUrl != null)
            IconButton(
              icon: const Icon(Icons.help_outline),
              onPressed: () => _openWiki(context),
              tooltip: '查看格式说明',
            ),
        ],
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                formatInfo.icon,
                size: 80,
                color: Theme.of(context).colorScheme.primary,
              ),
              const SizedBox(height: 24),
              Text(
                formatInfo.displayName,
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 16),
              Text(
                '支持的格式: ${formatInfo.extensions.join(", ")}',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 48),
              if (isLoading.value) ...[
                const CircularProgressIndicator(),
                const SizedBox(height: 16),
                Text(statusMessage.value),
              ],
            ],
          ),
        ),
      ),
      floatingActionButton: isLoading.value
          ? null
          : FloatingActionButton.extended(
              onPressed: () => _pickAndParseFile(
                context,
                isLoading,
                statusMessage,
              ),
              icon: const Icon(Icons.folder_open),
              label: const Text('本地导入'),
            ),
    );
  }
}
