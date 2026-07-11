package hobby.asad.mushad.mycar;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hobby.asad.mushad.mycar.database.AppDatabase;
import hobby.asad.mushad.mycar.database.Expenditure;
import hobby.asad.mushad.mycar.database.Vehicle;

public class DataRepository {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public interface DataCallback {
        void onSuccess(JSONObject data);
        void onError(String error);
    }

    public static void fetchExpenditureData(Context context, String carName, DataCallback callback) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                Vehicle vehicle = db.vehicleDao().getVehicleByModel(carName);

                if (vehicle == null) {
                    mainHandler.post(() -> callback.onError(context.getString(R.string.error_vehicle_not_found)));
                    return;
                }

                List<Expenditure> allEntries = db.expenditureDao().getExpendituresForVehicle(vehicle.id);
                int totalCount = allEntries.size();
                
                JSONObject root = new JSONObject();
                root.put("hasData", totalCount > 0);
                root.put("vehicleName", carName);
                JSONArray entriesArray = new JSONArray();

                // Group by date
                Map<String, List<Expenditure>> groupedByDate = new LinkedHashMap<>();
                for (Expenditure e : allEntries) {
                    String dayKey = dayFormat.format(new Date(e.date));
                    if (!groupedByDate.containsKey(dayKey)) {
                        groupedByDate.put(dayKey, new ArrayList<>());
                    }
                    groupedByDate.get(dayKey).add(e);
                }

                for (Map.Entry<String, List<Expenditure>> entry : groupedByDate.entrySet()) {
                    List<Expenditure> dayItems = entry.getValue();
                    String formattedDate = dateFormat.format(new Date(dayItems.get(0).date));
                    
                    if (dayItems.size() == 1) {
                        Expenditure exp = dayItems.get(0);
                        JSONObject entryJson = new JSONObject();
                        entryJson.put("type", "single");
                        entryJson.put("date", formattedDate);
                        entryJson.put("title", formatTitle(exp.title));
                        entryJson.put("amount", String.format(Locale.US, "$%.2f", exp.amount));
                        entriesArray.put(entryJson);
                    } else {
                        JSONObject groupJson = new JSONObject();
                        groupJson.put("type", "group");
                        groupJson.put("date", formattedDate);
                        groupJson.put("title", context.getString(R.string.daily_activities));
                        groupJson.put("summary", context.getString(R.string.items_summary, dayItems.size()));
                        
                        JSONArray childrenArray = new JSONArray();
                        for (Expenditure child : dayItems) {
                            JSONObject childJson = new JSONObject();
                            childJson.put("title", formatTitle(child.title));
                            childJson.put("amount", String.format(Locale.US, "$%.2f", child.amount));
                            childrenArray.put(childJson);
                        }
                        groupJson.put("children", childrenArray);
                        entriesArray.put(groupJson);
                    }
                }

                root.put("entries", entriesArray);

                mainHandler.post(() -> callback.onSuccess(root));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private static String formatTitle(String title) {
        if (title == null) return "";
        return title.replace("|", ", ");
    }

    /**
     * Helper to fetch statistics data for the dashboard
     */
    public static void fetchStatisticsData(Context context, String carName, String categoryFilter, DataCallback callback) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                Vehicle vehicle = db.vehicleDao().getVehicleByModel(carName);

                if (vehicle == null) {
                    mainHandler.post(() -> callback.onError(context.getString(R.string.error_vehicle_not_found)));
                    return;
                }

                JSONObject result = new JSONObject();
                int totalCount = db.expenditureDao().getExpenditureCount(vehicle.id);
                result.put("hasData", totalCount > 0);
                result.put("vehicleName", carName);

                String[] categories = {"TOLL", "FUEL", "MAINTENANCE", "REPAIR", "BEAUTIFICATION"};
                JSONArray reportArray = new JSONArray();
                double totalAmount = 0;

                for (String cat : categories) {
                    if (categoryFilter == null || categoryFilter.equalsIgnoreCase("All") || categoryFilter.equalsIgnoreCase("Total") || cat.equalsIgnoreCase(categoryFilter)) {
                        Double amount = db.expenditureDao().getTotalAmountByCategory(vehicle.id, cat);
                        double val = (amount != null) ? amount : 0.0;
                        
                        JSONObject row = new JSONObject();
                        row.put("label", cat.substring(0, 1).toUpperCase() + cat.substring(1).toLowerCase());
                        row.put("amount", val);
                        reportArray.put(row);
                        totalAmount += val;
                    }
                }

                result.put("report", reportArray);
                result.put("total", totalAmount);

                mainHandler.post(() -> callback.onSuccess(result));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
