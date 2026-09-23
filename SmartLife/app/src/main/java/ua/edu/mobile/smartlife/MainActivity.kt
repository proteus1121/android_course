package ua.edu.mobile.smartlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ua.edu.mobile.smartlife.ui.SmartLifeApp
import ua.edu.mobile.smartlife.ui.theme.SmartLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartLifeTheme {
                SmartLifeApp()
            }
        }
    }
}
