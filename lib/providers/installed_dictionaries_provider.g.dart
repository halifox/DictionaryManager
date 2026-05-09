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
    r'560253dab8a941645b2b4a0a8410eae493553ed3';

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
    required (String, String, String) super.argument,
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
        '$argument';
  }

  @$internal
  @override
  $FutureProviderElement<bool> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<bool> create(Ref ref) {
    final argument = this.argument as (String, String, String);
    return isDictionaryInstalled(ref, argument.$1, argument.$2, argument.$3);
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
    r'5cd951b8a31c5e9b584ca2a974da744aed5e233f';

final class IsDictionaryInstalledFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<bool>, (String, String, String)> {
  IsDictionaryInstalledFamily._()
    : super(
        retry: null,
        name: r'isDictionaryInstalledProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  IsDictionaryInstalledProvider call(
    String name,
    String source,
    String category,
  ) => IsDictionaryInstalledProvider._(
    argument: (name, source, category),
    from: this,
  );

  @override
  String toString() => r'isDictionaryInstalledProvider';
}
