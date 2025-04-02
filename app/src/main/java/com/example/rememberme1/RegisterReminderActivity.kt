package com.example.rememberme1

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class RegisterReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
        // もし登録済のリマインドを一覧で選択した場合、選択したリマインドを表示する
        val reminderJson = intent.getStringExtra("reminder")
        var update = false
        if (reminderJson != null) {
            val reminder = reminderJson.let { Gson().fromJson(it, Reminder::class.java) }
            viewModel.reminder = reminder
            update = true
        }

        setContent {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.padding(top = 10.dp)) {
                    CancelButton()
                    // 他の要素のサイズを除いたサイズ分、空白で埋める
                    Spacer(modifier = Modifier.weight(1f))
                    RegisterOrUpdateButton(viewModel, update)
                }
                Column {
                    RegisterLayout(viewModel)
                }
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
fun CancelButton() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Button(onClick = {
        // 画面遷移
        val intent = Intent(context, DisplayAllRemindersActivity::class.java)
        context.startActivity(intent)
    }) {
        Text("キャンセル")
    }
}

@Composable
fun RegisterOrUpdateButton(viewModel: ReminderViewModel, update: Boolean) {
    // Compose を使うとローカルなコンテキストを渡せる
    val context = androidx.compose.ui.platform.LocalContext.current
    val selectedTime by remember { mutableStateOf(LocalDateTime.now()) }
    Button(
        onClick = {
            if (update) {
                // 更新
                viewModel.updateReminder(viewModel.reminder)
            } else {
                // 登録
                viewModel.insertReminder(viewModel.reminder)
            }
            val now = LocalDateTime.now()
            val initialDelay = ChronoUnit.SECONDS.between(now, selectedTime)
            // 通知設定
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(ReminderWorker.REMINDER_CONTENT, viewModel.reminder.title)
                        .build()
                )
                // 遅延時間を設定
                .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
            // 曜日が設定されている場合
            viewModel.reminder.remindWeekDays.forEach { dayOfWeek ->
                // WorkManager で通知をスケジュール
                val workRequestPerWeek = PeriodicWorkRequestBuilder<ReminderWorker>(
                    repeatInterval = 7, // 7日ごとに繰り返す
                    repeatIntervalTimeUnit = TimeUnit.DAYS
                )
                    .setInputData(
                        Data.Builder()
                            .putString(ReminderWorker.REMINDER_CONTENT, viewModel.reminder.title)
                            .build()
                    )
                    // 遅延時間を設定
                    .setInitialDelay(initialDelay, TimeUnit.SECONDS)
                    .addTag("weekly_reminder_${dayOfWeek}")
                    .build()
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        "weekly_reminder_${dayOfWeek}",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        workRequestPerWeek
                    )
            }

            // 画面遷移
            val intent = Intent(context, DisplayAllRemindersActivity::class.java)
            context.startActivity(intent)
        }
    ) {
        if (update) {
            Text("更新")
        } else {
            Text("登録")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterLayout(viewModel: ReminderViewModel) {
    // 初期値設定後、MutableStateのみ紐づけて値の更新時、UIに反映できる
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
            value = viewModel.reminder.title,
            onValueChange = { newTitle ->
                viewModel.reminder = viewModel.reminder.copy(title = newTitle)
            },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 曜日選択
        Text("繰り返す曜日:")
        FlowRow {
            DayOfWeek.entries.forEach { dayOfWeek ->
                DayOfWeekCheckbox(
                    dayOfWeek = dayOfWeek,
                    isSelected = viewModel.reminder.remindWeekDays.contains(dayOfWeek),
                    onDaySelected = { isSelected ->
                        val updatedWeekDays = if (isSelected) {
                            viewModel.reminder.remindWeekDays + dayOfWeek
                        } else {
                            viewModel.reminder.remindWeekDays - dayOfWeek
                        }
                        viewModel.reminder = viewModel.reminder.copy(remindWeekDays = updatedWeekDays)
                    }
                )
            }
        }
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
    }
}

@Composable
fun DayOfWeekCheckbox(
    dayOfWeek: DayOfWeek,
    isSelected: Boolean,
    onDaySelected: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onDaySelected(!isSelected) }
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onDaySelected
        )
        Text(
            text = dayOfWeek.getDisplayName(
                java.time.format.TextStyle.SHORT,
                java.util.Locale.getDefault()
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}