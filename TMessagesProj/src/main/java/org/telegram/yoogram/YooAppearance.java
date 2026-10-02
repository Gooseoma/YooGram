package org.telegram.yoogram;

import android.text.TextUtils;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
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

    private static final Paint DOT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint RING_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** Draws a small green dot on the avatar of a message author who is online right now. */
    public static void drawOnlineDot(Canvas canvas, ImageReceiver avatar, int account, MessageObject message) {
        if (avatar == null || message == null || message.messageOwner == null || !(message.messageOwner.from_id instanceof TLRPC.TL_peerUser)) {
            return;
        }
        final TLRPC.User user = MessagesController.getInstance(account).getUser(message.messageOwner.from_id.user_id);
        if (user == null || user.bot || !(user.status instanceof TLRPC.TL_userStatusOnline)) {
            return;
        }
        if (user.status.expires <= ConnectionsManager.getInstance(account).getCurrentTime()) {
            return;
        }
        final float radius = AndroidUtilities.dp(5);
        final float cx = avatar.getImageX2() - AndroidUtilities.dp(3);
        final float cy = avatar.getImageY2() - AndroidUtilities.dp(3);
        RING_PAINT.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        DOT_PAINT.setColor(0xFF3DC23F);
        canvas.drawCircle(cx, cy, radius + AndroidUtilities.dp(1.5f), RING_PAINT);
        canvas.drawCircle(cx, cy, radius, DOT_PAINT);
    }
}
