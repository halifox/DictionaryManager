import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../models/table_entry.dart';
import '../providers/dictionary_installer_provider.dart';
import '../view/state_view.dart';

class InstallProgressPage extends HookConsumerWidget {
  const InstallProgressPage(this.words, {super.key});

  final List<TableEntry> words;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final installState = ref.watch(startInstallationProvider(words));
    return Scaffold(
      appBar: AppBar(title: Text("安装词库")),
      body: installState.when(
        data: (data) {
          return StateView.finish(
            title: '安装完成',
            message: '词库已成功安装',
          );
        },
        error: (error, stackTrace) {
          return StateView.error(message: error.toString());
        },
        loading: () {
          return StateView.loading();
        },
      ),
    );
  }
}
