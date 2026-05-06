import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:file_picker/file_picker.dart';
import '../models/table_entry.dart';
import '../services/table_parser.dart';

part 'table_provider.g.dart';

class TableState {
  final List<TableEntry> entries;
  final String? fileName;
  final bool isLoading;
  final String? error;

  const TableState({
    this.entries = const [],
    this.fileName,
    this.isLoading = false,
    this.error,
  });

  TableState copyWith({
    List<TableEntry>? entries,
    String? fileName,
    bool? isLoading,
    String? error,
  }) {
    return TableState(
      entries: entries ?? this.entries,
      fileName: fileName ?? this.fileName,
      isLoading: isLoading ?? this.isLoading,
      error: error ?? this.error,
    );
  }
}

@riverpod
class TableData extends _$TableData {
  @override
  TableState build() {
    return const TableState();
  }

  Future<void> pickAndParseFile() async {
    state = state.copyWith(isLoading: true, error: null);

    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.any,
      );

      if (result != null && result.files.single.path != null) {
        final filePath = result.files.single.path!;
        final fileName = result.files.single.name;

        if (!fileName.endsWith('.table')) {
          state = state.copyWith(
            isLoading: false,
            error: '请选择 .table 格式的文件',
          );
          return;
        }

        final entries = await TableParser.parseFile(filePath);

        state = TableState(
          entries: entries,
          fileName: fileName,
          isLoading: false,
        );
      } else {
        state = const TableState();
      }
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e.toString(),
      );
    }
  }

  void clear() {
    state = const TableState();
  }
}
