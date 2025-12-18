package dev.sanmer.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.sanmer.template.navigation.AppNavGraph
import dev.sanmer.template.ui.theme.ShinnaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShinnaTheme {
                AppNavGraph()
            }
        }
    }
}
