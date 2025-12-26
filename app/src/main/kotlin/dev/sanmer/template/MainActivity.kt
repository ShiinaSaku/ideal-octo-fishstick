package dev.sanmer.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/* ----------------------------- DATA ----------------------------- */

data class GitHubUser(
    @SerializedName("login") val login: String,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar_url") val avatarUrl: String
)

/* ----------------------------- NETWORK ----------------------------- */

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

/* ----------------------------- UI STATE ----------------------------- */

sealed interface UserUiState {
    data object Loading : UserUiState
    data class Success(val user: GitHubUser) : UserUiState
    data class Error(val message: String) : UserUiState
}

/* ----------------------------- VIEWMODEL ----------------------------- */

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
                uiState = UserUiState.Error(it.message ?: "Unknown error")
            }
        }
    }
}

/* ----------------------------- ACTIVITY ----------------------------- */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BestFriendTheme {
                MainScreen()
            }
        }
    }
}

/* ----------------------------- THEME ----------------------------- */

@Composable
fun BestFriendTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            darkColorScheme()
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

/* ----------------------------- SCREEN ----------------------------- */

@Composable
fun MainScreen(viewModel: UserViewModel = viewModel()) {
    val username = "prslc"

    LaunchedEffect(Unit) {
        viewModel.fetchUser(username)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = viewModel.uiState,
                label = "user_state"
            ) { state ->
                when (state) {
                    UserUiState.Loading -> LoadingState()
                    is UserUiState.Error -> ErrorState(state.message)
                    is UserUiState.Success -> UserContent(state.user)
                }
            }
        }
    }
}

/* ----------------------------- CONTENT ----------------------------- */

@Composable
fun UserContent(user: GitHubUser) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            model = user.avatarUrl,
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(32.dp))

        ExpressiveMessageCard(user.name ?: user.login)

        Spacer(Modifier.height(24.dp))

        ElevatedButton(
            shape = RoundedCornerShape(28.dp),
            onClick = {}
        ) {
            Text("You’re my best friend")
        }
    }
}

@Composable
fun ExpressiveMessageCard(name: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$name, you're so cute!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "$name、あなたはとてもかわいいです！",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

/* ----------------------------- STATES ----------------------------- */

@Composable
fun LoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Fetching best friend…")
    }
}

@Composable
fun ErrorState(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )
}
