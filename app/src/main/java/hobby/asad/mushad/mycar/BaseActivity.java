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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
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

import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected BottomNavigationView bottomNavigationView;
    protected Toolbar toolbar;

    protected TextView drawerCarInitial, drawerCarName, drawerCarOdo;
    protected TextView toolbarCarInitial, toolbarOdoDisplay;
    protected Spinner carSelector;
    protected AdView adView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);

        String savedLang = prefs.getString("app_language", "en");
        setLocale(savedLang);

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        MobileAds.initialize(this, initializationStatus -> {});

        initCommonViews();
        setupNavigation();
        setupFullscreen();
        setupAdView();
    }

    private void setupAdView() {
        adView = findViewById(R.id.ad_view);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

    @LayoutRes
    protected abstract int getLayoutResId();

    private void initCommonViews() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            drawerCarInitial = headerView.findViewById(R.id.drawer_car_initial);
            drawerCarName = headerView.findViewById(R.id.drawer_car_name);
            drawerCarOdo = headerView.findViewById(R.id.drawer_car_odo);
        }

        toolbarCarInitial = findViewById(R.id.car_initial);
        toolbarOdoDisplay = findViewById(R.id.odo_display);
        carSelector = findViewById(R.id.car_selector);

        if (carSelector != null) {
            setupCarSelector();
        }
    }

    private void setupNavigation() {
        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.drawer_home) navigateTo(MainActivity.class);
                else if (id == R.id.drawer_route) navigateTo(RouteActivity.class);
                else if (id == R.id.drawer_expenditure) navigateTo(DollarActivity.class);
                else if (id == R.id.drawer_statistics) navigateTo(StatisticsActivity.class);
                else if (id == R.id.drawer_reminder) navigateTo(ReminderActivity.class);
                else if (id == R.id.drawer_settings) navigateTo(SettingsActivity.class);
                else if (id == R.id.drawer_add_vehicle) VehicleUtils.showAddVehicleDialog(this);
                else if (id == R.id.drawer_about) Toast.makeText(this, "MY CAR - CLASSIC PERFORMANCE v1.0", Toast.LENGTH_SHORT).show();
                else if (id == R.id.drawer_filling_station) navigateToRouteWithQuery(getString(R.string.station_filling));
                else if (id == R.id.drawer_cng) navigateToRouteWithQuery(getString(R.string.station_cng));
                else if (id == R.id.drawer_lpg) navigateToRouteWithQuery(getString(R.string.station_lpg));
                else if (id == R.id.drawer_restaurants) navigateToRouteWithQuery("Restaurants");
                else if (id == R.id.drawer_hotels) navigateToRouteWithQuery("Hotels");
                else if (id == R.id.drawer_atm) navigateToRouteWithQuery("ATM");
                else if (id == R.id.drawer_mosque) navigateToRouteWithQuery("Mosque");
                else if (id == R.id.drawer_fuel) navigateToStatisticsWithFilter(getString(R.string.nav_fuel));
                else if (id == R.id.drawer_toll) navigateToStatisticsWithFilter(getString(R.string.nav_toll));
                else if (id == R.id.drawer_maintenance) navigateToStatisticsWithFilter(getString(R.string.nav_maintenance));
                else if (id == R.id.drawer_repair) navigateToStatisticsWithFilter(getString(R.string.nav_repair));
                else if (id == R.id.drawer_beautification) navigateToStatisticsWithFilter(getString(R.string.nav_beautification));
                else if (id == R.id.drawer_itemized_expenditure) {
                    toggleSubMenu(R.id.drawer_fuel, R.id.drawer_toll, R.id.drawer_maintenance, R.id.drawer_repair, R.id.drawer_beautification);
                    return true;
                } else if (id == R.id.drawer_nearby) {
                    toggleSubMenu(R.id.drawer_filling_station, R.id.drawer_cng, R.id.drawer_lpg, R.id.drawer_restaurants, R.id.drawer_hotels, R.id.drawer_atm, R.id.drawer_mosque);
                    return true;
                } else if (id == R.id.drawer_add_trip || id == R.id.drawer_backup_restore) {
                    Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                }
                
                if (drawerLayout != null) {
                    // Only close drawer if it's not a submenu item that was clicked (optional, but usually submenus expand)
                    // For NavigationView with submenus, clicking the parent item usually expands it.
                    // If we want to close drawer after clicking a leaf item, we need logic.
                    // Standard behavior is parent items with <menu> don't trigger listener for themselves usually if not checkable.
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        if (bottomNavigationView != null) {
            int selectedId = getBottomNavId();
            if (selectedId != 0) {
                bottomNavigationView.setSelectedItemId(selectedId);
            }
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == selectedId) return true;
                
                if (id == R.id.nav_home) navigateTo(MainActivity.class);
                else if (id == R.id.nav_route) navigateTo(RouteActivity.class);
                else if (id == R.id.nav_dollar) navigateTo(DollarActivity.class);
                else if (id == R.id.nav_statistics) navigateTo(StatisticsActivity.class);
                return true;
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    protected abstract int getBottomNavId();

    protected void navigateTo(Class<?> cls) {
        if (this.getClass() == cls) return;
        Intent intent = new Intent(this, cls);
        if (cls == MainActivity.class) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        if (cls != MainActivity.class) finish();
    }

//    protected void navigateToRouteWithQuery(String query) {
//        Intent intent = new Intent(this, RouteActivity.class);
//        intent.putExtra("SEARCH_QUERY", query);
//        if (this instanceof RouteActivity) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        }
//        startActivity(intent);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//        if (!(this instanceof MainActivity) && !(this instanceof RouteActivity)) finish();
//    }
    protected void navigateToRouteWithQuery(String query) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra("SEARCH_QUERY", query);

        // SAFE TYPE CHECKING: Compares Class references directly to avoid typecasting errors
        if (this.getClass() == RouteActivity.class) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (this.getClass() != MainActivity.class && this.getClass() != RouteActivity.class) {
            finish();
        }
    }

    protected void navigateToStatisticsWithFilter(String filter) {
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra("STAT_FILTER", filter);
        if (this.getClass() == StatisticsActivity.class) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        if (this.getClass() != MainActivity.class && this.getClass() != StatisticsActivity.class) {
            finish();
        }
    }

    private void setupCarSelector() {
        String[] cars = {"TESLA MODEL S", "PORSCHE 911", "BMW M4", "AUDI RS6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, cars);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        carSelector.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int lastSelected = prefs.getInt("selected_car_index", 0);
        carSelector.setSelection(lastSelected);

        carSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("selected_car_index", position).apply();
                String selectedCar = cars[position];
                updateCarUI(selectedCar, position);

                if (isFirstSelection) {
                    isFirstSelection = false;
                    // Defer initial trigger to ensure child activities are fully ready
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                        onCarSelected(selectedCar, position));
                } else {
                    onCarSelected(selectedCar, position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateCarUI(String selectedCar, int position) {
        if (toolbarCarInitial != null) toolbarCarInitial.setText(String.valueOf(selectedCar.charAt(0)));
        if (toolbarOdoDisplay != null) toolbarOdoDisplay.setText(getString(R.string.odo_format, (12345 + (position * 5000))));
        if (drawerCarInitial != null) drawerCarInitial.setText(String.valueOf(selectedCar.charAt(0)));
        if (drawerCarName != null) drawerCarName.setText(selectedCar);
        if (drawerCarOdo != null) drawerCarOdo.setText(getString(R.string.odo_format, (12345 + (position * 5000))));
    }

    protected void onCarSelected(String carName, int position) {}

    private void toggleSubMenu(int... ids) {
        if (navigationView == null) return;
        android.view.Menu menu = navigationView.getMenu();
        for (int id : ids) {
            android.view.MenuItem item = menu.findItem(id);
            if (item != null) {
                item.setVisible(!item.isVisible());
            }
        }
    }

    protected void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        android.content.res.Resources resources = getResources();
        android.content.res.Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void setupFullscreen() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onSupportNavigateUp();
    }
}