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
          AsyncValue<List<TableEntry>>,
          List<TableEntry>,
          FutureOr<List<TableEntry>>
        >
    with $FutureModifier<List<TableEntry>>, $FutureProvider<List<TableEntry>> {
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
  $FutureProviderElement<List<TableEntry>> $createElement(
    $ProviderPointer pointer,
  ) => $FutureProviderElement(pointer);

  @override
  FutureOr<List<TableEntry>> create(Ref ref) {
    return loadUserDictionary(ref);
  }
}

String _$loadUserDictionaryHash() =>
    r'a40fa3f4c25676828e9e1c6a288457f8640557bc';
