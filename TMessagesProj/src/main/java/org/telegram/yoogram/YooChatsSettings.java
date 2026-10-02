package org.telegram.yoogram;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.utils.settings.SharedSettings;

import java.util.ArrayList;

public class YooChatsSettings extends YooSettingsPage {

    @Override
    protected CharSequence getPageTitle() {
        return LocaleController.getString(R.string.YooChats);
    }

    @Override
    protected void buildRows(ArrayList<Row> rows) {
        rows.add(Row.header(LocaleController.getString(R.string.YooSecChat)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooQuickModeration), SharedSettings.yooQuickModeration));
        rows.add(Row.info(LocaleController.getString(R.string.YooQuickModerationInfo)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooHideGreeting), SharedSettings.yooHideGreeting));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooCommaAfterMention), SharedSettings.yooCommaAfterMention));
        rows.add(Row.shadow());

        rows.add(Row.header(LocaleController.getString(R.string.YooSecMessages)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooEditedIcon), SharedSettings.yooEditedIcon));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooHideStickerTime), SharedSettings.yooHideStickerTime));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooNoTail), SharedSettings.yooNoTail));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooNoReplyEmoji), SharedSettings.yooNoReplyEmoji));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooNoReplyColors), SharedSettings.yooNoReplyColors));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooOnlineDot), SharedSettings.yooOnlineDot));
        rows.add(Row.choice(
            LocaleController.getString(R.string.YooStickerSize),
            SharedSettings.yooStickerSize,
            new CharSequence[]{"8", "10", "12", "14", "16", "18", "20"},
            new int[]{8, 10, 12, 14, 16, 18, 20}));
        rows.add(Row.shadow());

        rows.add(Row.header(LocaleController.getString(R.string.YooSecPhoto)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooAlwaysHd), SharedSettings.yooAlwaysHd));
        rows.add(Row.shadow());

        rows.add(Row.header(LocaleController.getString(R.string.YooSecVideo)));
        rows.add(Row.choice(
            LocaleController.getString(R.string.YooSeekSeconds),
            SharedSettings.yooSeekSeconds,
            new CharSequence[]{
                LocaleController.getString(R.string.YooSeek5),
                LocaleController.getString(R.string.YooSeek10),
                LocaleController.getString(R.string.YooSeek20)
            },
            new int[]{5, 10, 20}));
    }
}
