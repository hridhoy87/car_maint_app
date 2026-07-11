package hobby.asad.mushad.mycar;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsDashboardFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private NestedScrollView scrollView;
    private ProgressBar loadingProgress;
    private LinearLayout containerMonthlyReport, containerTripCosts;
    private MultiSelectSpinner filterWeeklyGraph, filterMonthlyGraph;
    private SimpleLineChartView chartWeekly, chartMonthly, chartComparison;
    private TableLayout tableComparison;
    private TextView tvTotalTripCost;
    private View notificationBanner;
    private TextView bannerText;
    
    private String activeFilter = null;
    private String currentCarName = "";
    private String[] heads;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        heads = new String[]{
                getString(R.string.head_total), getString(R.string.head_toll),
                getString(R.string.head_fuel), getString(R.string.head_maintenance),
                getString(R.string.head_repair), getString(R.string.head_beautification),
                getString(R.string.head_all)
        };
        return inflater.inflate(R.layout.fragment_statistics_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            activeFilter = getArguments().getString("STAT_FILTER");
        }

        initViews(view);
        setupSpinners(view);
        setupFilters();
        setupToggle(view);

        swipeRefresh.setOnRefreshListener(this::startLazyLoading);

        view.findViewById(R.id.btn_export_pdf).setOnClickListener(v -> 
            Toast.makeText(getContext(), R.string.exporting_pdf, Toast.LENGTH_SHORT).show());
    }

    public void onCarSelected(String carName) {
        if (notificationBanner != null) notificationBanner.setVisibility(View.GONE);
        startLazyLoading();
    }

    private void showEmptyBanner(String carName) {
        if (notificationBanner != null && bannerText != null) {
            bannerText.setText(getString(R.string.no_data_for_vehicle, carName));
            notificationBanner.setVisibility(View.VISIBLE);
            notificationBanner.setAlpha(1f);
            notificationBanner.setTranslationX(0f);
        }
    }

    private void initViews(View v) {
        swipeRefresh = v.findViewById(R.id.swipe_refresh);
        scrollView = v.findViewById(R.id.scroll_view);
        loadingProgress = v.findViewById(R.id.loading_progress);
        
        containerMonthlyReport = v.findViewById(R.id.container_monthly_report);
        containerTripCosts = v.findViewById(R.id.container_trip_costs);
        filterWeeklyGraph = v.findViewById(R.id.filter_weekly_graph);
        filterMonthlyGraph = v.findViewById(R.id.filter_monthly_graph);
        chartWeekly = v.findViewById(R.id.chart_weekly);
        chartMonthly = v.findViewById(R.id.chart_monthly);
        chartComparison = v.findViewById(R.id.chart_comparison);
        tableComparison = v.findViewById(R.id.table_comparison);
        tvTotalTripCost = v.findViewById(R.id.tv_total_trip_cost);
        notificationBanner = v.findViewById(R.id.notification_banner);
        bannerText = v.findViewById(R.id.banner_text);
        
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).setupBannerSwipe(notificationBanner);
        }
    }

    private void setupSpinners(View view) {
        String[] months = {
                getString(R.string.month_jan), getString(R.string.month_feb), getString(R.string.month_mar),
                getString(R.string.month_apr), getString(R.string.month_may), getString(R.string.month_jun),
                getString(R.string.month_jul), getString(R.string.month_aug), getString(R.string.month_sep),
                getString(R.string.month_oct), getString(R.string.month_nov), getString(R.string.month_dec)
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ((Spinner)view.findViewById(R.id.spinner_monthly_report)).setAdapter(monthAdapter);
        ((Spinner)view.findViewById(R.id.spinner_year_from_month)).setAdapter(monthAdapter);
        ((Spinner)view.findViewById(R.id.spinner_year_to_month)).setAdapter(monthAdapter);
        ((Spinner)view.findViewById(R.id.trip_month_from)).setAdapter(monthAdapter);
        ((Spinner)view.findViewById(R.id.trip_month_to)).setAdapter(monthAdapter);

        String[] years = {"2023", "2024", "2025"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)view.findViewById(R.id.trip_year_from)).setAdapter(yearAdapter);
        ((Spinner)view.findViewById(R.id.trip_year_to)).setAdapter(yearAdapter);
    }

    private void setupFilters() {
        filterWeeklyGraph.setItems(heads);
        filterMonthlyGraph.setItems(heads);

        if (activeFilter != null) {
            filterWeeklyGraph.setSelection(new String[]{activeFilter});
            filterMonthlyGraph.setSelection(new String[]{activeFilter});
        }

        filterWeeklyGraph.setOnSelectionChangedListener(selection -> updateWeeklyChart(currentCarName, selection));
        filterMonthlyGraph.setOnSelectionChangedListener(selection -> updateMonthlyChart(currentCarName, selection));
    }

    private void setupToggle(View view) {
        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggle_comparison);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateComparisonChart(currentCarName);
        });
    }

    private void startLazyLoading() {
        loadingProgress.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        currentCarName = ""; 
        if (getActivity() != null) {
            Spinner carSelector = getActivity().findViewById(R.id.car_selector);
            if (carSelector != null && carSelector.getSelectedItem() != null) {
                currentCarName = carSelector.getSelectedItem().toString();
            }
        }

        final String carName = currentCarName;
        DataRepository.fetchStatisticsData(requireContext(), carName, activeFilter, new DataRepository.DataCallback() {
            @Override
            public void onSuccess(org.json.JSONObject data) {
                try {
                    boolean hasData = data.optBoolean("hasData", false);
                    if (!hasData) {
                        showEmptyBanner(carName);
                    } else {
                        if (notificationBanner != null) notificationBanner.setVisibility(View.GONE);
                    }

                    org.json.JSONArray report = data.getJSONArray("report");
                    containerMonthlyReport.removeAllViews();
                    for (int i = 0; i < report.length(); i++) {
                        org.json.JSONObject row = report.getJSONObject(i);
                        addReportRow(containerMonthlyReport, row.getString("label"), 
                                String.format(Locale.US, "$%.2f", row.getDouble("amount")), false);
                    }
                    addReportRow(containerMonthlyReport, getString(R.string.head_total), 
                            String.format(Locale.US, "$%.2f", data.getDouble("total")), true);

                    populateTripCosts(carName);
                    populateComparisonTable();
                    updateCharts(carName);

                    loadingProgress.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                } catch (org.json.JSONException e) {
                    onError(e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.error_prefix, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addReportRow(LinearLayout container, String label, String value, boolean isTotal) {
        if (getContext() == null) return;
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_report_row, container, false);
        TextView tvLabel = row.findViewById(R.id.row_label);
        TextView tvValue = row.findViewById(R.id.row_value);
        tvLabel.setText(label);
        tvValue.setText(value);
        if (isTotal) {
            tvLabel.setTextSize(16);
            tvLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            tvLabel.setTextColor(Color.BLACK);
            tvValue.setTextSize(16);
            tvValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.brandAccent));
        }
        container.addView(row);
    }

    private void populateTripCosts(String carName) {
        containerTripCosts.removeAllViews();
        new Thread(() -> {
            try {
                hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(requireContext());
                hobby.asad.mushad.mycar.database.Vehicle vehicle = db.vehicleDao().getVehicleByModel(carName);
                if (vehicle == null) return;

                java.util.List<hobby.asad.mushad.mycar.database.Expenditure> trips = db.expenditureDao().getExpendituresByCategory(vehicle.id, "TRIP");
                double total = 0;
                
                final double finalTotal = total;
                java.util.List<View> views = new java.util.ArrayList<>();
                
                for (hobby.asad.mushad.mycar.database.Expenditure trip : trips) {
                    total += trip.amount;
                    TextView tv = new TextView(getContext());
                    tv.setText(String.format(Locale.US, "%s - $%.2f", trip.title, trip.amount));
                    tv.setPadding(0, 8, 0, 8);
                    views.add(tv);
                    View divider = new View(getContext());
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.LTGRAY);
                    views.add(divider);
                }

                final double updatedTotal = total;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    for (View v : views) containerTripCosts.addView(v);
                    tvTotalTripCost.setText(String.format(Locale.US, "$%.2f", updatedTotal));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateComparisonTable() {
        tableComparison.removeAllViews();
        String[] headers = {
                getString(R.string.table_vehicle), getString(R.string.head_toll), 
                getString(R.string.head_fuel), getString(R.string.table_maint), 
                getString(R.string.head_repair), getString(R.string.table_beauty)
        };
        TableRow headerRow = new TableRow(getContext());

        android.util.TypedValue typedValue = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceVariant, typedValue, true);
        int headerBg = typedValue.data;
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true);
        int headerText = typedValue.data;
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        int dataText = typedValue.data;

        for (String h : headers) {
            TextView tv = new TextView(getContext());
            tv.setText(h);
            tv.setPadding(16, 16, 16, 16);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setBackgroundColor(headerBg);
            tv.setTextColor(headerText);
            headerRow.addView(tv);
        }
        tableComparison.addView(headerRow);

        new Thread(() -> {
            hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(requireContext());
            java.util.List<hobby.asad.mushad.mycar.database.Vehicle> vehicles = db.vehicleDao().getAllVehicles();

            for (hobby.asad.mushad.mycar.database.Vehicle v : vehicles) {
                double toll = getAmount(db, v.id, "TOLL");
                double fuel = getAmount(db, v.id, "FUEL");
                double maint = getAmount(db, v.id, "MAINTENANCE");
                double repair = getAmount(db, v.id, "REPAIR");
                double beauty = getAmount(db, v.id, "BEAUTIFICATION");

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    TableRow row = new TableRow(getContext());
                    String[] rowData = {
                        v.model, 
                        String.format(Locale.US, "%.0f", toll),
                        String.format(Locale.US, "%.0f", fuel),
                        String.format(Locale.US, "%.0f", maint),
                        String.format(Locale.US, "%.0f", repair),
                        String.format(Locale.US, "%.0f", beauty)
                    };
                    for (String cell : rowData) {
                        TextView tv = new TextView(getContext());
                        tv.setText(cell);
                        tv.setPadding(16, 16, 16, 16);
                        tv.setTextColor(dataText);
                        row.addView(tv);
                    }
                    tableComparison.addView(row);
                });
            }
        }).start();
    }

    private double getAmount(hobby.asad.mushad.mycar.database.AppDatabase db, String vehicleId, String category) {
        Double amt = db.expenditureDao().getTotalAmountByCategory(vehicleId, category);
        return amt != null ? amt : 0.0;
    }

    private void updateCharts(String carName) {
        boolean[] selection = new boolean[heads.length];
        if (activeFilter != null) {
            for (int i = 0; i < heads.length; i++) {
                if (heads[i].equalsIgnoreCase(activeFilter)) {
                    selection[i] = true;
                    break;
                }
            }
        }
        updateWeeklyChart(carName, selection);
        updateMonthlyChart(carName, selection);
        updateComparisonChart(carName);
    }

    private void updateWeeklyChart(String carName, boolean[] selection) {
        getDataForSelection(carName, selection, 7, data -> {
            chartWeekly.setData(data);
        });
    }

    private void updateMonthlyChart(String carName, boolean[] selection) {
        getDataForSelection(carName, selection, 12, data -> {
            chartMonthly.setData(data);
        });
    }

    interface ChartDataCallback {
        void onDataReady(List<List<Float>> data);
    }

    private void getDataForSelection(String carName, boolean[] selection, int pointsCount, ChartDataCallback callback) {
        new Thread(() -> {
            List<List<Float>> results = new ArrayList<>();
            try {
                hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(requireContext());
                hobby.asad.mushad.mycar.database.Vehicle vehicle = db.vehicleDao().getVehicleByModel(carName);
                if (vehicle == null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataReady(results));
                    return;
                }

                // Map UI head to DB category
                java.util.Map<String, String> headToCat = new java.util.HashMap<>();
                headToCat.put("Toll", "TOLL");
                headToCat.put("Fuel", "FUEL");
                headToCat.put("Maintenance", "MAINTENANCE");
                headToCat.put("Repair", "REPAIR");
                headToCat.put("Beautification", "BEAUTIFICATION");

                long now = System.currentTimeMillis();
                long step = pointsCount == 7 ? 86400000L : 2592000000L; // Day vs 30 Days approx

                boolean anySelected = false;
                int allIndex = -1;
                for(int i=0; i<heads.length; i++) {
                    if(heads[i].equalsIgnoreCase("All")) allIndex = i;
                }

                for (int i = 0; i < heads.length; i++) {
                    if (selection[i] || (allIndex != -1 && selection[allIndex] && !heads[i].equalsIgnoreCase("All"))) {
                        String cat = headToCat.get(heads[i]);
                        if (cat != null) {
                            List<Float> row = new ArrayList<>();
                            for (int p = pointsCount - 1; p >= 0; p--) {
                                long end = now - (p * step);
                                long start = end - step;
                                Double amt = db.expenditureDao().getTotalAmountByCategoryAndDateRange(vehicle.id, cat, start, end);
                                row.add(amt != null ? amt.floatValue() : 0f);
                            }
                            results.add(row);
                            anySelected = true;
                        }
                    }
                }

                if (!anySelected) {
                    results.add(new ArrayList<>(java.util.Collections.nCopies(pointsCount, 0f)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataReady(results));
        }).start();
    }

    private void updateComparisonChart(String carName) {
        // Just show the current vehicle data for now, or multiple if we want comparison
        getDataForSelection(carName, new boolean[heads.length], 10, data -> {
             chartComparison.setData(data);
        });
    }
}
