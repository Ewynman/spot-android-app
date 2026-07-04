package com.spot.android.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors
import com.spot.android.core.util.Constants

@Composable
fun OtpInputRow(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTagPrefix: String = "confirmEmail.otp",
) {
    val digits = remember(otp) {
        otp.padEnd(Constants.Auth.OTP_LENGTH, ' ').take(Constants.Auth.OTP_LENGTH).toList()
    }
    val focusRequesters = remember { List(Constants.Auth.OTP_LENGTH) { FocusRequester() } }
    var focusedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(focusedIndex) {
        if (focusedIndex in focusRequesters.indices) {
            focusRequesters[focusedIndex].requestFocus()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("$testTagPrefix.row"),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
    ) {
        repeat(Constants.Auth.OTP_LENGTH) { index ->
            val digitChar = digits[index]
            val displayValue = if (digitChar == ' ') "" else digitChar.toString()

            OutlinedTextField(
                value = displayValue,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    when {
                        filtered.length > 1 -> {
                            val pasted = filtered.take(Constants.Auth.OTP_LENGTH)
                            onOtpChange(pasted)
                            focusedIndex = (pasted.length).coerceAtMost(Constants.Auth.OTP_LENGTH - 1)
                        }
                        filtered.length == 1 -> {
                            val updated = buildString {
                                repeat(Constants.Auth.OTP_LENGTH) { i ->
                                    append(
                                        when {
                                            i < index -> if (digits[i] != ' ') digits[i] else ' '
                                            i == index -> filtered.first()
                                            else -> if (digits[i] != ' ') digits[i] else ' '
                                        },
                                    )
                                }
                            }.replace(" ", "")
                            onOtpChange(updated)
                            if (index < Constants.Auth.OTP_LENGTH - 1) {
                                focusedIndex = index + 1
                            }
                        }
                        filtered.isEmpty() && displayValue.isNotEmpty() -> {
                            val current = otp.toMutableList()
                            if (index < current.size) {
                                current.removeAt(index)
                            }
                            onOtpChange(current.joinToString(""))
                            if (index > 0) focusedIndex = index - 1
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequesters[index])
                    .testTag("$testTagPrefix.$index"),
                enabled = enabled,
                singleLine = true,
                textStyle = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpotColors.Primary,
                    unfocusedBorderColor = SpotColors.WelcomeLine,
                    cursorColor = SpotColors.Primary,
                    focusedTextColor = SpotColors.Primary,
                    unfocusedTextColor = SpotColors.Primary,
                ),
            )
        }
    }
}
