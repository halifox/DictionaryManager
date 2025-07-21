package com.github.dictionary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.dictionary.model.Dict
import com.github.dictionary.repository.DictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstallScreen(data: Install) {
    val viewModel = hiltViewModel<InstallViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.init(data)
    }

    if (uiState is UiState.Error) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("${(uiState as UiState.Error).message}")
        }
        return
    } else if (uiState == UiState.Loading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            LoadingIndicator()
        }
        return
    }
    val dict = (uiState as UiState.Success).data


    Scaffold(
        topBar = {
            TopAppBar({
                Text(dict.name.orEmpty())
            })
        }
    ) {
        Column(Modifier.padding(it)) {
            Text("${data.id}")
        }
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}


@HiltViewModel
class InstallViewModel @Inject constructor(private val repo: DictRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Dict>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()
    fun init(data: Install) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dict = repo.getDict(data.id)
                _uiState.value = UiState.Success(dict)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
