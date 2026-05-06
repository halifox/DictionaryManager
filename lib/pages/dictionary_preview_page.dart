import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:permission_handler/permission_handler.dart';
import '../providers/table_provider.dart';
import 'install_progress_page.dart';

class DictionaryPreviewPage extends HookConsumerWidget {
  const DictionaryPreviewPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tableState = ref.watch(tableDataProvider);
    final tableNotifier = ref.read(tableDataProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: const Text('词库预览'),
        centerTitle: true,
      ),
      body: tableState.entries.isEmpty
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.file_open_outlined,
                    size: 80,
                    color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.5),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    '请选择词库文件',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),
                  const SizedBox(height: 48),
                  FilledButton.icon(
                    onPressed: tableState.isLoading
                        ? null
                        : () async {
                            await tableNotifier.pickAndParseFile();
                            final state = ref.read(tableDataProvider);

                            if (context.mounted && state.error != null) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text('错误: ${state.error}'),
                                  behavior: SnackBarBehavior.floating,
                                ),
                              );
                            }
                          },
                    icon: tableState.isLoading
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.folder_open),
                    label: Text(tableState.isLoading ? '加载中...' : '选择文件'),
                  ),
                ],
              ),
            )
          : Column(
              children: [
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Card(
                    elevation: 0,
                    color: Theme.of(context).colorScheme.surfaceContainerHighest,
                    child: Padding(
                      padding: const EdgeInsets.all(20.0),
                      child: Row(
                        children: [
                          Icon(
                            Icons.description_outlined,
                            size: 40,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  tableState.fileName ?? '?',
                                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                        fontWeight: FontWeight.w600,
                                      ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  '共 ${tableState.entries.length} 条词条',
                                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                                      ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                Expanded(
                  child: ListView.builder(
                    itemCount: tableState.entries.length,
                    itemBuilder: (context, index) {
                      final entry = tableState.entries[index];
                      return ListTile(
                        leading: CircleAvatar(
                          backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                          foregroundColor: Theme.of(context).colorScheme.onPrimaryContainer,
                          child: Text('${index + 1}'),
                        ),
                        title: Text(entry.word),
                        subtitle: Text('快捷键: ${entry.shortcut ?? "无"}'),
                        trailing: Text(
                          '${entry.frequency}',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      );
                    },
                  ),
                ),
              ],
            ),
      floatingActionButton: tableState.entries.isNotEmpty
          ? FloatingActionButton.extended(
              onPressed: () async {
                final status = await Permission.contacts.status;
                if (!status.isGranted) {
                  if (context.mounted) {
                    _showPermissionDialog(context);
                  }
                  return;
                }

                if (context.mounted) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const InstallProgressPage(),
                    ),
                  );
                }
              },
              icon: const Icon(Icons.download),
              label: const Text('安装'),
            )
          : null,
    );
  }

  void _showPermissionDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        icon: Icon(
          Icons.lock_outline,
          size: 48,
          color: Theme.of(context).colorScheme.error,
        ),
        title: const Text('需要用户词典权限'),
        content: const Text('为了安装词库到系统用户词典，需要授予写入权限。'),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () async {
              Navigator.of(context).pop();
              final status = await Permission.contacts.request();
              if (status.isGranted) {
                if (context.mounted) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => const InstallProgressPage(),
                    ),
                  );
                }
              } else if (status.isPermanentlyDenied) {
                if (context.mounted) {
                  _showSettingsDialog(context);
                }
              }
            },
            child: const Text('授予权限'),
          ),
        ],
      ),
    );
  }

  void _showSettingsDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        icon: Icon(
          Icons.settings_outlined,
          size: 48,
          color: Theme.of(context).colorScheme.primary,
        ),
        title: const Text('需要在设置中授权'),
        content: const Text('您已拒绝该权限，请在系统设置中手动授予权限。'),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
            },
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () async {
              await openAppSettings();
              if (context.mounted) {
                Navigator.of(context).pop();
              }
            },
            child: const Text('去设置'),
          ),
        ],
      ),
    );
  }
}
