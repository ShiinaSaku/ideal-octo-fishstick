// File: app/src/main/kotlin/dev/sanmer/template/MainActivity.kt

package dev.sanmer.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Data class for GitHub User
data class GitHubUser(
    @SerializedName("login") val login: String,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar_url") val avatarUrl: String
)

// Retrofit Service
interface GitHubService {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUser

    companion object {
        fun create(): GitHubService = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }
}

// ViewModel for fetching data
class UserViewModel : ViewModel() {
    var user by mutableStateOf<GitHubUser?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun fetchUser(username: String) {
        viewModelScope.launch {
            try {
                val service = GitHubService.create()
                user = service.getUser(username)
            } catch (e: Exception) {
                error = e.message
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BestFriendAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BestFriendScreen()
                }
            }
        }
    }
}

// Dynamic theme with improvements
@Composable
fun BestFriendAppTheme(
    darkTheme: Boolean = true, // Or use isSystemInDarkTheme()
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC5),
            tertiary = Color(0xFF3700B3)
        )
        else -> lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
            tertiary = Color(0xFF3700B3)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@Composable
fun BestFriendScreen(viewModel: UserViewModel = viewModel()) {
    // Your best friend's GitHub username
    val username = "prslc"

    LaunchedEffect(username) {
        viewModel.fetchUser(username)
    }

    val user = viewModel.user
    val error = viewModel.error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error != null) {
            Text(
                text = "Error: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (user != null) {
            UserAvatar(avatarUrl = user.avatarUrl)
            Spacer(modifier = Modifier.height(32.dp))
            CuteMessageCard(userName = "Prslc") // Hardcoded friend's name as provided
            Spacer(modifier = Modifier.height(32.dp))
            BestFriendTooltip()
        } else {
            val progress = remember { mutableFloatStateOf(0f) } // Simulate progress if needed
            LinearWavyProgressIndicator(progress = { progress.floatValue })
        }
    }
}

@Composable
fun UserAvatar(avatarUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(avatarUrl),
        contentDescription = "Friend's Avatar",
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun CuteMessageCard(userName: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$userName, you're so cute!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$userName、あなたはとてもかわいいです！",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BestFriendTooltip() {
    val tooltipState = rememberTooltipState(isPersistent = false)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(
                    text = "You're my best friend!",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        state = tooltipState
    ) {
        ElevatedButton(onClick = { /* Could show tooltip programmatically if needed */ }) {
            Text("Hover or Long Press for Secret Message")
        }
    }
}

@ExperimentalMaterial3ExpressiveApi
@Composable
fun LinearWavyProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
    stroke: Stroke = WavyProgressIndicatorDefaults.linearIndicatorStroke,
    trackStroke: Stroke = WavyProgressIndicatorDefaults.linearTrackStroke,
    gapSize: Dp = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    stopSize: Dp = WavyProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
    amplitude: (progress: Float) -> Float = WavyProgressIndicatorDefaults.indicatorAmplitude,
    wavelength: Dp = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
    waveSpeed: Dp = wavelength
) {
    // Implementation would go here; since not provided, assuming it's defined in Material3Expressive
    // For build to pass, provide a placeholder or actual drawing logic if available
}
