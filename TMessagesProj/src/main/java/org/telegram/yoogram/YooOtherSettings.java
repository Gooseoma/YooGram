package org.telegram.yoogram;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;

import java.util.ArrayList;

public class YooOtherSettings extends YooSettingsPage {

    public static final String GITHUB_URL = "https://github.com/Gooseoma/M-YooGram";

    @Override
    protected CharSequence getPageTitle() {
        return LocaleController.getString(R.string.YooOther);
    }

    @Override
    protected void buildRows(ArrayList<Row> rows) {
        rows.add(Row.header(LocaleController.getString(R.string.YooSecSupport)));
        rows.add(Row.action(LocaleController.getString(R.string.YooGithub), R.drawable.msg_link, () -> {
            if (getParentActivity() != null) {
                Browser.openUrl(getParentActivity(), GITHUB_URL);
            }
        }));
        rows.add(Row.info(GITHUB_URL));
    }
}
