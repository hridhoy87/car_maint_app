package hobby.asad.mushad.mycar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView drawerCarInitial;
    private TextView drawerCarName;
    private TextView drawerCarOdo;
    private TextView toolbarCarInitial;
    private TextView toolbarOdoDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Theme and Language
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
        
        String savedLang = prefs.getString("app_language", "en");
        setLocale(savedLang);

        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Immersive Fullscreen Mode
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        View headerView = navigationView.getHeaderView(0);
        drawerCarInitial = headerView.findViewById(R.id.drawer_car_initial);
        drawerCarName = headerView.findViewById(R.id.drawer_car_name);
        drawerCarOdo = headerView.findViewById(R.id.drawer_car_odo);
        
        toolbarCarInitial = findViewById(R.id.car_initial);
        toolbarOdoDisplay = findViewById(R.id.odo_display);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setupCarSelector();
        setupFuelSelector();
        setupCurrencyIcon(savedLang);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.drawer_home) {
            } else if (id == R.id.drawer_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.drawer_about) {
                Toast.makeText(this, "MY CAR - CLASSIC PERFORMANCE v1.0", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_route) {
                startActivity(new Intent(this, RouteActivity.class));
                return true;
            } else if (id == R.id.nav_dollar) {
                startActivity(new Intent(this, DollarActivity.class));
                return true;
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            v.setPadding(0, 0, 0, 0);
            return insets;
        });
    }

    private void setLocale(String languageCode) {
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Resources resources = getResources();
        android.content.res.Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void setupCurrencyIcon(String lang) {
        MaterialSwitch switchUnit = findViewById(R.id.switch_unit);
        if (switchUnit == null) return;

        // Determine which icon to use based on language
        int currencyIconRes;
        if ("fr".equalsIgnoreCase(lang)) {
            currencyIconRes = R.drawable.ic_unit_euro;
        } else if ("ar".equalsIgnoreCase(lang)) {
            currencyIconRes = R.drawable.ic_unit_riyal;
        } else {
            // Default for en, bn, and others
            currencyIconRes = R.drawable.ic_unit_bdt;
        }

        // Create a new selector or update the existing one programmatically
        // Since we want different icons for the same checked state based on logic,
        // we swap the thumb icon drawable of the switch.
        
        android.graphics.drawable.StateListDrawable stateListDrawable = new android.graphics.drawable.StateListDrawable();
        
        // Checked state
        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, 
                ContextCompat.getDrawable(this, currencyIconRes));
        
        // Unchecked state (Litre)
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, 
                ContextCompat.getDrawable(this, R.drawable.ic_unit_litre));

        switchUnit.setThumbIconDrawable(stateListDrawable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure Home is always highlighted when we return to this screen
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        
        // Refresh currency icon in case language changed in settings
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedLang = prefs.getString("app_language", "en");
        setupCurrencyIcon(savedLang);
    }

    private void setupFuelSelector() {
        Spinner fuelSpinner = findViewById(R.id.spinner_fuel_type);
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
    }

    private void setupCarSelector() {
        Spinner carSelector = findViewById(R.id.car_selector);
        String[] cars = {"TESLA MODEL S", "PORSCHE 911", "BMW M4", "AUDI RS6"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, cars);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        carSelector.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int lastSelected = prefs.getInt("selected_car_index", 0);
        carSelector.setSelection(lastSelected);

        carSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("selected_car_index", position).apply();

                String selectedCar = cars[position];
                
                if (!selectedCar.isEmpty()) {
                    toolbarCarInitial.setText(String.valueOf(selectedCar.charAt(0)));
                    toolbarOdoDisplay.setText(getString(R.string.odo_format, (12345 + (position * 5000))));
                }
                
                if (drawerCarInitial != null) drawerCarInitial.setText(String.valueOf(selectedCar.charAt(0)));
                if (drawerCarName != null) drawerCarName.setText(selectedCar);
                if (drawerCarOdo != null) drawerCarOdo.setText(getString(R.string.odo_format, (12345 + (position * 5000))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
