package org.telegram.yoogram;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.utils.settings.SharedSettings;

import java.util.ArrayList;

public class YooGeneralSettings extends YooSettingsPage {

    @Override
    protected CharSequence getPageTitle() {
        return LocaleController.getString(R.string.YooGeneral);
    }

    @Override
    protected void buildRows(ArrayList<Row> rows) {
        rows.add(Row.header(LocaleController.getString(R.string.YooSecNumbersTime)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooNoNumberRounding), SharedSettings.yooNoNumberRounding));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooTimeSeconds), SharedSettings.yooTimeSeconds));
        rows.add(Row.info(LocaleController.getString(R.string.YooNumbersInfo)));

        rows.add(Row.toggle(LocaleController.getString(R.string.YooKeepDeleted), SharedSettings.yooKeepDeleted));
        rows.add(Row.info(LocaleController.getString(R.string.YooKeepDeletedInfo)));

        rows.add(Row.header(LocaleController.getString(R.string.YooSecTransfer)));
        rows.add(Row.choice(
            LocaleController.getString(R.string.YooDownloadBoost),
            SharedSettings.yooDownloadBoost,
            new CharSequence[]{
                LocaleController.getString(R.string.YooBoostOff),
                LocaleController.getString(R.string.YooBoostFast),
                LocaleController.getString(R.string.YooBoostUltra)
            },
            new int[]{0, 1, 2}));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooUploadBoost), SharedSettings.yooUploadBoost));
        rows.add(Row.info(LocaleController.getString(R.string.YooTransferInfo)));

        rows.add(Row.header(LocaleController.getString(R.string.YooSecProfile)));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooShowId), SharedSettings.yooShowId));
        rows.add(Row.toggle(LocaleController.getString(R.string.YooHidePhone), SharedSettings.yooHidePhone));
        rows.add(Row.info(LocaleController.getString(R.string.YooHidePhoneInfo)));
    }
}
