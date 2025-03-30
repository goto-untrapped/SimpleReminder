package com.example.rememberme1

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import android.Manifest
import android.app.TimePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // パーミッションの確認とリクエスト
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
                ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // viewModelが依存関係を持つため、viewModelインスタンス生成のタイミングで
        // db, repository, factoryを組み立てる
        val database = ReminderDatabase.getDatabase(this)
        val repository = ReminderRepository(database.reminderDao())
        val factory = ReminderViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(ReminderViewModel::class.java)
        enableEdgeToEdge()
        setContent {
            Column {
                RegisterLayout(viewModel)
                ShowLayout(viewModel)
            }
        }
    }

    private val requestPermissionLauncher =
        // registerForActivityResult を呼ぶと ActivityResultLauncher 型のオブジェクトになる
        // ActivityResultContracts.RequestPermission() を引数にすることで、コールバック関数を
        // オブジェクトに紐づけられる
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
            if (isGranted) {
                // パーミッションが付与された
            } else {
                // パーミッションが拒否された
            }
        }
}


@Composable
fun RegisterLayout(viewModel: ReminderViewModel) {
    // 初期値設定後、MutableStateのみ紐づけて値の更新時、UIに反映できる
    var text by remember { mutableStateOf("") }
    // Compose を使うとローカルなコンテキストを渡せる
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTime by remember { mutableStateOf(LocalDateTime.now()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 時刻選択ボタン
        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                // 時刻選択ダイアログを表示
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        selectedTime = LocalDateTime.now()
                            .withHour(selectedHour)
                            .withMinute(selectedMinute)
                            .withSecond(0)
                    },
                    hour,
                    minute,
                    true
                ).show()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("時刻を選択: ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                val reminder = Reminder(title = text)
                // 登録
                viewModel.insertReminder(reminder)
                val now = LocalDateTime.now()
                val initialDelay = ChronoUnit.SECONDS.between(now, selectedTime)
                // WorkManager で通知をスケジュール
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(
                        Data.Builder()
                            .putString(ReminderWorker.REMINDER_CONTENT, text)
                            .build()
                    )
                    // 遅延時間を設定
                    .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                    // 再試行ポリシーを設定(なくてもよさそう)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        Duration.ofMinutes(1)
                    )
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
                // 入力欄をクリア
                text = ""
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("登録")
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
