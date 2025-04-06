package com.example.rememberme1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.gson.GsonBuilder
import java.time.LocalTime


class DisplayAllRemindersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // viewModelが依存関係を持つため、viewModelインスタンス生成のタイミングで
        // db, repository, factoryを組み立てる
        val database = ReminderDatabase.getDatabase(this)
        val repository = ReminderRepository(database.reminderDao())
        val factory = ReminderViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(ReminderViewModel::class.java)
        setContent {
            MyCustomTheme {
                Column {
                    Spacer(modifier = Modifier.padding(top = 20.dp))
                    ScreenTitle("R E M I N D E R")
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MyCustomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF333333), // ここで primaryContainer を上書き
            onPrimaryContainer = Color.White,
            secondary = Color(0xFFBDBDBD),
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF424242),
            onSecondaryContainer = Color.Black,
            background = Color(0xFF121212),
            onBackground = Color.White,
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF333333),
            onSurfaceVariant = Color.White,
            error = Color(0xFFCF6679),
            onError = Color.Black,
            errorContainer = Color(0xFFB3261E),
            onErrorContainer = Color.White,
            outline = Color(0xFF757575)
        ),
        content = content
    )
}

@Composable
fun ScreenTitle(title: String) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
    ) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

@Composable
fun MainScreen(viewModel: ReminderViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // ボタンがクリックされたときの処理
                // 例: 新しいアイテムを追加するダイアログを表示する
                // 画面遷移
                val intent = Intent(context, RegisterReminderActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Filled.Add, "追加")
            }
        },
        content = { paddingValues ->
            Log.d("PaddingValues", "Top: ${paddingValues.calculateTopPadding()}, Bottom: ${paddingValues.calculateBottomPadding()}")
            RemindersLayout(viewModel, modifier = Modifier.padding(paddingValues))
        }
    )
}

@Composable
fun RemindersLayout(viewModel: ReminderViewModel, modifier: Modifier) {
    // データベースからリマインダーのリストを取得
    val reminders by viewModel.allReminders.collectAsState(initial = emptyList())
    LazyColumn {
        items(reminders) { reminder ->
            FloatingText(reminder)
        }
    }
}

@Composable
fun FloatingText(reminder: Reminder) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                // 画面遷移
                val intent = Intent(context, RegisterReminderActivity::class.java)
                val gson = GsonBuilder()
                    .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
                    .create()
                intent.putExtra("reminder", gson.toJson(reminder))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Text(
            text = reminder.title,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}