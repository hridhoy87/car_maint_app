package hobby.asad.mushad.mycar;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;

public class StatisticsActivity extends BaseActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_statistics;
    }

    @Override
    protected int getBottomNavId() {
        return R.id.nav_statistics;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        if (savedInstanceState == null) {
            StatisticsDashboardFragment fragment = new StatisticsDashboardFragment();
            if (getIntent().hasExtra("STAT_FILTER")) {
                Bundle args = new Bundle();
                args.putString("STAT_FILTER", getIntent().getStringExtra("STAT_FILTER"));
                fragment.setArguments(args);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("STAT_FILTER")) {
            StatisticsDashboardFragment fragment = new StatisticsDashboardFragment();
            Bundle args = new Bundle();
            args.putString("STAT_FILTER", intent.getStringExtra("STAT_FILTER"));
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}