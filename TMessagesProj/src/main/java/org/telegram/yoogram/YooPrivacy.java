package org.telegram.yoogram;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.utils.settings.SharedSettings;

public final class YooPrivacy {

    private YooPrivacy() {
    }

    /** Phone number as it is shown on screen: replaced with a stub when the "hide phone" setting is on. */
    public static String phone(String formatted) {
        if (SharedSettings.yooHidePhone.get()) {
            return LocaleController.getString(R.string.YooHiddenPhone);
        }
        return formatted;
    }
}
