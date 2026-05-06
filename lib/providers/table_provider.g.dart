// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'table_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(TableData)
final tableDataProvider = TableDataProvider._();

final class TableDataProvider extends $NotifierProvider<TableData, TableState> {
  TableDataProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'tableDataProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$tableDataHash();

  @$internal
  @override
  TableData create() => TableData();

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(TableState value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<TableState>(value),
    );
  }
}

String _$tableDataHash() => r'b6303b89377295cf51299a5d9d5e6ae9bbb9b75c';

abstract class _$TableData extends $Notifier<TableState> {
  TableState build();
  @$mustCallSuper
  @override
  void runBuild() {
    final ref = this.ref as $Ref<TableState, TableState>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<TableState, TableState>,
              TableState,
              Object?,
              Object?
            >;
    element.handleCreate(ref, build);
  }
}
