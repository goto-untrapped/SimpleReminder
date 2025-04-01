package com.example.rememberme1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider


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
            Column {
                ShowLayout(viewModel)
                Button(
                    onClick = {
                        // 画面遷移
                        val intent = Intent(
                            this@DisplayAllRemindersActivity,
                            RegisterReminderActivity::class.java
                        )
                        startActivity(intent)
                    }
                ) { }
            }
        }
    }
}

@Composable
fun ShowLayout(viewModel: ReminderViewModel) {
    // データベースからリマインダーのリストを取得
    val reminders by viewModel.allReminders.collectAsState(initial = emptyList())
    LazyColumn {
        items(reminders) { reminder ->
            Text(text = reminder.title)
        }
    }
}
