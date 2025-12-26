package dev.sanmer.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class GitHubUser(
    @SerializedName("login") val login: String,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar_url") val avatarUrl: String
)

interface GitHubService {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUser

    companion object {
        val instance: GitHubService by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubService::class.java)
        }
    }
}

sealed interface UserUiState {
    data object Loading : UserUiState
    data class Success(val user: GitHubUser) : UserUiState
    data class Error(val message: String) : UserUiState
}

class UserViewModel : ViewModel() {
    var uiState by mutableStateOf<UserUiState>(UserUiState.Loading)
        private set

    fun fetchUser(username: String) {
        viewModelScope.launch {
            uiState = UserUiState.Loading
            runCatching {
                GitHubService.instance.getUser(username)
            }.onSuccess {
                uiState = UserUiState.Success(it)
            }.onFailure {
                uiState = UserUiState.Error(it.message ?: "Connection failed")
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme(primary = Color(0xFFD0BCFF), secondary = Color(0xFFCCC2DC))
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@Composable
fun MainScreen(viewModel: UserViewModel = viewModel()) {
    val username = "prslc"

    LaunchedEffect(Unit) {
        viewModel.fetchUser(username)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = viewModel.uiState,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 0.9f) togetherWith fadeOut()
                },
                label = "state_transition"
            ) { state ->
                when (state) {
                    is UserUiState.Loading -> LoadingState()
                    is UserUiState.Error -> ErrorState(state.message) { viewModel.fetchUser(username) }
                    is UserUiState.Success -> UserProfileCard(state.user)
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(user: GitHubUser) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(6.dp)
        ) {
            AsyncImage(
                model = "https://github.com/${user.login}.png",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = user.name ?: user.login,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "@${user.login}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You're so cute!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "あなたはとてもかわいいです！",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        FilledTonalButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape
        ) {
            Text("You’re my best friend", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun LoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(strokeWidth = 4.dp, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(24.dp))
        Text("Finding your best friend...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
