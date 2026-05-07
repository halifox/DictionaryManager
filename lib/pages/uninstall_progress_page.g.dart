// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'uninstall_progress_page.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(uninstallDictionary)
final uninstallDictionaryProvider = UninstallDictionaryFamily._();

final class UninstallDictionaryProvider
    extends $FunctionalProvider<AsyncValue<void>, void, FutureOr<void>>
    with $FutureModifier<void>, $FutureProvider<void> {
  UninstallDictionaryProvider._({
    required UninstallDictionaryFamily super.from,
    required InstalledDictionary super.argument,
  }) : super(
         retry: null,
         name: r'uninstallDictionaryProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$uninstallDictionaryHash();

  @override
  String toString() {
    return r'uninstallDictionaryProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<void> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<void> create(Ref ref) {
    final argument = this.argument as InstalledDictionary;
    return uninstallDictionary(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is UninstallDictionaryProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$uninstallDictionaryHash() =>
    r'f32873193a6ac78d9a6e8a1b676960496f149ed4';

final class UninstallDictionaryFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<void>, InstalledDictionary> {
  UninstallDictionaryFamily._()
    : super(
        retry: null,
        name: r'uninstallDictionaryProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  UninstallDictionaryProvider call(InstalledDictionary dictionary) =>
      UninstallDictionaryProvider._(argument: dictionary, from: this);

  @override
  String toString() => r'uninstallDictionaryProvider';
}
