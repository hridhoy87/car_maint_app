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
    private Spinner maintenanceSpinner;
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
            textAmountUnit.setText("L");
        } else {
            if ("fr".equalsIgnoreCase(lang)) textAmountUnit.setText("EUR");
            else if ("ar".equalsIgnoreCase(lang)) textAmountUnit.setText("SAR");
            else textAmountUnit.setText("BDT");
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
                "Engine Oil", "Break Fluid", "Transmission Fluid", 
                "Tire Replacement", "Breakpad Replacement", 
                "Clutch Plate Replacement", "Wiper Replacement"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, maintenanceTypes);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        maintenanceSpinner.setAdapter(adapter);

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
        String veh = carSelector.getSelectedItem().toString();
        String odoOld = toolbarOdoDisplay.getText().toString().replaceAll("[^0-9]", "");
        String odoNew = editOdoIntake.getText().toString();
        String fuelType = fuelSpinner.getSelectedItem().toString();
        String pricePerL = editFuelPrice.getText().toString();
        String inputVal = editAmount.getText().toString();

        if (odoNew.isEmpty() || inputVal.isEmpty() || pricePerL.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amountL;
        double fuelPrice;
        
        double pricePerLVal = parseDoubleSafe(pricePerL);
        double inputValNum = parseDoubleSafe(inputVal);

        if (pricePerLVal <= 0 || inputValNum <= 0) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!switchUnit.isChecked()) {
            amountL = inputValNum;
            fuelPrice = amountL * pricePerLVal;
        } else {
            fuelPrice = inputValNum;
            amountL = fuelPrice / pricePerLVal;
        }

        String msg = String.format(Locale.US,
                "Veh: %s\nODO_old: %s\nODO_new: %s\nFuelType: %s\nAmount: %.2f L\nFuelPrice: %.2f\nFuelPricePerL: %s",
                veh, odoOld, odoNew, fuelType, amountL, fuelPrice, pricePerL);

        new AlertDialog.Builder(this)
                .setTitle("Fuel Intake Details")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleSaveMaintenance() {
        String veh = carSelector.getSelectedItem() != null ? carSelector.getSelectedItem().toString() : "";
        String odoOld = toolbarOdoDisplay != null ? toolbarOdoDisplay.getText().toString().replaceAll("[^0-9]", "") : "";
        String odoNew = editMaintenanceOdo.getText().toString();
        String type = maintenanceSpinner.getSelectedItem() != null ? maintenanceSpinner.getSelectedItem().toString() : "";
        String amount = editMaintenanceAmount.getText().toString();
        String shop = editShopName.getText().toString();

        if (odoNew.isEmpty() || amount.isEmpty() || shop.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (parseDoubleSafe(amount) <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String msg = String.format(Locale.US,
                "Veh: %s\nODO_old: %s\nODO_new: %s\nMaintenance: %s\nAmount: %s\nShop: %s",
                veh, odoOld, odoNew, type, amount, shop);

        new AlertDialog.Builder(this)
                .setTitle("Maintenance Details")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleSaveRepair() {
        String veh = carSelector.getSelectedItem() != null ? carSelector.getSelectedItem().toString() : "";
        String odoOld = toolbarOdoDisplay != null ? toolbarOdoDisplay.getText().toString().replaceAll("[^0-9]", "") : "";
        String odoNew = editRepairOdo.getText().toString();
        String title = editRepairTitle.getText().toString();
        String amount = editRepairAmount.getText().toString();
        String shop = editRepairShopName.getText().toString();

        if (odoNew.isEmpty() || title.isEmpty() || amount.isEmpty() || shop.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (parseDoubleSafe(amount) <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String msg = String.format(Locale.US,
                "Veh: %s\nODO_old: %s\nODO_new: %s\nRepair: %s\nAmount: %s\nShop: %s",
                veh, odoOld, odoNew, title, amount, shop);

        new AlertDialog.Builder(this)
                .setTitle("Abnormal Repair Details")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleSaveEnhancement() {
        String veh = carSelector.getSelectedItem() != null ? carSelector.getSelectedItem().toString() : "";
        String odoOld = toolbarOdoDisplay != null ? toolbarOdoDisplay.getText().toString().replaceAll("[^0-9]", "") : "";
        String odoNew = editEnhancementOdo.getText().toString();
        String title = editEnhancementTitle.getText().toString();
        String amount = editEnhancementAmount.getText().toString();
        String shop = editEnhancementShopName.getText().toString();

        if (odoNew.isEmpty() || title.isEmpty() || amount.isEmpty() || shop.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (parseDoubleSafe(amount) <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String msg = String.format(Locale.US,
                "Veh: %s\nODO_old: %s\nODO_new: %s\nEnhancement: %s\nAmount: %s\nShop: %s",
                veh, odoOld, odoNew, title, amount, shop);

        new AlertDialog.Builder(this)
                .setTitle("Enhancement Details")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
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