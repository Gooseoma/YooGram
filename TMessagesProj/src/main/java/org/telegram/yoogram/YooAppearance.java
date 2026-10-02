package org.telegram.yoogram;

import android.text.TextUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.utils.settings.SharedSettings;

public final class YooAppearance {

    private YooAppearance() {
    }

    /** Text shown in the chat list header, depending on the "Header text" setting. */
    public static String headerTitle(int account) {
        final int mode = SharedSettings.yooHeaderText.get();
        if (mode == 1) {
            return LocaleController.getString(R.string.YooHeaderChats);
        }
        final TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
        if (user != null) {
            switch (mode) {
                case 2:
                    if (!TextUtils.isEmpty(user.first_name)) {
                        return user.first_name;
                    }
                    break;
                case 3: {
                    final String username = UserObject.getPublicUsername(user);
                    if (!TextUtils.isEmpty(username)) {
                        return "@" + username;
                    }
                    break;
                }
                case 4: {
                    final String name = UserObject.getUserName(user);
                    if (!TextUtils.isEmpty(name)) {
                        return name;
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return YooBranding.NAME;
    }
}
