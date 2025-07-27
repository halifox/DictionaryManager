package com.github.dictionary.ui

sealed class DictionaryDetailState {
    object Idle : DictionaryDetailState()
    object Downloading : DictionaryDetailState()
    object Uninstalled : DictionaryDetailState()
    object Installed : DictionaryDetailState()
    object Installing : DictionaryDetailState()
    object UnInstalling : DictionaryDetailState()
    data class Error(val exception: Exception) : DictionaryDetailState()
}
