package org.telegram.yoogram;

public final class YooConfig {

    /** Badge server address without trailing slash, e.g. "https://badges.example.com". Empty disables badges. */
    public static final String BADGES_BASE_URL = "";

    /** Ed25519 public key (32 bytes, base64) shown at the bottom of the badge server admin page. */
    public static final String BADGES_PUBLIC_KEY = "";

    private YooConfig() {
    }
}
