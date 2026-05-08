// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dictionary_installer_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(startInstallation)
final startInstallationProvider = StartInstallationFamily._();

final class StartInstallationProvider
    extends
        $FunctionalProvider<
          AsyncValue<List<int>>,
          List<int>,
          FutureOr<List<int>>
        >
    with $FutureModifier<List<int>>, $FutureProvider<List<int>> {
  StartInstallationProvider._({
    required StartInstallationFamily super.from,
    required List<TableEntryData> super.argument,
  }) : super(
         retry: null,
         name: r'startInstallationProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$startInstallationHash();

  @override
  String toString() {
    return r'startInstallationProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<List<int>> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<List<int>> create(Ref ref) {
    final argument = this.argument as List<TableEntryData>;
    return startInstallation(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is StartInstallationProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$startInstallationHash() => r'044db0e66d86b7c86c6d8bd3abdbbce14cb6c87d';

final class StartInstallationFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<List<int>>, List<TableEntryData>> {
  StartInstallationFamily._()
    : super(
        retry: null,
        name: r'startInstallationProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  StartInstallationProvider call(List<TableEntryData> words) =>
      StartInstallationProvider._(argument: words, from: this);

  @override
  String toString() => r'startInstallationProvider';
}
