import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../services/user_dictionary_service.dart';

part 'user_dictionary_provider.g.dart';

class UserWord {
  final String word;
  final String? shortcut;
  final int frequency;
  final String? locale;

  UserWord({
    required this.word,
    this.shortcut,
    required this.frequency,
    this.locale,
  });
}

class UserDictionaryState {
  final List<UserWord> words;
  final bool isLoading;
  final String? error;

  UserDictionaryState({
    this.words = const [],
    this.isLoading = false,
    this.error,
  });

  UserDictionaryState copyWith({
    List<UserWord>? words,
    bool? isLoading,
    String? error,
  }) {
    return UserDictionaryState(
      words: words ?? this.words,
      isLoading: isLoading ?? this.isLoading,
      error: error ?? this.error,
    );
  }
}

@riverpod
class UserDictionary extends _$UserDictionary {
  final _service = UserDictionaryService();

  @override
  UserDictionaryState build() {
    return UserDictionaryState();
  }

  Future<void> loadUserDictionary() async {
    state = state.copyWith(isLoading: true, error: null);

    try {
      final result = await _service.queryUserDictionary();
      final words = result.map((item) => UserWord(
        word: item['word'] as String,
        frequency: item['frequency'] as int,
        locale: item['locale'] as String?,
        shortcut: item['shortcut'] as String?,
      )).toList();

      state = state.copyWith(words: words, isLoading: false);
    } catch (e) {
      state = state.copyWith(error: e.toString(), isLoading: false);
    }
  }
}
