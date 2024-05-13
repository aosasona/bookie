package com.trulyao.bookie.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


val ProgressSize = 24.dp

@Composable
fun LoadingButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: () -> Unit,
    horizontalArrangement: Arrangement.Horizontal? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    var mod = modifier
    if (horizontalArrangement != null) {
        mod = mod.fillMaxWidth()
    }

    val isEnabled by remember {
        derivedStateOf {
            enabled && isLoading.not()
        }
    }

    Row(
        horizontalArrangement = horizontalArrangement ?: Arrangement.Start,
        verticalAlignment = verticalAlignment,
        modifier = mod
    ) {
        Button(onClick = onClick, enabled = isEnabled) {
            if (isLoading.not()) {
                content()
            } else {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(ProgressSize)
                        .height(ProgressSize)
                )
            }
        }
    }
}