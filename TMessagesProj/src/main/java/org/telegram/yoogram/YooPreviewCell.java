package org.telegram.yoogram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.utils.settings.SharedSettings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.telegram.messenger.AndroidUtilities.dp;

/** Small chat preview: two sample bubbles that follow the current YooGram message settings. */
public class YooPreviewCell extends View {

    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path tail = new Path();

    public YooPreviewCell(Context context) {
        super(context);
        textPaint.setTextSize(dp(16));
        timePaint.setTextSize(dp(12));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(150));
    }

    private String time() {
        final String pattern = SharedSettings.yooTimeSeconds.get() ? "HH:mm:ss" : "HH:mm";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date());
    }

    private void bubble(Canvas canvas, boolean out, String text, String timeText, float top) {
        final float radius = dp(SharedConfig.bubbleRadius);
        final float textW = textPaint.measureText(text);
        final float timeW = timePaint.measureText(timeText);
        final float w = Math.max(textW, timeW) + dp(24);
        final float h = dp(52);
        final float margin = dp(14);
        final float left = out ? getMeasuredWidth() - margin - w : margin;
        rect.set(left, top, left + w, top + h);

        bubblePaint.setColor(Theme.getColor(out ? Theme.key_chat_outBubble : Theme.key_chat_inBubble));
        canvas.drawRoundRect(rect, radius, radius, bubblePaint);

        if (!SharedSettings.yooNoTail.get()) {
            final float tx = out ? rect.right : rect.left;
            final float dir = out ? 1 : -1;
            tail.reset();
            tail.moveTo(tx - dir * dp(8), rect.bottom - dp(10));
            tail.lineTo(tx + dir * dp(5), rect.bottom);
            tail.lineTo(tx - dir * dp(8), rect.bottom);
            tail.close();
            canvas.drawPath(tail, bubblePaint);
        }

        textPaint.setColor(Theme.getColor(out ? Theme.key_chat_messageTextOut : Theme.key_chat_messageTextIn));
        canvas.drawText(text, left + dp(12), top + dp(24), textPaint);
        timePaint.setColor(Theme.getColor(out ? Theme.key_chat_outTimeText : Theme.key_chat_inTimeText));
        canvas.drawText(timeText, rect.right - dp(12) - timeW, top + dp(42), timePaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Theme.getColor(Theme.key_windowBackgroundGray));
        bubble(canvas, false, LocaleController.getString(R.string.YooPreviewIn), time(), dp(14));
        bubble(canvas, true, LocaleController.getString(R.string.YooPreviewOut), time(), dp(80));
    }
}
