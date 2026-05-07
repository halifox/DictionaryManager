// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'installed_dictionaries_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(InstalledDictionaries)
final installedDictionariesProvider = InstalledDictionariesProvider._();

final class InstalledDictionariesProvider
    extends
        $AsyncNotifierProvider<
          InstalledDictionaries,
          List<InstalledDictionary>
        > {
  InstalledDictionariesProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'installedDictionariesProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$installedDictionariesHash();

  @$internal
  @override
  InstalledDictionaries create() => InstalledDictionaries();
}

String _$installedDictionariesHash() =>
    r'f24b9f9ce20b3166a8a3236e2b61f14f0d5f33a5';

abstract class _$InstalledDictionaries
    extends $AsyncNotifier<List<InstalledDictionary>> {
  FutureOr<List<InstalledDictionary>> build();
  @$mustCallSuper
  @override
  void runBuild() {
    final ref =
        this.ref
            as $Ref<
              AsyncValue<List<InstalledDictionary>>,
              List<InstalledDictionary>
            >;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<
                AsyncValue<List<InstalledDictionary>>,
                List<InstalledDictionary>
              >,
              AsyncValue<List<InstalledDictionary>>,
              Object?,
              Object?
            >;
    element.handleCreate(ref, build);
  }
}

@ProviderFor(isDictionaryInstalled)
final isDictionaryInstalledProvider = IsDictionaryInstalledFamily._();

final class IsDictionaryInstalledProvider
    extends $FunctionalProvider<AsyncValue<bool>, bool, FutureOr<bool>>
    with $FutureModifier<bool>, $FutureProvider<bool> {
  IsDictionaryInstalledProvider._({
    required IsDictionaryInstalledFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'isDictionaryInstalledProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$isDictionaryInstalledHash();

  @override
  String toString() {
    return r'isDictionaryInstalledProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<bool> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<bool> create(Ref ref) {
    final argument = this.argument as String;
    return isDictionaryInstalled(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is IsDictionaryInstalledProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$isDictionaryInstalledHash() =>
    r'bf0ed2ca2c5b528ec14e2d62117f3454ab04a84e';

final class IsDictionaryInstalledFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<bool>, String> {
  IsDictionaryInstalledFamily._()
    : super(
        retry: null,
        name: r'isDictionaryInstalledProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  IsDictionaryInstalledProvider call(String name) =>
      IsDictionaryInstalledProvider._(argument: name, from: this);

  @override
  String toString() => r'isDictionaryInstalledProvider';
}
