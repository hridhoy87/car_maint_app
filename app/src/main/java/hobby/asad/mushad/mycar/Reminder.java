package hobby.asad.mushad.mycar;

import org.json.JSONException;
import org.json.JSONObject;

public class Reminder {
    private String id;
    private String title;
    private String type; // OIL, TRANS, BRAKE, TAX, CUSTOM
    private long lastDate;
    private long dueDate;
    private String notificationMode; // RING, VIBRATE, MUTE
    private boolean isEnabled;

    public Reminder(String id, String title, String type, long lastDate, long dueDate, String notificationMode, boolean isEnabled) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.lastDate = lastDate;
        this.dueDate = dueDate;
        this.notificationMode = notificationMode;
        this.isEnabled = isEnabled;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public long getLastDate() { return lastDate; }
    public long getDueDate() { return dueDate; }
    public String getNotificationMode() { return notificationMode; }
    public boolean isEnabled() { return isEnabled; }

    public void setLastDate(long lastDate) { this.lastDate = lastDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public void setNotificationMode(String notificationMode) { this.notificationMode = notificationMode; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("title", title);
            json.put("type", type);
            json.put("lastDate", lastDate);
            json.put("dueDate", dueDate);
            json.put("notificationMode", notificationMode);
            json.put("isEnabled", isEnabled);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Reminder fromJson(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            return new Reminder(
                json.getString("id"),
                json.getString("title"),
                json.getString("type"),
                json.getLong("lastDate"),
                json.getLong("dueDate"),
                json.getString("notificationMode"),
                json.getBoolean("isEnabled")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
