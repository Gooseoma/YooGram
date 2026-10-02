package org.telegram.yoogram;

import android.util.LruCache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Replaces the product name in UI strings coming from language packs. */
public final class YooBranding {

    public static final String NAME = "YooGram";

    // Links, e-mails and @handles stay untouched; everything else named "Telegram" is rebranded.
    private static final Pattern PATTERN = Pattern.compile(
        "(https?://[^\\s<>\"')\\]]+|tg://[^\\s<>\"')\\]]+|[\\w.+-]+@[\\w.-]+\\.\\w+|\\bt\\.me/[^\\s<>\"')\\]]*|[\\w.-]*telegram\\w*\\.(?:org|me|com)\\b[^\\s<>\"')\\]]*|@\\w*[Tt]elegram\\w*)|(Telegram|TELEGRAM|Телеграм|Телеграмм)");

    private static final LruCache<String, String> CACHE = new LruCache<>(1024);

    private YooBranding() {
    }

    /** The official service chat (777000) is shown under the app name. */
    public static String userName(org.telegram.tgnet.TLRPC.User user, String name) {
        return user != null && user.id == 777000 ? "YooGram" : name;
    }

    public static String fix(String value) {
        if (value == null || value.length() < 8) {
            return value;
        }
        if (value.indexOf("elegram") < 0 && value.indexOf("ELEGRAM") < 0 && value.indexOf("елеграм") < 0) {
            return value;
        }
        String cached;
        synchronized (CACHE) {
            cached = CACHE.get(value);
        }
        if (cached != null) {
            return cached;
        }
        final Matcher m = PATTERN.matcher(value);
        final StringBuffer sb = new StringBuffer(value.length());
        while (m.find()) {
            final String replacement;
            if (m.group(1) != null) {
                replacement = m.group(1);
            } else if ("TELEGRAM".equals(m.group(2))) {
                replacement = "YOOGRAM";
            } else {
                replacement = NAME;
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        final String result = sb.toString();
        synchronized (CACHE) {
            CACHE.put(value, result);
        }
        return result;
    }
}
