// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_dictionary_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(loadUserDictionary)
final loadUserDictionaryProvider = LoadUserDictionaryProvider._();

final class LoadUserDictionaryProvider
    extends
        $FunctionalProvider<
          AsyncValue<List<TableEntryData>>,
          List<TableEntryData>,
          FutureOr<List<TableEntryData>>
        >
    with
        $FutureModifier<List<TableEntryData>>,
        $FutureProvider<List<TableEntryData>> {
  LoadUserDictionaryProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'loadUserDictionaryProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$loadUserDictionaryHash();

  @$internal
  @override
  $FutureProviderElement<List<TableEntryData>> $createElement(
    $ProviderPointer pointer,
  ) => $FutureProviderElement(pointer);

  @override
  FutureOr<List<TableEntryData>> create(Ref ref) {
    return loadUserDictionary(ref);
  }
}

String _$loadUserDictionaryHash() =>
    r'7be95c837cd68e1a0eb98e16a75495d8cb3a3fb5';
