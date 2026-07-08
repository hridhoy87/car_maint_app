package hobby.asad.mushad.mycar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ReminderManager {
    private static final String PREF_NAME = "reminders_prefs";
    private static final String KEY_REMINDERS = "reminders_list";

    public static void saveReminders(Context context, List<Reminder> reminders) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (Reminder r : reminders) {
            try {
                array.put(new org.json.JSONObject(r.toJson()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_REMINDERS, array.toString()).apply();
        scheduleAlarms(context, reminders);
    }

    public static List<Reminder> loadReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_REMINDERS, null);
        List<Reminder> list = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    list.add(Reminder.fromJson(array.getJSONObject(i).toString()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        if (list.isEmpty()) {
            list = getDefaultReminders(context);
        }
        return list;
    }

    private static List<Reminder> getDefaultReminders(Context context) {
        List<Reminder> list = new ArrayList<>();
        list.add(new Reminder("1", context.getString(R.string.engine_oil_reminder), "OIL", 0, 0, "RING", false));
        list.add(new Reminder("2", context.getString(R.string.transmission_oil_reminder), "TRANS", 0, 0, "RING", false));
        list.add(new Reminder("3", context.getString(R.string.brake_fluid_reminder), "BRAKE", 0, 0, "RING", false));
        list.add(new Reminder("4", context.getString(R.string.tax_date_reminder), "TAX", 0, 0, "RING", false));
        list.add(new Reminder("5", context.getString(R.string.custom_reminder), "CUSTOM", 0, 0, "RING", false));
        return list;
    }

    public static void scheduleAlarms(Context context, List<Reminder> reminders) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        for (Reminder r : reminders) {
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("reminder_id", r.getId());
            intent.putExtra("reminder_title", r.getTitle());
            intent.putExtra("notification_mode", r.getNotificationMode());
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                Integer.parseInt(r.getId()), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (r.isEnabled() && r.getDueDate() > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            r.getDueDate(),
                            pendingIntent
                        );
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            r.getDueDate(),
                            pendingIntent
                        );
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        r.getDueDate(),
                        pendingIntent
                    );
                }
            } else {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public static long calculateDueDate(String type, long lastDate) {
        if (lastDate <= 0) return 0;
        
        LocalDate date = Instant.ofEpochMilli(lastDate).atZone(ZoneId.systemDefault()).toLocalDate();
        switch (type) {
            case "OIL":
                // 3.5 months = 3 months + 15 days
                date = date.plusMonths(3).plusDays(15);
                break;
            case "TRANS":
                date = date.plusYears(1);
                break;
            case "BRAKE":
                date = date.plusMonths(9);
                break;
            default:
                return lastDate; // For TAX and CUSTOM, user sets the date directly
        }
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
