// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dictionary_installer_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(DictionaryInstaller)
final dictionaryInstallerProvider = DictionaryInstallerProvider._();

final class DictionaryInstallerProvider
    extends $NotifierProvider<DictionaryInstaller, InstallState> {
  DictionaryInstallerProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'dictionaryInstallerProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$dictionaryInstallerHash();

  @$internal
  @override
  DictionaryInstaller create() => DictionaryInstaller();

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(InstallState value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<InstallState>(value),
    );
  }
}

String _$dictionaryInstallerHash() =>
    r'5c86b7412b4fe5b7ddac9fd508ef8f10dab0a1c6';

abstract class _$DictionaryInstaller extends $Notifier<InstallState> {
  InstallState build();
  @$mustCallSuper
  @override
  void runBuild() {
    final ref = this.ref as $Ref<InstallState, InstallState>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<InstallState, InstallState>,
              InstallState,
              Object?,
              Object?
            >;
    element.handleCreate(ref, build);
  }
}
