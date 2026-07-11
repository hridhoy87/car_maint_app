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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import hobby.asad.mushad.mycar.database.Vehicle;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected BottomNavigationView bottomNavigationView;
    protected Toolbar toolbar;

    protected TextView drawerCarInitial, drawerCarName, drawerCarReg, drawerCarOdo;
    protected TextView toolbarCarInitial, toolbarOdoDisplay;
    protected Spinner carSelector;
    protected AdView adView;
    protected java.util.List<hobby.asad.mushad.mycar.database.Vehicle> vehiclesList = new java.util.ArrayList<>();

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
            drawerCarReg = headerView.findViewById(R.id.drawer_car_reg);
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
                else if (id == R.id.drawer_about) Toast.makeText(this, R.string.about_toast, Toast.LENGTH_SHORT).show();
                else if (id == R.id.drawer_filling_station) navigateToRouteWithQuery(getString(R.string.station_filling));
                else if (id == R.id.drawer_cng) navigateToRouteWithQuery(getString(R.string.station_cng));
                else if (id == R.id.drawer_lpg) navigateToRouteWithQuery(getString(R.string.station_lpg));
                else if (id == R.id.drawer_restaurants) navigateToRouteWithQuery(getString(R.string.nav_restaurants_label));
                else if (id == R.id.drawer_hotels) navigateToRouteWithQuery(getString(R.string.nav_hotels_label));
                else if (id == R.id.drawer_atm) navigateToRouteWithQuery(getString(R.string.nav_atm_label));
                else if (id == R.id.drawer_mosque) navigateToRouteWithQuery(getString(R.string.nav_mosque_label));
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
                    Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
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
        
        Intent intent;
        if (cls == StatisticsActivity.class) {
            intent = new Intent(this, AdActivity.class);
            Intent target = new Intent(this, StatisticsActivity.class);
            intent.putExtra("TARGET_INTENT", target);
        } else {
            intent = new Intent(this, cls);
        }
        
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
        Intent target = new Intent(this, StatisticsActivity.class);
        target.putExtra("STAT_FILTER", filter);
        if (this.getClass() == StatisticsActivity.class) {
            target.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        Intent intent = new Intent(this, AdActivity.class);
        intent.putExtra("TARGET_INTENT", target);

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        if (this.getClass() != MainActivity.class && this.getClass() != StatisticsActivity.class) {
            finish();
        }
    }

    protected void setupCarSelector() {
        new Thread(() -> {
            hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(this);
            java.util.List<hobby.asad.mushad.mycar.database.Vehicle> vehicles = db.vehicleDao().getAllVehicles();
            
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                this.vehiclesList = vehicles;
                
                ArrayAdapter<Vehicle> adapter = new ArrayAdapter<Vehicle>(this, R.layout.spinner_item, android.R.id.text1, vehicles) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        return createItemView(position, convertView, parent, false);
                    }

                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        return createItemView(position, convertView, parent, true);
                    }

                    private View createItemView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
                        View view = convertView;
                        if (view == null) {
                            int layout = isDropDown ? R.layout.spinner_dropdown_item : R.layout.spinner_item;
                            view = LayoutInflater.from(getContext()).inflate(layout, parent, false);
                        }
                        TextView tv = view.findViewById(android.R.id.text1);
                        Vehicle v = getItem(position);
                        if (v != null) {
                            String model = v.model != null ? v.model : getString(R.string.unknown_model);
                            String reg = v.registrationNumber != null ? v.registrationNumber : "";
                            
                            SpannableStringBuilder ssb = new SpannableStringBuilder(model);
                            if (!reg.isEmpty()) {
                                ssb.append("\n").append(reg);
                                ssb.setSpan(new AbsoluteSizeSpan(10, true), model.length(), ssb.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ssb.setSpan(new ForegroundColorSpan(androidx.core.content.ContextCompat.getColor(BaseActivity.this, R.color.accentColor)), model.length(), ssb.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            tv.setText(ssb);
                        } else {
                            tv.setText(getString(R.string.no_vehicles_available));
                        }
                        return view;
                    }
                };

                carSelector.setAdapter(adapter);

                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                int lastSelected = prefs.getInt("selected_car_index", 0);
                if (lastSelected >= vehicles.size()) lastSelected = 0;
                carSelector.setSelection(lastSelected);

                carSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    private boolean isFirstSelection = true;

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        prefs.edit().putInt("selected_car_index", position).apply();
                        Vehicle selectedVehicle = vehicles.get(position);
                        String selectedCar = selectedVehicle.model;
                        String reg = selectedVehicle.registrationNumber;
                        int odo = selectedVehicle.currentOdometer;
                        
                        prefs.edit().putString("selected_car_reg", reg).apply();

                        updateCarUI(selectedCar, reg, odo);

                        if (isFirstSelection) {
                            isFirstSelection = false;
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                                onCarSelected(selectedCar, position));
                        } else {
                            onCarSelected(selectedCar, position);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        }).start();
    }

    private void updateCarUI(String selectedCar, String reg, int odo) {
        if (getString(R.string.no_vehicles_available).equals(selectedCar)) {
            if (toolbarCarInitial != null) toolbarCarInitial.setText("-");
            if (toolbarOdoDisplay != null) toolbarOdoDisplay.setText(getString(R.string.odo_format, 0));
            if (drawerCarInitial != null) drawerCarInitial.setText("-");
            if (drawerCarName != null) drawerCarName.setText(R.string.no_vehicle);
            if (drawerCarReg != null) drawerCarReg.setText("");
            if (drawerCarOdo != null) drawerCarOdo.setText(getString(R.string.odo_format, 0));
            return;
        }
        if (toolbarCarInitial != null) toolbarCarInitial.setText(String.valueOf(selectedCar.charAt(0)));
        if (toolbarOdoDisplay != null) toolbarOdoDisplay.setText(getString(R.string.odo_format, odo));
        if (drawerCarInitial != null) drawerCarInitial.setText(String.valueOf(selectedCar.charAt(0)));
        if (drawerCarName != null) drawerCarName.setText(selectedCar);
        if (drawerCarReg != null) drawerCarReg.setText(reg);
        if (drawerCarOdo != null) drawerCarOdo.setText(getString(R.string.odo_format, odo));
    }

    protected void onCarSelected(String carName, int position) {}

    public void updateVehicleOdo(String vehicleId, int newOdo) {
        for (int i = 0; i < vehiclesList.size(); i++) {
            hobby.asad.mushad.mycar.database.Vehicle v = vehiclesList.get(i);
            if (v.id.equals(vehicleId)) {
                if (newOdo > v.currentOdometer) {
                    v.currentOdometer = newOdo;
                    if (carSelector != null && carSelector.getSelectedItemPosition() == i) {
                        updateCarUI(v.model, v.registrationNumber, newOdo);
                    }
                }
                break;
            }
        }
    }

    protected String getSelectedVehicleId() {
        if (carSelector != null && !vehiclesList.isEmpty()) {
            int position = carSelector.getSelectedItemPosition();
            if (position >= 0 && position < vehiclesList.size()) {
                return vehiclesList.get(position).id;
            }
        }
        return null;
    }

    protected void setupBannerSwipe(View banner) {
        if (banner == null) return;
        
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams params = 
            (androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) banner.getLayoutParams();
            
        com.google.android.material.behavior.SwipeDismissBehavior<View> swipe = 
            new com.google.android.material.behavior.SwipeDismissBehavior<>();
            
        swipe.setSwipeDirection(com.google.android.material.behavior.SwipeDismissBehavior.SWIPE_DIRECTION_ANY);
        
        swipe.setListener(new com.google.android.material.behavior.SwipeDismissBehavior.OnDismissListener() {
            @Override
            public void onDismiss(View view) {
                banner.setVisibility(View.GONE);
                // Reset alpha and translation for next time it might be shown
                banner.setAlpha(1f);
                banner.setTranslationX(0f);
            }

            @Override
            public void onDragStateChanged(int state) {}
        });
        
        params.setBehavior(swipe);
    }

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