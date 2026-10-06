package org.telegram.yoogram;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.utils.settings.BooleanSetting;
import org.telegram.utils.settings.IntSetting;

import java.util.ArrayList;

/** Declarative settings page: subclasses only list their rows. */
public abstract class YooSettingsPage extends BaseFragment {

    protected static final int TYPE_HEADER = 0;
    protected static final int TYPE_SWITCH = 1;
    protected static final int TYPE_CHOICE = 2;
    protected static final int TYPE_INFO = 3;
    protected static final int TYPE_NAV = 4;
    protected static final int TYPE_SHADOW = 5;
    protected static final int TYPE_ACTION = 6;
    protected static final int TYPE_PREVIEW = 7;

    public interface Action {
        void run();
    }

    public interface FragmentFactory {
        BaseFragment create();
    }

    public interface BoolGetter {
        boolean get();
    }

    public interface BoolSetter {
        void set(boolean value);
    }

    protected static class Row {
        int type;
        CharSequence title;
        CharSequence value;
        int icon;
        BooleanSetting boolSetting;
        BoolGetter boolGetter;
        BoolSetter boolSetter;
        IntSetting intSetting;
        CharSequence[] options;
        int[] optionValues;
        boolean restartHint;
        FragmentFactory factory;
        Action action;

        static Row header(CharSequence title) {
            Row r = new Row();
            r.type = TYPE_HEADER;
            r.title = title;
            return r;
        }

        static Row info(CharSequence text) {
            Row r = new Row();
            r.type = TYPE_INFO;
            r.title = text;
            return r;
        }

        static Row preview() {
            Row r = new Row();
            r.type = TYPE_PREVIEW;
            return r;
        }

        static Row shadow() {
            Row r = new Row();
            r.type = TYPE_SHADOW;
            return r;
        }

        static Row toggle(CharSequence title, BooleanSetting setting) {
            Row r = new Row();
            r.type = TYPE_SWITCH;
            r.title = title;
            r.boolSetting = setting;
            return r;
        }

        /** Same as {@link #toggle}, but shows "restart the app" after changing. */
        static Row toggleRestart(CharSequence title, BooleanSetting setting) {
            Row r = toggle(title, setting);
            r.restartHint = true;
            return r;
        }

        static Row toggleCustom(CharSequence title, BoolGetter getter, BoolSetter setter, boolean restartHint) {
            Row r = new Row();
            r.type = TYPE_SWITCH;
            r.title = title;
            r.boolGetter = getter;
            r.boolSetter = setter;
            r.restartHint = restartHint;
            return r;
        }

        static Row choice(CharSequence title, IntSetting setting, CharSequence[] options, int[] optionValues) {
            Row r = new Row();
            r.type = TYPE_CHOICE;
            r.title = title;
            r.intSetting = setting;
            r.options = options;
            r.optionValues = optionValues;
            return r;
        }

        static Row nav(CharSequence title, int icon, FragmentFactory factory) {
            Row r = new Row();
            r.type = TYPE_NAV;
            r.title = title;
            r.icon = icon;
            r.factory = factory;
            return r;
        }

        static Row action(CharSequence title, int icon, Action action) {
            Row r = new Row();
            r.type = TYPE_ACTION;
            r.title = title;
            r.icon = icon;
            r.action = action;
            return r;
        }
    }

    private final ArrayList<Row> rows = new ArrayList<>();
    private RecyclerListView listView;
    private ListAdapter adapter;

    protected abstract CharSequence getPageTitle();

    protected abstract void buildRows(ArrayList<Row> rows);

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getPageTitle());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        rows.clear();
        buildRows(rows);

        listView = new RecyclerListView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position) -> onRowClicked(view, position));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    private void onRowClicked(View view, int position) {
        if (position < 0 || position >= rows.size()) {
            return;
        }
        final Row row = rows.get(position);
        switch (row.type) {
            case TYPE_SWITCH: {
                final boolean value = !rowValue(row);
                if (row.boolSetting != null) {
                    row.boolSetting.set(value);
                } else {
                    row.boolSetter.set(value);
                }
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(value);
                }
                refreshPreviews();
                applyLive();
                if (row.restartHint) {
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, org.telegram.messenger.LocaleController.getString(R.string.YooRestartHint)).show();
                }
                break;
            }
            case TYPE_CHOICE: {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(row.title);
                builder.setItems(row.options, (dialog, which) -> {
                    row.intSetting.set(row.optionValues[which]);
                    applyLive();
                    adapter.notifyItemChanged(position);
                    refreshPreviews();
                });
                showDialog(builder.create());
                break;
            }
            case TYPE_NAV:
                presentFragment(row.factory.create());
                break;
            case TYPE_ACTION:
                row.action.run();
                break;
            default:
                break;
        }
    }

    /** Pushes the changed settings into screens that are already open (chat list). */
    private void applyLive() {
        if (parentLayout == null) {
            return;
        }
        for (BaseFragment fragment : parentLayout.getFragmentStack()) {
            if (fragment instanceof org.telegram.ui.DialogsActivity) {
                ((org.telegram.ui.DialogsActivity) fragment).yooApplySettings();
            }
        }
    }

    private void refreshPreviews() {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).type == TYPE_PREVIEW) {
                adapter.notifyItemChanged(i);
            }
        }
    }

    private static boolean rowValue(Row row) {
        return row.boolSetting != null ? row.boolSetting.get() : row.boolGetter.get();
    }

    private CharSequence currentOptionLabel(Row row) {
        final int current = row.intSetting.get();
        for (int i = 0; i < row.optionValues.length; i++) {
            if (row.optionValues[i] == current) {
                return row.options[i];
            }
        }
        return row.options[0];
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).type;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            final int type = holder.getItemViewType();
            return type == TYPE_SWITCH || type == TYPE_CHOICE || type == TYPE_NAV || type == TYPE_ACTION;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case TYPE_HEADER:
                    view = new HeaderCell(context);
                    break;
                case TYPE_SWITCH:
                    view = new TextCheckCell(context);
                    break;
                case TYPE_CHOICE:
                    view = new TextSettingsCell(context);
                    break;
                case TYPE_NAV:
                case TYPE_ACTION:
                    view = new TextCell(context);
                    break;
                case TYPE_SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case TYPE_PREVIEW:
                    view = new YooPreviewCell(context);
                    break;
                default:
                    view = new TextInfoPrivacyCell(context);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final Row row = rows.get(position);
            switch (row.type) {
                case TYPE_HEADER:
                    ((HeaderCell) holder.itemView).setText(row.title);
                    break;
                case TYPE_SWITCH:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(row.title, rowValue(row), false);
                    break;
                case TYPE_CHOICE:
                    ((TextSettingsCell) holder.itemView).setTextAndValue(row.title, currentOptionLabel(row), false);
                    break;
                case TYPE_NAV:
                case TYPE_ACTION:
                    ((TextCell) holder.itemView).setTextAndIcon(row.title, row.icon, false);
                    break;
                case TYPE_INFO:
                    ((TextInfoPrivacyCell) holder.itemView).setText(row.title);
                    break;
                default:
                    break;
            }
        }
    }
}
