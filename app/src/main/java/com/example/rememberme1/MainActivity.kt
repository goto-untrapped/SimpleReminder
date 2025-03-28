package com.example.rememberme1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rememberme1.ui.theme.RememberMe1Theme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // viewModelに依存関係があるため、viewModelインスタンス生成のタイミングで
        // db, repository, factoryを取得
        val database = ReminderDatabase.getDatabase(this)
        val repository = ReminderRepository(database.reminderDao())
        val factory = ReminderViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(ReminderViewModel::class.java)
        enableEdgeToEdge()
        setContent {
            RegisterLayout(viewModel)
            ShowLayout(viewModel)
        }
    }
}
// test
@Composable
fun RegisterLayout(viewModel: ReminderViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var text by remember { mutableStateOf("") }
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val reminder = Reminder(title = text)
                viewModel.insertReminder(reminder)
                text = ""
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("ボタン")
        }
    }
}

@Composable
fun ShowLayout(viewModel: ReminderViewModel) {
    val reminders by viewModel.allReminders.collectAsState(initial = emptyList())
    LazyColumn {
        items(reminders) { reminder ->
            Text(text = reminder.title)
        }
    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RememberMe1Theme {
        Greeting("Android")
    }
}

