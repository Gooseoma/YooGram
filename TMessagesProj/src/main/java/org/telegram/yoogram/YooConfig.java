package org.telegram.yoogram;

public final class YooConfig {

    /** Badge server address without trailing slash, e.g. "https://badges.example.com". Empty disables badges. */
    public static final String BADGES_BASE_URL = "https://api-tg.gooseoma.ru";

    /** Ed25519 public key (32 bytes, base64) shown at the bottom of the badge server admin page. */
    public static final String BADGES_PUBLIC_KEY = "2osxSbQGkDvrMiHUMDoMlQ0t0fE9vOyxS1dYbS+KbpE=";

    /** Where the "Update app" buttons lead (instead of the official store pages). */
    public static final String UPDATE_URL = "https://t.me/gooseoma_news";

    private YooConfig() {
    }
}
