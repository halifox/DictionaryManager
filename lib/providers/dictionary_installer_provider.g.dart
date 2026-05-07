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
    extends $FunctionalProvider<AsyncValue<void>, void, FutureOr<void>>
    with $FutureModifier<void>, $FutureProvider<void> {
  StartInstallationProvider._({
    required StartInstallationFamily super.from,
    required List<TableEntry> super.argument,
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
  $FutureProviderElement<void> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<void> create(Ref ref) {
    final argument = this.argument as List<TableEntry>;
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

String _$startInstallationHash() => r'f6181ab12ae046f148fc474fc478a2ae680f5ba2';

final class StartInstallationFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<void>, List<TableEntry>> {
  StartInstallationFamily._()
    : super(
        retry: null,
        name: r'startInstallationProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  StartInstallationProvider call(List<TableEntry> words) =>
      StartInstallationProvider._(argument: words, from: this);

  @override
  String toString() => r'startInstallationProvider';
}
