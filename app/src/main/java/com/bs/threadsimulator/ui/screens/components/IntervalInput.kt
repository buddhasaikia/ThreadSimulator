package com.bs.threadsimulator.ui.screens.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow

/**
 * A reusable interval/number input field with support for validation feedback.
 *
 * @param label The label text displayed above the input field
 * @param value The current input value
 * @param onValueChange Callback invoked when the input value changes
 * @param modifier The modifier to apply to this composable
 * @param isError Whether to display the field in error state
 * @param errorMessage Optional error message to display below the field
 */
@Composable
fun IntervalInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        onValueChange = {
            onValueChange(it.trim())
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifier = modifier,
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}