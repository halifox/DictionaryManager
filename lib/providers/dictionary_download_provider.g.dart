// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dictionary_download_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(DictionaryDownloader)
final dictionaryDownloaderProvider = DictionaryDownloaderFamily._();

final class DictionaryDownloaderProvider
    extends $StreamNotifierProvider<DictionaryDownloader, DownloadProgress> {
  DictionaryDownloaderProvider._({
    required DictionaryDownloaderFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'dictionaryDownloaderProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$dictionaryDownloaderHash();

  @override
  String toString() {
    return r'dictionaryDownloaderProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  DictionaryDownloader create() => DictionaryDownloader();

  @override
  bool operator ==(Object other) {
    return other is DictionaryDownloaderProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$dictionaryDownloaderHash() =>
    r'43cac216098489d325f0bf5518fe5eb8bd2803b0';

final class DictionaryDownloaderFamily extends $Family
    with
        $ClassFamilyOverride<
          DictionaryDownloader,
          AsyncValue<DownloadProgress>,
          DownloadProgress,
          Stream<DownloadProgress>,
          String
        > {
  DictionaryDownloaderFamily._()
    : super(
        retry: null,
        name: r'dictionaryDownloaderProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  DictionaryDownloaderProvider call(String fileName) =>
      DictionaryDownloaderProvider._(argument: fileName, from: this);

  @override
  String toString() => r'dictionaryDownloaderProvider';
}

abstract class _$DictionaryDownloader
    extends $StreamNotifier<DownloadProgress> {
  late final _$args = ref.$arg as String;
  String get fileName => _$args;

  Stream<DownloadProgress> build(String fileName);
  @$mustCallSuper
  @override
  void runBuild() {
    final ref =
        this.ref as $Ref<AsyncValue<DownloadProgress>, DownloadProgress>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<AsyncValue<DownloadProgress>, DownloadProgress>,
              AsyncValue<DownloadProgress>,
              Object?,
              Object?
            >;
    element.handleCreate(ref, () => build(_$args));
  }
}

@ProviderFor(getDictionaryPath)
final getDictionaryPathProvider = GetDictionaryPathFamily._();

final class GetDictionaryPathProvider
    extends $FunctionalProvider<AsyncValue<String>, String, FutureOr<String>>
    with $FutureModifier<String>, $FutureProvider<String> {
  GetDictionaryPathProvider._({
    required GetDictionaryPathFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'getDictionaryPathProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$getDictionaryPathHash();

  @override
  String toString() {
    return r'getDictionaryPathProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<String> $createElement($ProviderPointer pointer) =>
      $FutureProviderElement(pointer);

  @override
  FutureOr<String> create(Ref ref) {
    final argument = this.argument as String;
    return getDictionaryPath(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is GetDictionaryPathProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$getDictionaryPathHash() => r'792b0b44345a41eb00e09a015dc9d1dd66dbfee4';

final class GetDictionaryPathFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<String>, String> {
  GetDictionaryPathFamily._()
    : super(
        retry: null,
        name: r'getDictionaryPathProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  GetDictionaryPathProvider call(String fileName) =>
      GetDictionaryPathProvider._(argument: fileName, from: this);

  @override
  String toString() => r'getDictionaryPathProvider';
}

@ProviderFor(isDictionaryDownloaded)
final isDictionaryDownloadedProvider = IsDictionaryDownloadedFamily._();

final class IsDictionaryDownloadedProvider
    extends $FunctionalProvider<AsyncValue<bool>, bool, FutureOr<bool>>
    with $FutureModifier<bool>, $FutureProvider<bool> {
  IsDictionaryDownloadedProvider._({
    required IsDictionaryDownloadedFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'isDictionaryDownloadedProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$isDictionaryDownloadedHash();

  @override
  String toString() {
    return r'isDictionaryDownloadedProvider'
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
    return isDictionaryDownloaded(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is IsDictionaryDownloadedProvider &&
        other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$isDictionaryDownloadedHash() =>
    r'7dabb5be2033865efddc5531e41c8092453255e3';

final class IsDictionaryDownloadedFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<bool>, String> {
  IsDictionaryDownloadedFamily._()
    : super(
        retry: null,
        name: r'isDictionaryDownloadedProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  IsDictionaryDownloadedProvider call(String fileName) =>
      IsDictionaryDownloadedProvider._(argument: fileName, from: this);

  @override
  String toString() => r'isDictionaryDownloadedProvider';
}
