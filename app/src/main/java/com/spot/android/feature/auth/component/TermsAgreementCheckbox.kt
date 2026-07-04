package com.spot.android.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.spot.android.core.design.theme.SpotColors

@Composable
fun TermsAgreementCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkboxTestTag: String = "auth.termsCheckbox",
) {
    val context = LocalContext.current
    val annotated = buildAnnotatedString {
        append("I agree to the ")
        pushStringAnnotation(tag = "terms", annotation = TERMS_URL)
        withStyle(SpanStyle(color = SpotColors.WelcomeGlow, textDecoration = TextDecoration.Underline)) {
            append("Terms")
        }
        pop()
        append(" & ")
        pushStringAnnotation(tag = "privacy", annotation = PRIVACY_URL)
        withStyle(SpanStyle(color = SpotColors.WelcomeGlow, textDecoration = TextDecoration.Underline)) {
            append("Privacy Policy")
        }
        pop()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(checkboxTestTag),
            colors = CheckboxDefaults.colors(
                checkedColor = SpotColors.Primary,
                uncheckedColor = SpotColors.WelcomeLine,
                checkmarkColor = SpotColors.ButtonText,
            ),
        )
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.bodySmall.copy(color = SpotColors.WelcomeMutedText),
            onClick = { offset ->
                annotated.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let {
                    openLegalUrl(context, it.item)
                }
            },
            modifier = Modifier.testTag("auth.termsLabel"),
        )
    }
}
