package hobby.asad.mushad.mycar;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.util.Locale;

public class RouteActivity extends BaseActivity {

    private WebView mapWebView;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean isPageLoaded = false;
    private Location lastPendingLocation = null;
    private boolean isDarkMode = false;
    private String currentStationType = "Filling Station";

    // Tracks if an external intent parameter is dominant over the spinner initial state
    private boolean isInitialIntentHandled = false;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_route;
    }

    @Override
    protected int getBottomNavId() {
        return R.id.nav_route;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BaseActivity has already invoked initCommonViews, setupNavigation, etc.
        isDarkMode = (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);

        // 1. Intercept any optional data passed down from BaseActivity's drawer actions
        handleIncomingIntent(getIntent());

        setupWebView();
        setupLocation();
        setupStationTypeSelector();
    }

    /**
     * Intercepts incoming target bundle queries securely
     */
    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("SEARCH_QUERY")) {
            String query = intent.getStringExtra("SEARCH_QUERY");
            if (query != null && !query.trim().isEmpty()) {
                currentStationType = query;
                isInitialIntentHandled = true;
            }
        }
    }

    /**
     * Re-route parameters gracefully if user fires a drawer option while RouteActivity is active
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);

        if (isInitialIntentHandled) {
            isPageLoaded = false;
            if (mapWebView != null) {
                mapWebView.loadUrl("file:///android_asset/route_map.html");
            }
            syncSpinnerSelectionWithCurrentType();
        }
    }

    private void setupWebView() {
        mapWebView = findViewById(R.id.map_webview);
        if (mapWebView == null) return;

        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        if (isDarkMode) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON);
            }
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, true);
            }
        }

        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                isPageLoaded = true;

                mapWebView.evaluateJavascript("setTheme(" + isDarkMode + ")", null);

                if (lastPendingLocation != null) {
                    updateMapLocation(lastPendingLocation.getLatitude(), lastPendingLocation.getLongitude());
                } else {
                    if (locationManager != null && ActivityCompat.checkSelfPermission(RouteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnown != null) {
                            updateMapLocation(lastKnown.getLatitude(), lastKnown.getLongitude());
                        }
                    }
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
                return false;
            }
        });

        mapWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
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
                Toast.makeText(this, R.string.error_location_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupStationTypeSelector() {
        Spinner stationSelector = findViewById(R.id.station_type_selector);
        if (stationSelector == null) return;

        String[] stationTypes = {
                getString(R.string.station_filling),
                getString(R.string.station_gas),
                getString(R.string.station_lpg),
                getString(R.string.station_cng)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, stationTypes);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        stationSelector.setAdapter(adapter);

        if (isInitialIntentHandled) {
            syncSpinnerSelectionWithCurrentType();
        }

        stationSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newType = stationTypes[position];

                if (isInitialIntentHandled) {
                    isInitialIntentHandled = false;
                    return;
                }

                if (!newType.equals(currentStationType)) {
                    currentStationType = newType;
                    isPageLoaded = false;
                    mapWebView.loadUrl("file:///android_asset/route_map.html");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void syncSpinnerSelectionWithCurrentType() {
        Spinner stationSelector = findViewById(R.id.station_type_selector);
        if (stationSelector == null || stationSelector.getAdapter() == null) return;

        boolean found = false;
        for (int i = 0; i < stationSelector.getAdapter().getCount(); i++) {
            if (stationSelector.getAdapter().getItem(i).toString().equalsIgnoreCase(currentStationType)) {
                stationSelector.setSelection(i);
                found = true;
                break;
            }
        }

        // If not found in standard spinner (e.g., "ATM", "Hotels"), 
        // we can optionally clear selection or add it, but per requirement 
        // not to hamper design, we just keep current state. 
        // The search logic is already dominant.
    }
}