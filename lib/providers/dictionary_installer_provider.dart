import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:flutter/services.dart';
import 'table_provider.dart';

part 'dictionary_installer_provider.g.dart';

class InstallState {
  final int totalCount;
  final int installedCount;
  final int currentBatch;
  final bool isCompleted;
  final String? error;

  const InstallState({
    this.totalCount = 0,
    this.installedCount = 0,
    this.currentBatch = 0,
    this.isCompleted = false,
    this.error,
  });

  double get progress {
    if (totalCount == 0) return 0.0;
    return installedCount / totalCount;
  }

  String get elapsedTime {
    return '${(installedCount / 1000).toStringAsFixed(1)}s';
  }

  InstallState copyWith({
    int? totalCount,
    int? installedCount,
    int? currentBatch,
    bool? isCompleted,
    String? error,
  }) {
    return InstallState(
      totalCount: totalCount ?? this.totalCount,
      installedCount: installedCount ?? this.installedCount,
      currentBatch: currentBatch ?? this.currentBatch,
      isCompleted: isCompleted ?? this.isCompleted,
      error: error ?? this.error,
    );
  }
}

@riverpod
class DictionaryInstaller extends _$DictionaryInstaller {
  static const platform = MethodChannel('com.halifox.puredict/dictionary');

  @override
  InstallState build() {
    return const InstallState();
  }

  Future<void> startInstallation() async {
    final tableState = ref.read(tableDataProvider);
    final entries = tableState.entries;

    if (entries.isEmpty) {
      state = state.copyWith(error: '没有可安装的词条');
      return;
    }

    state = state.copyWith(totalCount: entries.length);

    try {
      const batchSize = 500;

      for (int i = 0; i < entries.length; i += batchSize) {
        final end = (i + batchSize < entries.length) ? i + batchSize : entries.length;
        final batch = entries.sublist(i, end);

        final words = batch.map((entry) => entry.toMap()).toList();

        await platform.invokeMethod('insertBatch', {
          'words': words,
        });

        state = state.copyWith(
          installedCount: end,
          currentBatch: (i ~/ batchSize) + 1,
        );

        await Future.delayed(const Duration(milliseconds: 10));
      }

      state = state.copyWith(isCompleted: true);
    } catch (e) {
      state = state.copyWith(error: '安装失败: $e');
    }
  }
}
