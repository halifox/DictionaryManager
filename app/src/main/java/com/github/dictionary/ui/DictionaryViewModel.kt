package com.github.dictionary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.github.dictionary.repository.DictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(private val repo: DictRepository) : ViewModel() {
    fun getSearchPager(source: String, key: String) = Pager(
        PagingConfig(pageSize = 20)
    ) {
        repo.search(source, key, getMaxTiers(source))
    }.flow.cachedIn(viewModelScope)

    fun getSubTreeQuery(pid: String?, source: String) = Pager(
        PagingConfig(pageSize = 20)
    ) {
        repo.getSubTreeQuery(pid ?: "0", source, getMaxTiers(source))
    }.flow.cachedIn(viewModelScope)

    fun getCategories(source: String, pid: String?, tiers: Int) = callbackFlow {
        if (tiers < getMaxTiers(source)) {
            send(repo.getCategories(source, pid ?: "0", tiers))
        }
        awaitClose {}
    }

    fun getMaxTiers(source: String): Int {
        return when (source) {
            "sougo" -> 4
            "baidu" -> 3
            "qq"    -> 3
            else    -> 0
        }
    }

}