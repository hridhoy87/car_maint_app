package hobby.asad.mushad.mycar;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class AdActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private MaterialButton btnSkip;
    private ProgressBar progressBar;
    private CountDownTimer mainTimer;
    private Intent targetIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        tvCountdown = findViewById(R.id.tv_countdown);
        btnSkip = findViewById(R.id.btn_skip_ad);
        progressBar = findViewById(R.id.ad_timer_progress);

        // Retrieve original intent to pass through
        targetIntent = getIntent().getParcelableExtra("TARGET_INTENT");
        if (targetIntent == null) {
            targetIntent = new Intent(this, StatisticsActivity.class);
        }

        startAdSequence();

        btnSkip.setOnClickListener(v -> finishAd());

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to disable back button during ad
            }
        });
    }

    private void startAdSequence() {
        mainTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                progressBar.setProgress(secondsRemaining);
                tvCountdown.setText(getString(R.string.ad_remaining_format, secondsRemaining));

                if (secondsRemaining <= 20) { // 30 - 10 = 20 seconds remaining means 10 seconds have passed
                    btnSkip.setEnabled(true);
                    btnSkip.setText(R.string.ad_skip_now);
                } else {
                    btnSkip.setText(getString(R.string.ad_skip_format, secondsRemaining - 20));
                }
            }

            @Override
            public void onFinish() {
                finishAd();
            }
        }.start();
    }

    private void finishAd() {
        if (mainTimer != null) mainTimer.cancel();
        startActivity(targetIntent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
