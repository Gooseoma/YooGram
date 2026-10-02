package org.telegram.yoogram;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;

/** Settings > YooGram: the hub with one entry per tab. */
public class YooGramSettingsActivity extends YooSettingsPage {

    @Override
    protected CharSequence getPageTitle() {
        return LocaleController.getString(R.string.YooGram);
    }

    @Override
    protected void buildRows(ArrayList<Row> rows) {
        rows.add(Row.nav(LocaleController.getString(R.string.YooGeneral), R.drawable.msg_settings, YooGeneralSettings::new));
        rows.add(Row.nav(LocaleController.getString(R.string.YooAppearance), R.drawable.msg_theme, YooAppearanceSettings::new));
        rows.add(Row.nav(LocaleController.getString(R.string.YooChats), R.drawable.msg_discussion, YooChatsSettings::new));
        rows.add(Row.nav(LocaleController.getString(R.string.YooOther), R.drawable.msg_info, YooOtherSettings::new));
        rows.add(Row.shadow());
    }
}
