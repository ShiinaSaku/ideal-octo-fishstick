package dev.sanmer.template.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ShinnaTheme(content: @Composable () -> Unit) {
    val scheme = dynamicLightColorScheme(LocalContext.current)

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        content = content
    )
}
