package hobby.asad.mushad.mycar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("reminder_title");
        String mode = intent.getStringExtra("notification_mode");
        String idStr = intent.getStringExtra("reminder_id");
        
        if (idStr == null) return;
        
        int id = Integer.parseInt(idStr);
        showNotification(context, id, title, mode);
    }

    private void showNotification(Context context, int id, String title, String mode) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Car Reminders", NotificationManager.IMPORTANCE_HIGH);
        
        if ("VIBRATE".equals(mode)) {
            channel.enableVibration(true);
            channel.setSound(null, null);
        } else if ("MUTE".equals(mode)) {
            channel.enableVibration(false);
            channel.setSound(null, null);
        } else {
            channel.enableVibration(true);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(alarmSound, audioAttributes);
        }
        notificationManager.createNotificationChannel(channel);

        Intent notificationIntent = new Intent(context, ReminderActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle("Car Reminder")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setNumber(1)
                .setContentIntent(pendingIntent);

        if ("VIBRATE".equals(mode)) {
            builder.setVibrate(new long[]{0, 500, 200, 500});
            builder.setSound(null);
        } else if ("MUTE".equals(mode)) {
            builder.setVibrate(null);
            builder.setSound(null);
        } else {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            builder.setSound(alarmSound);
        }

        notificationManager.notify(id, builder.build());
        
        // If mode is RING or VIBRATE, and user said "set off alarm", maybe we should trigger something more?
        // But for now, high priority notification is a good start.
    }
}
