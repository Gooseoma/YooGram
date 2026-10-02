package org.telegram.yoogram;

import android.os.Build;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.utils.settings.SharedSettings;

import java.util.ArrayList;

public class YooAppearanceSettings extends YooSettingsPage {

    @Override
    protected CharSequence getPageTitle() {
        return LocaleController.getString(R.string.YooAppearance);
    }

    @Override
    protected void buildRows(ArrayList<Row> rows) {
        rows.add(Row.header(LocaleController.getString(R.string.YooSecChatList)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooForceSnow), SharedSettings.yooForceSnow));
        rows.add(Row.toggleRestart(LocaleController.getString(R.string.YooHeaderCenter), SharedSettings.yooHeaderCenter));
        rows.add(Row.toggleRestart(LocaleController.getString(R.string.YooHideStories), SharedSettings.yooHideStories));
        rows.add(Row.choice(
            LocaleController.getString(R.string.YooHeaderText),
            SharedSettings.yooHeaderText,
            new CharSequence[]{
                LocaleController.getString(R.string.YooHeaderApp),
                LocaleController.getString(R.string.YooHeaderChats),
                LocaleController.getString(R.string.YooHeaderTag),
                LocaleController.getString(R.string.YooHeaderFullName)
            },
            new int[]{0, 1, 3, 4}));
        rows.add(Row.shadow());

        rows.add(Row.header(LocaleController.getString(R.string.YooSecInterface)));
        if (Build.VERSION.SDK_INT >= 33) {
            rows.add(Row.toggleRestart(LocaleController.getString(R.string.YooLiquidGlass), SharedSettings.yooForceGlass));
            rows.add(Row.info(LocaleController.getString(R.string.YooLiquidGlassInfo)));
        }
        rows.add(Row.toggleCustom(
            LocaleController.getString(R.string.YooSystemEmoji),
            () -> SharedConfig.useSystemEmoji,
            SharedConfig::setUseSystemEmoji,
            true));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            rows.add(Row.toggleCustom(
                LocaleController.getString(R.string.YooSystemFont),
                () -> SharedConfig.useSystemBoldFont,
                value -> {
                    if (SharedConfig.useSystemBoldFont != value) {
                        SharedConfig.toggleUseSystemBoldFont();
                    }
                },
                true));
        }
        rows.add(Row.info(LocaleController.getString(R.string.YooRestartInfo)));
    }
}
