package com.trulyao.bookie.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun TextInput(
    title: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
    placeholderText: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        TextField(
            value = value,
            onValueChange = onChange,
            placeholder = { if (placeholder != null) placeholder() else Text(placeholderText) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = visualTransformation
        )
    }
}