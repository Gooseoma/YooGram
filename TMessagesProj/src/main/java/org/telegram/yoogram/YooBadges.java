package org.telegram.yoogram;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.utils.settings.SharedSettings;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/** Custom profile badges: signed list downloaded from the badge server and drawn next to names. */
public class YooBadges {

    public static class Badge {
        public final boolean isEmoji;
        public final String text;
        public final int color;
        @Nullable
        public final String tooltip;

        Badge(boolean isEmoji, String text, int color, @Nullable String tooltip) {
            this.isEmoji = isEmoji;
            this.text = text;
            this.color = color;
            this.tooltip = tooltip;
        }
    }

    private static final long REFRESH_INTERVAL = 3 * 60 * 60 * 1000L;
    private static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;

    private static volatile HashMap<Long, Badge> badges;
    private static boolean loadedFromDisk;
    private static boolean loading;

    private YooBadges() {
    }

    /** Badges the user can give themselves in Settings > YooGram. Shown only on this device. */
    public static final Badge[] PRESETS = {
        new Badge(true, "\u2B50", 0xFFFFC107, null),
        new Badge(true, "\uD83D\uDD25", 0xFFFF5722, null),
        new Badge(true, "\uD83D\uDC8E", 0xFF03A9F4, null),
        new Badge(true, "\uD83D\uDC51", 0xFFFFB300, null),
        new Badge(true, "\uD83D\uDC31", 0xFF9C27B0, null),
        new Badge(false, "YooGram", 0xFF7C4DFF, null),
        new Badge(false, "Developer", 0xFF2A9DF4, null),
    };

    @Nullable
    public static Badge get(long userId) {
        if (userId <= 0) {
            return null;
        }
        ensureLoaded();
        HashMap<Long, Badge> map = badges;
        Badge badge = map != null ? map.get(userId) : null;
        // Badges come only from the signed server list.
        return badge;
    }

    private static synchronized void ensureLoaded() {
        if (loadedFromDisk) {
            return;
        }
        loadedFromDisk = true;
        String payload = SharedSettings.yooBadgesPayload.get();
        if (payload != null) {
            // The payload was verified before being stored.
            badges = parse(payload);
        }
    }

    /** Fetches the list if it is older than the refresh interval. Safe to call often. */
    public static void refreshIfNeeded() {
        if (TextUtils.isEmpty(YooConfig.BADGES_BASE_URL) || TextUtils.isEmpty(YooConfig.BADGES_PUBLIC_KEY)) {
            return;
        }
        ensureLoaded();
        synchronized (YooBadges.class) {
            if (loading || System.currentTimeMillis() - SharedSettings.yooBadgesLastUpdate.get() < REFRESH_INTERVAL) {
                return;
            }
            loading = true;
        }
        Utilities.globalQueue.postRunnable(() -> {
            try {
                fetch();
            } catch (Throwable e) {
                FileLog.e(e);
            } finally {
                synchronized (YooBadges.class) {
                    loading = false;
                }
            }
        });
    }

    private static void fetch() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(YooConfig.BADGES_BASE_URL + "/v1/badges").openConnection();
        try {
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("Accept", "application/json");
            String etag = SharedSettings.yooBadgesEtag.get();
            if (etag != null && SharedSettings.yooBadgesPayload.get() != null) {
                connection.setRequestProperty("If-None-Match", etag);
            }
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_NOT_MODIFIED) {
                SharedSettings.yooBadgesLastUpdate.set(System.currentTimeMillis());
                return;
            }
            if (code != HttpURLConnection.HTTP_OK) {
                return;
            }
            String body = readBody(connection.getInputStream());
            JSONObject envelope = new JSONObject(body);
            String payload = envelope.getString("payload");
            String sig = envelope.getString("sig");
            if (!verify(payload, sig)) {
                FileLog.e("YooBadges: bad signature, ignoring list");
                return;
            }
            JSONObject json = new JSONObject(payload);
            int version = json.optInt("version", 0);
            if (version < SharedSettings.yooBadgesVersion.get()) {
                FileLog.e("YooBadges: older version, ignoring list");
                return;
            }
            HashMap<Long, Badge> parsed = parse(payload);
            if (parsed == null) {
                return;
            }
            SharedSettings.yooBadgesPayload.set(payload);
            SharedSettings.yooBadgesVersion.set(version);
            SharedSettings.yooBadgesEtag.set(connection.getHeaderField("ETag"));
            SharedSettings.yooBadgesLastUpdate.set(System.currentTimeMillis());
            badges = parsed;
        } finally {
            connection.disconnect();
        }
    }

    private static String readBody(InputStream in) throws Exception {
        try (InputStream stream = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) > 0) {
                out.write(buffer, 0, read);
                if (out.size() > MAX_RESPONSE_BYTES) {
                    throw new Exception("badges response too large");
                }
            }
            return out.toString("UTF-8");
        }
    }

    public static boolean verify(String payload, String sigBase64) {
        try {
            byte[] key = Base64.decode(YooConfig.BADGES_PUBLIC_KEY, Base64.DEFAULT);
            byte[] sig = Base64.decode(sigBase64, Base64.DEFAULT);
            return Ed25519.verify(key, payload.getBytes(StandardCharsets.UTF_8), sig);
        } catch (Throwable e) {
            return false;
        }
    }

    @Nullable
    private static HashMap<Long, Badge> parse(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            JSONObject list = json.getJSONObject("badges");
            HashMap<Long, Badge> result = new HashMap<>();
            java.util.Iterator<String> keys = list.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    JSONObject item = list.getJSONObject(key);
                    boolean emoji = "emoji".equals(item.optString("kind"));
                    String text = item.optString(emoji ? "emoji" : "label", "");
                    if (text.isEmpty()) {
                        continue;
                    }
                    int color;
                    try {
                        color = Color.parseColor(item.optString("color", "#2a9df4"));
                    } catch (IllegalArgumentException e) {
                        color = 0xFF2A9DF4;
                    }
                    result.put(Long.parseLong(key), new Badge(emoji, text, color, item.has("tooltip") ? item.getString("tooltip") : null));
                } catch (Exception ignore) {
                }
            }
            return result;
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    /** Returns {@code name} followed by the user's badge, or {@code name} itself when there is none. */
    public static CharSequence appendBadge(CharSequence name, long userId) {
        Badge badge = get(userId);
        if (badge == null || name == null) {
            return name;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(name);
        builder.append(' ');
        builder.setSpan(new BadgeSpan(badge), builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    /**
     * Same as {@link #appendBadge} for text that is ellipsized to {@code maxWidth}:
     * the name is shortened first so the badge always stays visible.
     */
    public static CharSequence appendBadge(CharSequence name, long userId, Paint paint, float maxWidth) {
        Badge badge = get(userId);
        if (badge == null || name == null) {
            return TextUtils.ellipsize(name, new android.text.TextPaint(paint), maxWidth, TextUtils.TruncateAt.END);
        }
        BadgeSpan span = new BadgeSpan(badge);
        float badgeWidth = span.getSize(paint, " ", 0, 1, null);
        CharSequence cut = TextUtils.ellipsize(name, new android.text.TextPaint(paint), Math.max(0, maxWidth - badgeWidth), TextUtils.TruncateAt.END);
        SpannableStringBuilder builder = new SpannableStringBuilder(cut);
        builder.append(' ');
        builder.setSpan(span, builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private static class BadgeSpan extends ReplacementSpan {
        private final Badge badge;
        private final RectF rect = new RectF();

        BadgeSpan(Badge badge) {
            this.badge = badge;
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            return (int) Math.ceil(measure(paint)[0]);
        }

        /** Returns {width, textWidth}. */
        private float[] measure(Paint paint) {
            float size = paint.getTextSize();
            float gap = size * 0.2f;
            if (badge.isEmoji) {
                float w = paint.measureText(badge.text);
                return new float[]{w + gap, w};
            }
            Paint small = smallPaint(paint);
            float tw = small.measureText(badge.text);
            return new float[]{tw + size * 0.7f + gap, tw};
        }

        private Paint smallPaint(Paint base) {
            Paint p = new Paint(base);
            p.setTextSize(base.getTextSize() * 0.72f);
            p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            return p;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
            float size = paint.getTextSize();
            float gap = size * 0.2f;
            if (badge.isEmoji) {
                int oldColor = paint.getColor();
                paint.setColor(Color.WHITE);
                canvas.drawText(badge.text, x + gap, y, paint);
                paint.setColor(oldColor);
                return;
            }
            Paint small = smallPaint(paint);
            float tw = small.measureText(badge.text);
            Paint.FontMetrics fm = small.getFontMetrics();
            float h = (fm.descent - fm.ascent) + size * 0.15f;
            float cy = y - size * 0.3f;
            rect.set(x + gap, cy - h / 2f, x + gap + tw + size * 0.7f, cy + h / 2f);
            Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
            bg.setColor(badge.color);
            canvas.drawRoundRect(rect, h / 2f, h / 2f, bg);
            small.setColor(isLight(badge.color) ? Color.BLACK : Color.WHITE);
            canvas.drawText(badge.text, rect.left + size * 0.35f, cy - (fm.ascent + fm.descent) / 2f, small);
        }

        private static boolean isLight(int color) {
            return (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) > 170;
        }
    }
}
