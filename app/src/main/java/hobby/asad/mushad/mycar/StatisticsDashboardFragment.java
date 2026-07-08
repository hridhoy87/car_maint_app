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
    private LinearLayout containerMonthlyReport, containerYearlyReport, containerTripCosts;
    private MultiSelectSpinner filterWeeklyGraph, filterMonthlyGraph;
    private SimpleLineChartView chartWeekly, chartMonthly, chartComparison;
    private TableLayout tableComparison;
    private TextView tvTotalTripCost;
    
    private String activeFilter = null;
    private final String[] heads = {"Total", "Toll", "Fuel - Octane", "Fuel - LPG", "Maintenance", "Repair", "Beautification", "All"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        startLazyLoading();

        swipeRefresh.setOnRefreshListener(this::startLazyLoading);

        view.findViewById(R.id.btn_export_pdf).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Exporting as PDF...", Toast.LENGTH_SHORT).show());
    }

    private void initViews(View v) {
        swipeRefresh = v.findViewById(R.id.swipe_refresh);
        scrollView = v.findViewById(R.id.scroll_view);
        loadingProgress = v.findViewById(R.id.loading_progress);
        
        containerMonthlyReport = v.findViewById(R.id.container_monthly_report);
        containerYearlyReport = v.findViewById(R.id.container_yearly_report);
        containerTripCosts = v.findViewById(R.id.container_trip_costs);
        filterWeeklyGraph = v.findViewById(R.id.filter_weekly_graph);
        filterMonthlyGraph = v.findViewById(R.id.filter_monthly_graph);
        chartWeekly = v.findViewById(R.id.chart_weekly);
        chartMonthly = v.findViewById(R.id.chart_monthly);
        chartComparison = v.findViewById(R.id.chart_comparison);
        tableComparison = v.findViewById(R.id.table_comparison);
        tvTotalTripCost = v.findViewById(R.id.tv_total_trip_cost);
    }

    private void setupSpinners(View view) {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
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

        filterWeeklyGraph.setOnSelectionChangedListener(selection -> updateWeeklyChart(selection));
        filterMonthlyGraph.setOnSelectionChangedListener(selection -> updateMonthlyChart(selection));
    }

    private void setupToggle(View view) {
        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggle_comparison);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateComparisonChart();
        });
    }

    private void startLazyLoading() {
        loadingProgress.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        Handler handler = new Handler(Looper.getMainLooper());
        
        handler.postDelayed(() -> {
            populateReport(containerMonthlyReport, new double[]{50, 200, 150, 100, 300, 50});
        }, 500);

        handler.postDelayed(() -> {
            populateReport(containerYearlyReport, new double[]{600, 2400, 1800, 1200, 3600, 600});
        }, 1000);

        handler.postDelayed(() -> {
            populateTripCosts();
            populateComparisonTable();
            updateCharts();
            
            loadingProgress.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }, 1500);
    }

    private void populateReport(LinearLayout container, double[] values) {
        container.removeAllViews();
        String[] labels = {"Toll", "Fuel - Octane", "Fuel - LPG", "Maintenance", "Repair", "Beautification"};
        double total = 0;
        for (int i = 0; i < labels.length; i++) {
            if (activeFilter == null || labels[i].equalsIgnoreCase(activeFilter) || activeFilter.equalsIgnoreCase("Total")) {
                addReportRow(container, labels[i], String.format(Locale.US, "$%.2f", values[i]), false);
                total += values[i];
            }
        }
        if (activeFilter == null || activeFilter.equalsIgnoreCase("Total")) {
            addReportRow(container, "Total", String.format(Locale.US, "$%.2f", total), true);
        }
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

    private void populateTripCosts() {
        containerTripCosts.removeAllViews();
        String[] trips = {"Trip to City Center - $15.00", "Weekend Getaway - $120.00", "Business Meet - $45.00"};
        for (String trip : trips) {
            TextView tv = new TextView(getContext());
            tv.setText(trip);
            tv.setPadding(0, 8, 0, 8);
            containerTripCosts.addView(tv);
            View divider = new View(getContext());
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.LTGRAY);
            containerTripCosts.addView(divider);
        }
        tvTotalTripCost.setText("$180.00");
    }

    private void populateComparisonTable() {
        tableComparison.removeAllViews();
        String[] headers = {"Vehicle", "Toll", "Octane", "LPG", "Maint.", "Repair", "Beauty"};
        TableRow headerRow = new TableRow(getContext());
        for (String h : headers) {
            TextView tv = new TextView(getContext());
            tv.setText(h);
            tv.setPadding(16, 16, 16, 16);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setBackgroundColor(Color.parseColor("#F0F0F0"));
            headerRow.addView(tv);
        }
        tableComparison.addView(headerRow);

        String[][] data = {
            {"Tesla S", "10", "0", "0", "50", "20", "10"},
            {"Porsche 911", "20", "150", "0", "100", "50", "30"},
            {"BMW M4", "15", "120", "0", "80", "40", "20"}
        };

        for (String[] rowData : data) {
            TableRow row = new TableRow(getContext());
            for (String cell : rowData) {
                TextView tv = new TextView(getContext());
                tv.setText(cell);
                tv.setPadding(16, 16, 16, 16);
                row.addView(tv);
            }
            tableComparison.addView(row);
        }
    }

    private void updateCharts() {
        boolean[] selection = new boolean[heads.length];
        if (activeFilter != null) {
            for (int i = 0; i < heads.length; i++) {
                if (heads[i].equalsIgnoreCase(activeFilter)) {
                    selection[i] = true;
                    break;
                }
            }
        }
        updateWeeklyChart(selection);
        updateMonthlyChart(selection);
        updateComparisonChart();
    }

    private void updateWeeklyChart(boolean[] selection) {
        chartWeekly.setData(getDataForSelection(selection, 7));
    }

    private void updateMonthlyChart(boolean[] selection) {
        chartMonthly.setData(getDataForSelection(selection, 12));
    }

    private List<List<Float>> getDataForSelection(boolean[] selection, int pointsCount) {
        List<List<Float>> results = new ArrayList<>();
        boolean anySelected = false;
        
        // Handle "All" selection
        int allIndex = -1;
        for(int i=0; i<heads.length; i++) {
            if(heads[i].equalsIgnoreCase("All")) allIndex = i;
        }
        
        if (allIndex != -1 && selection[allIndex]) {
            for (int i = 0; i < heads.length; i++) {
                if (!heads[i].equalsIgnoreCase("All")) {
                    results.add(generateRandomData(pointsCount));
                }
            }
            return results;
        }

        for (int i = 0; i < selection.length; i++) {
            if (selection[i]) {
                results.add(generateRandomData(pointsCount));
                anySelected = true;
            }
        }
        
        // Default if nothing selected
        if (!anySelected) {
            results.add(generateRandomData(pointsCount));
        }
        
        return results;
    }

    private void updateComparisonChart() {
        List<List<Float>> comparisonData = new ArrayList<>();
        comparisonData.add(generateRandomData(10));
        chartComparison.setData(comparisonData);
    }

    private List<Float> generateRandomData(int count) {
        List<Float> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add((float) (Math.random() * 100));
        }
        return data;
    }
}
