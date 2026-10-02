package org.telegram.yoogram;

import android.content.SharedPreferences;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.utils.settings.SharedSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Keeps messages deleted by other people: the delete update is not applied locally,
 * the message id is only remembered so that the UI can mark it.
 */
public final class YooDeleted {

    private static final String PREF_KEY = "yoo_deleted_messages";
    private static final int MAX_STORED = 5000;

    private static HashSet<String> ids;

    private YooDeleted() {
    }

    public static boolean enabled() {
        return SharedSettings.yooKeepDeleted.get();
    }

    private static String key(int account, long dialogId, int messageId) {
        // Ids are global for private chats and basic groups, and per channel for channels/supergroups.
        return account + ":" + (dialogId < 0 ? dialogId : 0) + ":" + messageId;
    }

    private static synchronized HashSet<String> load() {
        if (ids == null) {
            ids = new HashSet<>();
            try {
                final SharedPreferences prefs = MessagesController.getGlobalMainSettings();
                final java.util.Set<String> stored = prefs.getStringSet(PREF_KEY, null);
                if (stored != null) {
                    ids.addAll(stored);
                }
            } catch (Exception ignore) {
            }
        }
        return ids;
    }

    public static synchronized void mark(int account, long dialogId, ArrayList<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        final HashSet<String> set = load();
        for (int i = 0; i < messageIds.size(); i++) {
            set.add(key(account, dialogId, messageIds.get(i)));
        }
        // Keep the stored set bounded.
        if (set.size() > MAX_STORED) {
            final Iterator<String> it = set.iterator();
            int toRemove = set.size() - MAX_STORED;
            while (toRemove-- > 0 && it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        try {
            MessagesController.getGlobalMainSettings().edit().putStringSet(PREF_KEY, new HashSet<>(set)).apply();
        } catch (Exception ignore) {
        }
    }

    public static synchronized boolean isDeleted(int account, long dialogId, int messageId) {
        if (!enabled() || messageId <= 0) {
            return false;
        }
        return load().contains(key(account, dialogId, messageId));
    }

    private static Boolean emojiOk;

    /** Prefix for the time label of a deleted message: wastebasket if the font has it, text otherwise. */
    public static String marker() {
        if (emojiOk == null) {
            boolean ok;
            try {
                ok = new android.graphics.Paint().hasGlyph("\uD83D\uDDD1");
            } catch (Throwable t) {
                ok = false;
            }
            emojiOk = ok;
        }
        return emojiOk ? "\uD83D\uDDD1 " : org.telegram.messenger.LocaleController.getString(R.string.YooDeletedLabel) + " ";
    }
}
