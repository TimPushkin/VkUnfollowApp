package me.timpushkin.vkunsubapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import me.timpushkin.vkunsubapp.ui.theme.VkUnsubAppTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val authLauncher = VK.login(this) { result ->
        when (result) {
            is VKAuthenticationResult.Success -> Log.i(TAG, "Login succeeded")
            is VKAuthenticationResult.Failed -> Log.i(TAG, "Login failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authLauncher.launch(listOf(VKScope.GROUPS))

        setContent {
            VkUnsubAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}
