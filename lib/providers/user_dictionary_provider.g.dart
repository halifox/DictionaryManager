// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_dictionary_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(UserDictionary)
final userDictionaryProvider = UserDictionaryProvider._();

final class UserDictionaryProvider
    extends $NotifierProvider<UserDictionary, UserDictionaryState> {
  UserDictionaryProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'userDictionaryProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$userDictionaryHash();

  @$internal
  @override
  UserDictionary create() => UserDictionary();

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(UserDictionaryState value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<UserDictionaryState>(value),
    );
  }
}

String _$userDictionaryHash() => r'9e5f76a2ff11e5a81022513076750f0313d66044';

abstract class _$UserDictionary extends $Notifier<UserDictionaryState> {
  UserDictionaryState build();
  @$mustCallSuper
  @override
  void runBuild() {
    final ref = this.ref as $Ref<UserDictionaryState, UserDictionaryState>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<UserDictionaryState, UserDictionaryState>,
              UserDictionaryState,
              Object?,
              Object?
            >;
    element.handleCreate(ref, build);
  }
}
