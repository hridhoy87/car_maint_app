package hobby.asad.mushad.mycar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

public class MainActivity extends BaseActivity {

    private EditText editAmount;
    private TextView textAmountUnit;
    private EditText editOdoIntake;
    private EditText editFuelPrice;
    private Spinner fuelSpinner;
    private MaterialSwitch switchUnit;

    // Maintenance Views
    private MultiSelectSpinner maintenanceSpinner;
    private EditText editMaintenanceAmount;
    private EditText editMaintenanceOdo;
    private EditText editShopName;

    // Repair Views
    private EditText editRepairTitle;
    private EditText editRepairAmount;
    private EditText editRepairOdo;
    private EditText editRepairShopName;

    // Enhancement Views
    private EditText editEnhancementTitle;
    private EditText editEnhancementAmount;
    private EditText editEnhancementOdo;
    private EditText editEnhancementShopName;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getBottomNavId() {
        return R.id.nav_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Fuel Views
        editAmount = findViewById(R.id.edit_amount);
        textAmountUnit = findViewById(R.id.text_amount_unit);
        editOdoIntake = findViewById(R.id.edit_odo_intake);
        editFuelPrice = findViewById(R.id.edit_fuel_price);
        fuelSpinner = findViewById(R.id.spinner_fuel_type);
        switchUnit = findViewById(R.id.switch_unit);

        // Maintenance Views
        maintenanceSpinner = findViewById(R.id.spinner_maintenance_type);
        editMaintenanceAmount = findViewById(R.id.edit_maintenance_amount);
        editMaintenanceOdo = findViewById(R.id.edit_maintenance_odo);
        editShopName = findViewById(R.id.edit_shop_name);

        // Repair Views
        editRepairTitle = findViewById(R.id.edit_repair_title);
        editRepairAmount = findViewById(R.id.edit_repair_amount);
        editRepairOdo = findViewById(R.id.edit_repair_odo);
        editRepairShopName = findViewById(R.id.edit_repair_shop_name);

        // Enhancement Views
        editEnhancementTitle = findViewById(R.id.edit_enhancement_title);
        editEnhancementAmount = findViewById(R.id.edit_enhancement_amount);
        editEnhancementOdo = findViewById(R.id.edit_enhancement_odo);
        editEnhancementShopName = findViewById(R.id.edit_enhancement_shop_name);

        setupFuelSelector();
        setupMaintenanceSelector();
        setupRepairSelector();
        setupEnhancementSelector();
        
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        setupCurrencyIcon(prefs.getString("app_language", "en"));
    }

    private void setupCurrencyIcon(String lang) {
        if (switchUnit == null) return;

        int currencyIconRes;
        if ("fr".equalsIgnoreCase(lang)) {
            currencyIconRes = R.drawable.ic_unit_euro;
        } else if ("ar".equalsIgnoreCase(lang)) {
            currencyIconRes = R.drawable.ic_unit_riyal;
        } else {
            currencyIconRes = R.drawable.ic_unit_bdt;
        }

        android.graphics.drawable.StateListDrawable stateListDrawable = new android.graphics.drawable.StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, 
                ContextCompat.getDrawable(this, currencyIconRes));
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, 
                ContextCompat.getDrawable(this, R.drawable.ic_unit_litre));

        switchUnit.setThumbIconDrawable(stateListDrawable);
        
        updateEditAmountUnit(switchUnit.isChecked(), lang);
    }

    private void updateEditAmountUnit(boolean isCurrency, String lang) {
        if (textAmountUnit == null) return;
        
        if (!isCurrency) {
            textAmountUnit.setText(R.string.unit_litre_short);
        } else {
            if ("fr".equalsIgnoreCase(lang)) textAmountUnit.setText(R.string.unit_eur);
            else if ("ar".equalsIgnoreCase(lang)) textAmountUnit.setText(R.string.unit_sar);
            else textAmountUnit.setText(R.string.unit_bdt);
        }
    }

    private void setupFuelSelector() {
        String[] fuelTypes = {
                getString(R.string.fuel_octane),
                getString(R.string.fuel_petrol),
                getString(R.string.fuel_diesel),
                getString(R.string.fuel_cng),
                getString(R.string.fuel_lpg)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, fuelTypes);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        fuelSpinner.setAdapter(adapter);

        loadFuelPriceFromCache(fuelTypes[0]);

        fuelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFuelPriceFromCache(fuelTypes[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        editFuelPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                saveFuelPriceToCache(fuelSpinner.getSelectedItem().toString(), s.toString());
            }
        });

        switchUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            String lang = prefs.getString("app_language", "en");
            updateEditAmountUnit(isChecked, lang);
            editAmount.setText("");
        });

        findViewById(R.id.btn_save_fuel).setOnClickListener(v -> handleSaveFuel());
    }

    private void setupMaintenanceSelector() {
        String[] maintenanceTypes = {
                getString(R.string.maint_engine_oil), getString(R.string.maint_brake_fluid), getString(R.string.maint_trans_fluid), 
                getString(R.string.maint_tire), getString(R.string.maint_brake_pad), 
                getString(R.string.maint_clutch), getString(R.string.maint_wiper), getString(R.string.maint_all)
        };

        maintenanceSpinner.setItems(maintenanceTypes);

        findViewById(R.id.btn_save_maintenance).setOnClickListener(v -> handleSaveMaintenance());
    }

    private void setupRepairSelector() {
        findViewById(R.id.btn_save_repair).setOnClickListener(v -> handleSaveRepair());
    }

    private void setupEnhancementSelector() {
        findViewById(R.id.btn_save_enhancement).setOnClickListener(v -> handleSaveEnhancement());
    }

    private void loadFuelPriceFromCache(String fuelType) {
        SharedPreferences prefs = getSharedPreferences("fuel_prices", MODE_PRIVATE);
        String price = prefs.getString(fuelType.toLowerCase(), "0.00");
        editFuelPrice.setText(price);
    }

    private void saveFuelPriceToCache(String fuelType, String price) {
        SharedPreferences prefs = getSharedPreferences("fuel_prices", MODE_PRIVATE);
        prefs.edit().putString(fuelType.toLowerCase(), price).apply();
    }

    private void handleSaveFuel() {
        String vehicleId = getSelectedVehicleId();
        if (vehicleId == null) {
            Toast.makeText(this, R.string.error_select_vehicle, Toast.LENGTH_SHORT).show();
            return;
        }

        String odoNew = editOdoIntake.getText().toString();
        String fuelType = fuelSpinner.getSelectedItem().toString();
        String pricePerL = editFuelPrice.getText().toString();
        String inputVal = editAmount.getText().toString();

        if (odoNew.isEmpty() || inputVal.isEmpty() || pricePerL.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePerLVal = parseDoubleSafe(pricePerL);
        double inputValNum = parseDoubleSafe(inputVal);

        if (pricePerLVal <= 0 || inputValNum <= 0) {
            Toast.makeText(this, R.string.error_valid_numeric, Toast.LENGTH_SHORT).show();
            return;
        }

        double amountL;
        double fuelPrice;

        if (!switchUnit.isChecked()) {
            amountL = inputValNum;
            fuelPrice = amountL * pricePerLVal;
        } else {
            fuelPrice = inputValNum;
            amountL = fuelPrice / pricePerLVal;
        }

        new Thread(() -> {
            try {
                hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(this);
                hobby.asad.mushad.mycar.database.Expenditure exp = new hobby.asad.mushad.mycar.database.Expenditure();
                exp.vehicleId = vehicleId;
                exp.category = "FUEL";
                exp.title = getString(R.string.fuel_title_format, fuelType);
                exp.amount = fuelPrice;
                exp.fuelQuantity = amountL;
                exp.fuelPricePerUnit = pricePerLVal;
                exp.odometer = Integer.parseInt(odoNew);
                exp.date = System.currentTimeMillis();

                db.expenditureDao().insert(exp);

                // Update vehicle odometer
                hobby.asad.mushad.mycar.database.Vehicle v = db.vehicleDao().getVehicleById(vehicleId);
                if (v != null && exp.odometer > v.currentOdometer) {
                    v.currentOdometer = exp.odometer;
                    db.vehicleDao().update(v);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.fuel_record_saved, Toast.LENGTH_SHORT).show();
                    editAmount.setText("");
                    editOdoIntake.setText("");
                    updateVehicleOdo(vehicleId, exp.odometer);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleSaveMaintenance() {
        String vehicleId = getSelectedVehicleId();
        if (vehicleId == null) {
            Toast.makeText(this, R.string.error_select_vehicle, Toast.LENGTH_SHORT).show();
            return;
        }

        String odoNew = editMaintenanceOdo.getText().toString();
        String type = maintenanceSpinner.getSelectedItemsFormatted();
        String amount = editMaintenanceAmount.getText().toString();
        String shop = editShopName.getText().toString();

        if (odoNew.isEmpty() || amount.isEmpty() || shop.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amountVal = parseDoubleSafe(amount);
        if (amountVal <= 0) {
            Toast.makeText(this, R.string.error_valid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(this);
                hobby.asad.mushad.mycar.database.Expenditure exp = new hobby.asad.mushad.mycar.database.Expenditure();
                exp.vehicleId = vehicleId;
                exp.category = "MAINTENANCE";
                exp.title = type;
                exp.amount = amountVal;
                exp.shopName = shop;
                exp.odometer = Integer.parseInt(odoNew);
                exp.date = System.currentTimeMillis();

                db.expenditureDao().insert(exp);

                // Update vehicle odometer
                hobby.asad.mushad.mycar.database.Vehicle v = db.vehicleDao().getVehicleById(vehicleId);
                if (v != null && exp.odometer > v.currentOdometer) {
                    v.currentOdometer = exp.odometer;
                    db.vehicleDao().update(v);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.maintenance_record_saved, Toast.LENGTH_SHORT).show();
                    editMaintenanceAmount.setText("");
                    editMaintenanceOdo.setText("");
                    editShopName.setText("");
                    updateVehicleOdo(vehicleId, exp.odometer);
                    checkAndShowReminderAlert(exp.title);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void checkAndShowReminderAlert(String title) {
        String type = null;
        String tLow = title.toLowerCase();
        if (tLow.contains("oil")) type = "OIL";
        else if (tLow.contains("transmission")) type = "TRANS";
        else if (tLow.contains("brake")) type = "BRAKE";

        if (type != null) {
            String finalType = type;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.set_reminder)
                    .setMessage(R.string.reminder_alert_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> showDatePickerForReminder(finalType, title))
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    }

    private void showDatePickerForReminder(String type, String title) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            java.util.Calendar selected = java.util.Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            long dueDate = selected.getTimeInMillis();

            java.util.List<Reminder> reminders = ReminderManager.loadReminders(this);
            for (Reminder r : reminders) {
                if (type.equals(r.getType())) {
                    r.setDueDate(dueDate);
                    r.setEnabled(true);
                    break;
                }
            }
            ReminderManager.saveReminders(this, reminders);
            Toast.makeText(this, getString(R.string.reminder_set_for, title), Toast.LENGTH_SHORT).show();
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void handleSaveRepair() {
        String vehicleId = getSelectedVehicleId();
        if (vehicleId == null) {
            Toast.makeText(this, R.string.error_select_vehicle, Toast.LENGTH_SHORT).show();
            return;
        }

        String odoNew = editRepairOdo.getText().toString();
        String title = editRepairTitle.getText().toString();
        String amount = editRepairAmount.getText().toString();
        String shop = editRepairShopName.getText().toString();

        if (odoNew.isEmpty() || title.isEmpty() || amount.isEmpty() || shop.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amountVal = parseDoubleSafe(amount);
        if (amountVal <= 0) {
            Toast.makeText(this, R.string.error_valid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(this);
                hobby.asad.mushad.mycar.database.Expenditure exp = new hobby.asad.mushad.mycar.database.Expenditure();
                exp.vehicleId = vehicleId;
                exp.category = "REPAIR";
                exp.title = title;
                exp.amount = amountVal;
                exp.shopName = shop;
                exp.odometer = Integer.parseInt(odoNew);
                exp.date = System.currentTimeMillis();

                db.expenditureDao().insert(exp);

                // Update vehicle odometer
                hobby.asad.mushad.mycar.database.Vehicle v = db.vehicleDao().getVehicleById(vehicleId);
                if (v != null && exp.odometer > v.currentOdometer) {
                    v.currentOdometer = exp.odometer;
                    db.vehicleDao().update(v);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.repair_record_saved, Toast.LENGTH_SHORT).show();
                    editRepairTitle.setText("");
                    editRepairAmount.setText("");
                    editRepairOdo.setText("");
                    editRepairShopName.setText("");
                    updateVehicleOdo(vehicleId, exp.odometer);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleSaveEnhancement() {
        String vehicleId = getSelectedVehicleId();
        if (vehicleId == null) {
            Toast.makeText(this, R.string.error_select_vehicle, Toast.LENGTH_SHORT).show();
            return;
        }

        String odoNew = editEnhancementOdo.getText().toString();
        String title = editEnhancementTitle.getText().toString();
        String amount = editEnhancementAmount.getText().toString();
        String shop = editEnhancementShopName.getText().toString();

        if (odoNew.isEmpty() || title.isEmpty() || amount.isEmpty() || shop.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double amountVal = parseDoubleSafe(amount);
        if (amountVal <= 0) {
            Toast.makeText(this, R.string.error_valid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(this);
                hobby.asad.mushad.mycar.database.Expenditure exp = new hobby.asad.mushad.mycar.database.Expenditure();
                exp.vehicleId = vehicleId;
                exp.category = "BEAUTIFICATION";
                exp.title = title;
                exp.amount = amountVal;
                exp.shopName = shop;
                exp.odometer = Integer.parseInt(odoNew);
                exp.date = System.currentTimeMillis();

                db.expenditureDao().insert(exp);

                // Update vehicle odometer
                hobby.asad.mushad.mycar.database.Vehicle v = db.vehicleDao().getVehicleById(vehicleId);
                if (v != null && exp.odometer > v.currentOdometer) {
                    v.currentOdometer = exp.odometer;
                    db.vehicleDao().update(v);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.enhancement_record_saved, Toast.LENGTH_SHORT).show();
                    editEnhancementTitle.setText("");
                    editEnhancementAmount.setText("");
                    editEnhancementOdo.setText("");
                    editEnhancementShopName.setText("");
                    updateVehicleOdo(vehicleId, exp.odometer);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private double parseDoubleSafe(String val) {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException | NullPointerException e) {
            return -1.0;
        }
    }
}