package hobby.asad.mushad.mycar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int getBottomNavId() {
        return 0; // Not in bottom nav
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (bottomNavigationView != null) {
            bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
            for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                bottomNavigationView.getMenu().getItem(i).setChecked(false);
            }
            bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
        }

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        setupThemeSelector(prefs);
        setupLanguageSelector(prefs);
    }

    private void setupThemeSelector(SharedPreferences prefs) {
        RadioGroup themeGroup = findViewById(R.id.theme_radio_group);
        int savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (savedMode == AppCompatDelegate.MODE_NIGHT_NO) themeGroup.check(R.id.radio_light);
        else if (savedMode == AppCompatDelegate.MODE_NIGHT_YES) themeGroup.check(R.id.radio_dark);
        else themeGroup.check(R.id.radio_system);

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.radio_light) mode = AppCompatDelegate.MODE_NIGHT_NO;
            else if (checkedId == R.id.radio_dark) mode = AppCompatDelegate.MODE_NIGHT_YES;
            else mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            prefs.edit().putInt("theme_mode", mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void setupLanguageSelector(SharedPreferences prefs) {
        RadioGroup langGroup = findViewById(R.id.language_radio_group);
        String savedLang = prefs.getString("app_language", "en");

        if ("bn".equals(savedLang)) langGroup.check(R.id.lang_bn);
        else if ("ar".equals(savedLang)) langGroup.check(R.id.lang_ar);
        else if ("fr".equals(savedLang)) langGroup.check(R.id.lang_fr);
        else langGroup.check(R.id.lang_en);

        langGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang;
            if (checkedId == R.id.lang_bn) lang = "bn";
            else if (checkedId == R.id.lang_ar) lang = "ar";
            else if (checkedId == R.id.lang_fr) lang = "fr";
            else lang = "en";

            prefs.edit().putString("app_language", lang).apply();
            setLocale(lang);
            recreate();
        });
    }
}