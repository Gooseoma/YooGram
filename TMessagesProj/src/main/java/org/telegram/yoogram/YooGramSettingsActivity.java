package org.telegram.yoogram;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;

import org.telegram.messenger.LiteMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.utils.settings.SharedSettings;

/** Settings > YooGram. */
public class YooGramSettingsActivity extends BaseFragment {

    private static final int ROW_HEADER = 0;
    private static final int ROW_QUICK_MODERATION = 1;
    private static final int ROW_INFO = 2;
    private static final int ROW_APPEARANCE_HEADER = 3;
    private static final int ROW_LIQUID_GLASS = 4;
    private static final int ROW_LIQUID_GLASS_INFO = 5;

    private RecyclerListView listView;
    private ListAdapter adapter;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.YooGram));
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

        listView = new RecyclerListView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position) -> {
            if (position == ROW_QUICK_MODERATION) {
                boolean value = !SharedSettings.yooQuickModeration.get();
                SharedSettings.yooQuickModeration.set(value);
                ((TextCheckCell) view).setChecked(value);
            } else if (position == ROW_LIQUID_GLASS) {
                boolean value = !LiteMode.isEnabledSetting(LiteMode.FLAG_LIQUID_GLASS);
                LiteMode.toggleFlag(LiteMode.FLAG_LIQUID_GLASS, value);
                ((TextCheckCell) view).setChecked(value);
            }
        });
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context context;

        private ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getItemCount() {
            return Build.VERSION.SDK_INT >= 33 ? 6 : 3;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            return position == ROW_QUICK_MODERATION || position == ROW_LIQUID_GLASS;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == ROW_HEADER || viewType == ROW_APPEARANCE_HEADER) {
                view = new HeaderCell(context);
            } else if (viewType == ROW_QUICK_MODERATION || viewType == ROW_LIQUID_GLASS) {
                view = new TextCheckCell(context);
            } else {
                view = new TextInfoPrivacyCell(context);
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position == ROW_HEADER) {
                ((HeaderCell) holder.itemView).setText(LocaleController.getString(R.string.YooModeration));
            } else if (position == ROW_APPEARANCE_HEADER) {
                ((HeaderCell) holder.itemView).setText(LocaleController.getString(R.string.YooAppearance));
            } else if (position == ROW_QUICK_MODERATION) {
                ((TextCheckCell) holder.itemView).setTextAndCheck(LocaleController.getString(R.string.YooQuickModeration), SharedSettings.yooQuickModeration.get(), false);
            } else if (position == ROW_LIQUID_GLASS) {
                ((TextCheckCell) holder.itemView).setTextAndCheck(LocaleController.getString(R.string.YooLiquidGlass), LiteMode.isEnabledSetting(LiteMode.FLAG_LIQUID_GLASS), false);
            } else if (position == ROW_LIQUID_GLASS_INFO) {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.YooLiquidGlassInfo));
            } else {
                ((TextInfoPrivacyCell) holder.itemView).setText(LocaleController.getString(R.string.YooQuickModerationInfo));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }
}
