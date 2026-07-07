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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class StatisticsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView drawerCarInitial;
    private TextView drawerCarName;
    private TextView drawerCarOdo;
    private TextView toolbarCarInitial;
    private TextView toolbarOdoDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);

        String savedLang = prefs.getString("app_language", "en");
        setLocale(savedLang);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.drawer_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else if (id == R.id.drawer_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            } else if (id == R.id.drawer_about) {
                Toast.makeText(this, "MY CAR - CLASSIC PERFORMANCE v1.0", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_route) {
                startActivity(new Intent(this, RouteActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_dollar) {
                startActivity(new Intent(this, DollarActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_statistics) {
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
                
                if (cars.length == 1) {
                    Toast.makeText(StatisticsActivity.this, "Only one vehicle available", Toast.LENGTH_SHORT).show();
                }

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

    private void setLocale(String languageCode) {
        java.util.Locale locale = new java.util.Locale(languageCode);
        java.util.Locale.setDefault(locale);
        android.content.res.Resources resources = getResources();
        android.content.res.Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }
}
