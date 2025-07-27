package com.github.dictionary.ui

import com.github.dictionary.model.Dict
import com.github.dictionary.model.LocalRecord

sealed class DictionaryDetailState {
    object Loading : DictionaryDetailState()
    data class Error(val exception: Exception) : DictionaryDetailState()
    data class Installed(val dict: Dict, val localRecord: LocalRecord?) : DictionaryDetailState()
}
