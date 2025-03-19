package com.github.dictionary2.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.dictionary.R
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val navHostController = LocalNavController.current
    val inputMethodManager: InputMethodManager = koinInject()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it),
        ) {


            ButtonPreference(
                title = stringResource(id = R.string.tq_notification_permission),
                description = stringResource(id = R.string.tq_notification_permission_statement),
                buttonText = stringResource(id = R.string.request_permission),
                onClick = { /* Handle notification permission request */ }
            )

            Preference(
                title = stringResource(id = R.string.td_user_dictionary),
                description = stringResource(id = R.string.td_user_dictionary_statement)
            )

            Preference(
                title = stringResource(id = R.string.td_gboard),
                description = stringResource(id = R.string.btn_gboard_statement)
            )

            Preference(
                title = stringResource(id = R.string.td_sogou),
                description = stringResource(id = R.string.td_statement),
                onClick = {
                    navHostController.navigate("/dictionary_index_screen")
                }
            )

            Preference(
                title = stringResource(id = R.string.td_xunfei),
                description = stringResource(id = R.string.td_statement)
            )

            Preference(
                title = stringResource(id = R.string.td_baidu),
                description = stringResource(id = R.string.td_statement)
            )
        }

    }
}


@Preview(device = "spec:parent=pixel_5")
@Composable
fun ButtonPreference(
    title: String = "title",
    description: String = "description",
    buttonText: String = "click",
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedButton(onClick = onClick) {
            Text(text = buttonText)
        }
    }
}

@Preview(device = "spec:parent=pixel_5")
@Composable
fun Preference(
    title: String = "title",
    description: String = "description",
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)

        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
