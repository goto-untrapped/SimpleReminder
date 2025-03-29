package com.example.rememberme1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.Manifest

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val reminderContent = inputData.getString(REMINDER_CONTENT) ?: "リマインダー"
        showNotification(reminderContent)
        // 通知をここで送信
        return Result.success()
    }

    private fun showNotification(content: String) {
        val channelId = "reminder_channel"
        val notificationId = 1

        // 通知チャネルの作成（Android 8.0 以降）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "リマインダー通知"
            val descriptionText = "リマインダーの内容をお知らせします"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                        NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 通知の作成
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("リマインダー")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // 通知の表示
        with(NotificationManagerCompat.from(applicationContext)) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
                ) {
                // パーミッションが不足している
                return
            }
            notify(notificationId, builder.build())
        }
    }

    companion object {
        const val REMINDER_CONTENT = "reminder_content"
    }
}