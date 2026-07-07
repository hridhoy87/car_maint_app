package hobby.asad.mushad.mycar;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import android.widget.AdapterView;
import android.content.ActivityNotFoundException;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class RouteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView drawerCarInitial;
    private TextView drawerCarName;
    private TextView drawerCarOdo;
    private TextView toolbarCarInitial;
    private TextView toolbarOdoDisplay;
    private WebView mapWebView;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean isPageLoaded = false;
    private Location lastPendingLocation = null;
    private boolean isDarkMode = false;
    private String currentStationType = "Filling Station";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);

        String savedLang = prefs.getString("app_language", "en");
        setLocale(savedLang);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        
        isDarkMode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);

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

        setupWebView();
        setupLocation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setupCarSelector();
        setupStationTypeSelector();

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

        bottomNavigationView.setSelectedItemId(R.id.nav_route);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_route) {
                return true;
            } else if (id == R.id.nav_dollar) {
                startActivity(new Intent(this, DollarActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_statistics) {
                startActivity(new Intent(this, StatisticsActivity.class));
                finish();
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

    private void setupWebView() {
        mapWebView = findViewById(R.id.map_webview);
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // Force Dark Mode for Web Content if App is in Dark Mode
        if (isDarkMode) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON);
            }
            // Enable algorithmic darkening for better results on modern devices
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, true);
            }
        }

        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoaded = true;
                
                // Inject theme status into JS
                mapWebView.evaluateJavascript("setTheme(" + isDarkMode + ")", null);

                if (lastPendingLocation != null) {
                    updateMapLocation(lastPendingLocation.getLatitude(), lastPendingLocation.getLongitude());
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) return false;

                if (url.startsWith("geo:") || url.startsWith("intent:") || url.startsWith("maps:")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent != null) {
                            view.stopLoading();
                            PackageManager packageManager = getPackageManager();
                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent);
                                return true;
                            }
                            // Fallback for intent: urls that might be app-specific
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            if (fallbackUrl != null) {
                                view.loadUrl(fallbackUrl);
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MyCar_WebView", "Error parsing intent: " + url, e);
                    }
                }
                
                // Keep standard https urls inside the webview unless they are explicit map links
                return false;
            }
        });

        mapWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Grant geolocation permission to the WebView for the requested origin
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("MyCar_WebView", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return true;
            }
        });

        mapWebView.loadUrl("file:///android_asset/route_map.html");
    }

    private void setupLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    updateMapLocation(location.getLatitude(), location.getLongitude());
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(@NonNull String provider) {}
                @Override public void onProviderDisabled(@NonNull String provider) {}
            });

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown != null) {
                updateMapLocation(lastKnown.getLatitude(), lastKnown.getLongitude());
            }
        }
    }

    private void updateMapLocation(double lat, double lon) {
        if (mapWebView != null) {
            if (isPageLoaded) {
                mapWebView.evaluateJavascript("updateLocation(" + lat + "," + lon + ", '" + currentStationType + "')", null);
                lastPendingLocation = null;
            } else {
                lastPendingLocation = new Location("");
                lastPendingLocation.setLatitude(lat);
                lastPendingLocation.setLongitude(lon);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupStationTypeSelector() {
        Spinner stationSelector = findViewById(R.id.station_type_selector);
        String[] stationTypes = {
                getString(R.string.station_filling),
                getString(R.string.station_gas),
                getString(R.string.station_lpg),
                getString(R.string.station_cng)
        };

        // Use the same custom spinner items as the car selector
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, stationTypes);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        stationSelector.setAdapter(adapter);

        stationSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newType = stationTypes[position];
                if (!newType.equals(currentStationType)) {
                    currentStationType = newType;
                    // Reset page loaded status and reload the base HTML to trigger fresh updateLocation
                    isPageLoaded = false;
                    mapWebView.loadUrl("file:///android_asset/route_map.html");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
                String odoValue = "ODO: " + (12345 + (position * 5000)) + " km";
                
                if (cars.length == 1) {
                    Toast.makeText(RouteActivity.this, "Only one vehicle available", Toast.LENGTH_SHORT).show();
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
