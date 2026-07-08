package hobby.asad.mushad.mycar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataRepository {

    public interface DataCallback {
        void onSuccess(JSONObject data);
        void onError(String error);
    }

    public static void fetchExpenditureData(Context context, String carName, DataCallback callback) {
        // Simulate loading delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Check cache first
                SharedPreferences prefs = context.getSharedPreferences("expenditure_cache", Context.MODE_PRIVATE);
                String cachedData = prefs.getString(carName, null);

                if (cachedData != null) {
                    callback.onSuccess(new JSONObject(cachedData));
                    return;
                }

                // Simulate DB Fetch / JSON creation
                JSONObject root = new JSONObject();
                JSONArray entries = new JSONArray();

                // Dummy Entry 1
                JSONObject entry1 = new JSONObject();
                entry1.put("type", "single");
                entry1.put("date", "Oct 24, 2023");
                entry1.put("title", "Full Tank Refuel");
                entry1.put("amount", "$85.00");
                entries.put(entry1);

                // Dummy Group Entry
                JSONObject group = new JSONObject();
                group.put("type", "group");
                group.put("date", "Oct 22, 2023");
                group.put("title", "Maintenance Day");
                group.put("summary", "3 Items • Click to expand");
                
                JSONArray children = new JSONArray();
                JSONObject child1 = new JSONObject();
                child1.put("title", "Oil Change");
                child1.put("amount", "$120.00");
                children.put(child1);

                JSONObject child2 = new JSONObject();
                child2.put("title", "Tire Rotation");
                child2.put("amount", "$40.00");
                children.put(child2);

                JSONObject child3 = new JSONObject();
                child3.put("title", "Car Wash");
                child3.put("amount", "$15.00");
                children.put(child3);

                group.put("children", children);
                entries.put(group);

                // Dummy Entry 3
                JSONObject entry3 = new JSONObject();
                entry3.put("type", "single");
                entry3.put("date", "Oct 15, 2023");
                entry3.put("title", "Parking Fee");
                entry3.put("amount", "$10.00");
                entries.put(entry3);

                root.put("entries", entries);

                // Save to cache
                prefs.edit().putString(carName, root.toString()).apply();

                callback.onSuccess(root);

            } catch (JSONException e) {
                callback.onError(e.getMessage());
            }
        }, 1500); // 1.5 second delay
    }
}