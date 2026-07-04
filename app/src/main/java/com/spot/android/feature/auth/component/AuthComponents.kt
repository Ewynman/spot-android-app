package com.spot.android.feature.auth.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.spot.android.core.design.Dimensions
import com.spot.android.core.design.theme.SpotColors

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        enabled = enabled,
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction?.invoke() },
            onNext = { onImeAction?.invoke() },
            onGo = { onImeAction?.invoke() },
        ),
        shape = RoundedCornerShape(Dimensions.Radius.medium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SpotColors.Primary,
            unfocusedBorderColor = SpotColors.WelcomeLine,
            focusedLabelColor = SpotColors.WelcomeMutedText,
            unfocusedLabelColor = SpotColors.WelcomeMutedText,
            cursorColor = SpotColors.Primary,
            focusedTextColor = SpotColors.Primary,
            unfocusedTextColor = SpotColors.Primary,
        ),
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    enabled: Boolean = true,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(Dimensions.Radius.medium),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = SpotColors.Primary,
            contentColor = SpotColors.ButtonText,
            disabledContainerColor = SpotColors.WelcomeLine,
            disabledContentColor = SpotColors.WelcomeMutedText,
        ),
    ) {
        Text(text = text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
    enabled: Boolean = true,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(Dimensions.Radius.medium),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = SpotColors.Primary,
            disabledContentColor = SpotColors.WelcomeMutedText,
        ),
    ) {
        Text(text = text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AuthErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    testTag: String = "auth.errorBanner",
) {
    Text(
        text = message,
        color = SpotColors.Primary,
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .background(SpotColors.WelcomeChipFill, RoundedCornerShape(Dimensions.Radius.small))
            .padding(Dimensions.Spacing.medium)
            .testTag(testTag),
    )
}

fun openLegalUrl(context: android.content.Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

const val TERMS_URL = "https://spotapp.online/terms"
const val PRIVACY_URL = "https://spotapp.online/privacy"
