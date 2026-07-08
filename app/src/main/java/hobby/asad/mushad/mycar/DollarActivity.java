package hobby.asad.mushad.mycar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DollarActivity extends BaseActivity {

    private LinearLayout timelineContainer;
    private View loadingProgress;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_dollar;
    }

    @Override
    protected int getBottomNavId() {
        return R.id.nav_dollar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timelineContainer = findViewById(R.id.timeline_container);
        loadingProgress = findViewById(R.id.loading_progress);
    }

    @Override
    protected void onCarSelected(String carName, int position) {
        loadExpenditureData(carName);
    }

    private void loadExpenditureData(String carName) {
        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);
        if (timelineContainer != null) timelineContainer.removeAllViews();

        DataRepository.fetchExpenditureData(this, carName, new DataRepository.DataCallback() {
            @Override
            public void onSuccess(JSONObject data) {
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                try {
                    renderTimeline(data.getJSONArray("entries"));
                } catch (JSONException e) {
                    Toast.makeText(DollarActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                Toast.makeText(DollarActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderTimeline(JSONArray entries) throws JSONException {
        if (timelineContainer == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            String type = entry.getString("type");

            if ("single".equals(type)) {
                View cardView = inflater.inflate(R.layout.layout_expenditure_card, timelineContainer, false);
                ((TextView) cardView.findViewById(R.id.text_date)).setText(entry.getString("date"));
                ((TextView) cardView.findViewById(R.id.text_title)).setText(entry.getString("title"));
                ((TextView) cardView.findViewById(R.id.text_amount)).setText(getString(R.string.amount_label, entry.getString("amount")));
                timelineContainer.addView(cardView);
            } else if ("group".equals(type)) {
                View groupView = inflater.inflate(R.layout.layout_expenditure_group, timelineContainer, false);
                ((TextView) groupView.findViewById(R.id.text_date)).setText(entry.getString("date"));
                ((TextView) groupView.findViewById(R.id.text_title)).setText(entry.getString("title"));
                ((TextView) groupView.findViewById(R.id.text_summary)).setText(entry.getString("summary"));

                LinearLayout childrenContainer = groupView.findViewById(R.id.group_children_container);
                JSONArray children = entry.getJSONArray("children");
                for (int j = 0; j < children.length(); j++) {
                    JSONObject child = children.getJSONObject(j);
                    View childView = inflater.inflate(R.layout.layout_expenditure_child, childrenContainer, false);
                    ((TextView) childView.findViewById(R.id.text_child_title)).setText(child.getString("title"));
                    ((TextView) childView.findViewById(R.id.text_child_amount)).setText(child.getString("amount"));
                    childrenContainer.addView(childView);
                }

                View cardHeader = groupView.findViewById(R.id.card_header);
                ImageView expandIcon = groupView.findViewById(R.id.expand_icon);
                cardHeader.setOnClickListener(v -> {
                    if (childrenContainer.getVisibility() == View.GONE) {
                        childrenContainer.setVisibility(View.VISIBLE);
                        if (expandIcon != null) expandIcon.setRotation(180f);
                    } else {
                        childrenContainer.setVisibility(View.GONE);
                        if (expandIcon != null) expandIcon.setRotation(0f);
                    }
                });

                timelineContainer.addView(groupView);
            }
        }
    }
}