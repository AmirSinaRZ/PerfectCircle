package sina.PerfectCircle;

// Cr : AmirSina Razghandi 28 jul 2024


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CircleDrawView extends View {
    private Paint paint;
    private float centerX, centerY;
    private List<Point> points;
    private boolean isDrawing;
    private final float refRadius = 250;  // شعاع مرجع
    private OnSimilarityCalculatedListener listener;

    private static class Point {
        float x, y;
        int color;

        Point(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    public interface OnSimilarityCalculatedListener {
        void onSimilarityCalculated(float similarity, boolean isClosed);
    }

    public CircleDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        points = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (centerX == 0 && centerY == 0) {
            centerX = getWidth() / 2;
            centerY = getHeight() / 2;
        }

        // رسم دایره مرجع
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(centerX, centerY, refRadius, paint);

        // رسم نقطه مرکزی
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 10, paint);

        // رسم دایره کاربر با رنگ متغیر
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < points.size() - 1; i++) {
            paint.setColor(points.get(i).color);
            canvas.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDrawing = true;
                points.clear();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDrawing) {
                    float x = event.getX();
                    float y = event.getY();
                    int color = calculateColor(x, y);
                    points.add(new Point(x, y, color));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                isDrawing = false;
                // محاسبه درصد شباهت
                boolean isClosed = isShapeClosed();
                float similarity = calculateSimilarity(isClosed);
                if (listener != null) {
                    listener.onSimilarityCalculated(similarity, isClosed);
                }
                showSimilarityDialog(similarity, isClosed);
                break;
        }
        return true;
    }

    private int calculateColor(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float diff = Math.abs(distance - refRadius);

        // تعیین رنگ بر اساس درصد خطا
        if (diff < 20) { // خطا کمتر از 10 درصد
            return Color.GREEN;
        } else if (diff < 40) { // خطا بین 10 تا 20 درصد
            return Color.BLUE;
        } else if (diff < 60) { // خطا بین 20 تا 30 درصد
            return Color.YELLOW;
        } else if (diff < 80) { // خطا بین 30 تا 40 درصد
            return Color.rgb(255, 165, 0); // نارنجی
        } else { // خطا بیشتر از 40 درصد
            return Color.RED;
        }
    }

    private float calculateSimilarity(boolean isClosed) {
        if (points.size() < 50 || !isClosed) { // اگر نقاط کمتر از 50 باشد، یا شکل بسته نباشد
            return 0;
        }

        float sumRadius = 0;
        int count = points.size();
        float maxDiff = 0;
        for (Point point : points) {
            float dx = point.x - centerX;
            float dy = point.y - centerY;
            float radius = (float) Math.sqrt(dx * dx + dy * dy);
            sumRadius += radius;

            // محاسبه بیشترین اختلاف شعاع با شعاع مرجع
            float diff = Math.abs(radius - refRadius);
            if (diff > maxDiff) {
                maxDiff = diff;
            }
        }
        float avgRadius = sumRadius / count;

        float difference = Math.abs(avgRadius - refRadius);

        // اگر اختلاف متوسط شعاع بیش از یک مقدار معین بود، درصد شباهت را کاهش می‌دهیم
        float similarity = Math.max(0, 100 - (difference / refRadius * 100));

        // اگر اختلاف بیشترین شعاع با شعاع مرجع بیش از مقدار معین باشد، درصد شباهت را بیشتر کاهش می‌دهیم
        if (maxDiff > refRadius * 0.4) {
            similarity *= 0.5;
        }

        return similarity;
    }

    private boolean isShapeClosed() {
        if (points.size() < 10) return false;

        Point end = points.get(points.size() - 1);

        for (int i = 0; i < points.size() - 10; i++) {
            Point point = points.get(i);
            float dx = end.x - point.x;
            float dy = end.y - point.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 20) { // اگر فاصله کمتر از 20 پیکسل باشد، شکل بسته است
                return true;
            }
        }

        return false; // شکل بسته نیست
    }

    private void showSimilarityDialog(float similarity, boolean isClosed) {
        String message;
        if (!isClosed) {
            message = "شکل بسته نیست!";
        } else {
            message = "درصد شباهت: " + similarity + "%";
        }

       /* new android.app.AlertDialog.Builder(getContext())
                .setTitle("نتیجه")
                .setMessage(message)
                .setPositiveButton("باشه", null)
                .show(); */
    }

    public void setOnSimilarityCalculatedListener(OnSimilarityCalculatedListener listener) {
        this.listener = listener;
    }
}
