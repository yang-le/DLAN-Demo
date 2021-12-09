package me.yangle.dlnademo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun DlnaList(viewModel: DlnaViewModel) {
    var showDetail by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf("") }

    LazyColumn {
        items(viewModel.devices) {
            ListItem(Modifier.clickable {
                showDetail = true
                detail = it.detailsMessage
            }) {
                Text(it.toString())
            }
        }
    }

    AnimatedVisibility(showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            confirmButton = { },
            text = { Text(detail) }
        )
    }
}