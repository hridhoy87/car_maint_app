package hobby.asad.mushad.mycar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SimpleLineChartView extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint gridPaint;
    private List<List<Float>> datasets = new ArrayList<>();
    private float maxValue = 100f;
    
    // Distinct colors for multiple curves
    private final int[] chartColors = {
        Color.parseColor("#D4AF37"), // Gold
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#F44336"), // Red
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#00BCD4")  // Cyan
    };

    public SimpleLineChartView(Context context) {
        super(context);
        init();
    }

    public SimpleLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);
    }

    public void setData(List<List<Float>> data) {
        this.datasets = data;
        maxValue = 0;
        for (List<Float> points : data) {
            for (Float p : points) {
                if (p > maxValue) maxValue = p;
            }
        }
        if (maxValue == 0) maxValue = 100f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datasets.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float padding = 40f;

        float graphWidth = width - 2 * padding;
        float graphHeight = height - 2 * padding;

        // Draw horizontal grid lines
        for (int i = 0; i <= 4; i++) {
            float y = padding + i * (graphHeight / 4);
            canvas.drawLine(padding, y, width - padding, y, gridPaint);
        }

        for (int d = 0; d < datasets.size(); d++) {
            List<Float> dataPoints = datasets.get(d);
            if (dataPoints.size() < 2) continue;

            int color = chartColors[d % chartColors.length];
            linePaint.setColor(color);
            pointPaint.setColor(color);

            float stepX = graphWidth / (dataPoints.size() - 1);
            Path path = new Path();

            for (int i = 0; i < dataPoints.size(); i++) {
                float x = padding + i * stepX;
                float normalizedY = dataPoints.get(i) / maxValue;
                float y = height - padding - (normalizedY * graphHeight);

                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    float prevX = padding + (i - 1) * stepX;
                    float prevY = height - padding - (dataPoints.get(i - 1) / maxValue * graphHeight);
                    path.cubicTo((prevX + x) / 2, prevY, (prevX + x) / 2, y, x, y);
                }
                canvas.drawCircle(x, y, 8f, pointPaint);
            }
            canvas.drawPath(path, linePaint);
        }
    }
}
