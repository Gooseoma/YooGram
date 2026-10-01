package org.telegram.yoogram;

import android.app.Activity;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.utils.settings.SharedSettings;

/** Quick mute / ban actions for supergroup messages. */
public final class YooModeration {

    private YooModeration() {
    }

    public static boolean canModerate(int account, TLRPC.Chat chat, MessageObject message) {
        if (!SharedSettings.yooQuickModeration.get() || chat == null || message == null) {
            return false;
        }
        if (!ChatObject.isMegagroup(chat) || !ChatObject.canBlockUsers(chat)) {
            return false;
        }
        if (message.messageOwner instanceof TLRPC.TL_messageService || message.isOutOwner() || message.messageOwner == null || message.messageOwner.post) {
            return false;
        }
        if (!(message.messageOwner.from_id instanceof TLRPC.TL_peerUser)) {
            return false;
        }
        long uid = message.messageOwner.from_id.user_id;
        if (uid == 0 || uid == UserConfig.getInstance(account).getClientUserId()) {
            return false;
        }
        MessagesController controller = MessagesController.getInstance(account);
        if (controller.getAdminInChannel(uid, chat.id) != null) {
            return false;
        }
        TLRPC.User user = controller.getUser(uid);
        return user != null;
    }

    public static void showMenu(int account, BaseFragment fragment, TLRPC.Chat chat, MessageObject message) {
        Activity activity = fragment.getParentActivity();
        if (activity == null) {
            return;
        }
        final TLRPC.User user = MessagesController.getInstance(account).getUser(message.messageOwner.from_id.user_id);
        if (user == null) {
            return;
        }
        CharSequence[] items = new CharSequence[]{
            LocaleController.getString(R.string.YooMute10Min),
            LocaleController.getString(R.string.YooMute1Hour),
            LocaleController.getString(R.string.YooMute1Day),
            LocaleController.getString(R.string.YooBan)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(LocaleController.getString(R.string.YooModeration));
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0:
                    mute(account, fragment, chat, user, 600);
                    break;
                case 1:
                    mute(account, fragment, chat, user, 3600);
                    break;
                case 2:
                    mute(account, fragment, chat, user, 86400);
                    break;
                default:
                    confirmBan(account, fragment, chat, user);
                    break;
            }
        });
        fragment.showDialog(builder.create());
    }

    private static void confirmBan(int account, BaseFragment fragment, TLRPC.Chat chat, TLRPC.User user) {
        Activity activity = fragment.getParentActivity();
        if (activity == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(LocaleController.getString(R.string.YooBan));
        builder.setMessage(LocaleController.formatString(R.string.YooBanConfirm, UserObject.getUserName(user)));
        builder.setPositiveButton(LocaleController.getString(R.string.YooBan), (d, w) -> ban(account, fragment, chat, user));
        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        AlertDialog dialog = builder.create();
        fragment.showDialog(dialog);
        dialog.redPositive();
    }

    private static void mute(int account, BaseFragment fragment, TLRPC.Chat chat, TLRPC.User user, int seconds) {
        TLRPC.TL_chatBannedRights rights = new TLRPC.TL_chatBannedRights();
        rights.until_date = (int) (ConnectionsManager.getInstance(account).getCurrentTime() + seconds);
        rights.send_messages = true;
        rights.send_media = true;
        rights.send_plain = true;
        rights.send_stickers = true;
        rights.send_gifs = true;
        rights.send_games = true;
        rights.send_inline = true;
        rights.send_polls = true;
        rights.send_photos = true;
        rights.send_videos = true;
        rights.send_roundvideos = true;
        rights.send_audios = true;
        rights.send_voices = true;
        rights.send_docs = true;
        rights.embed_links = true;
        send(account, fragment, chat, user, rights, LocaleController.formatString(R.string.YooMuted, UserObject.getUserName(user)));
    }

    private static void ban(int account, BaseFragment fragment, TLRPC.Chat chat, TLRPC.User user) {
        TLRPC.TL_chatBannedRights rights = new TLRPC.TL_chatBannedRights();
        rights.view_messages = true;
        rights.until_date = 0;
        send(account, fragment, chat, user, rights, LocaleController.formatString(R.string.YooBanned, UserObject.getUserName(user)));
    }

    private static void send(int account, BaseFragment fragment, TLRPC.Chat chat, TLRPC.User user, TLRPC.TL_chatBannedRights rights, String successText) {
        MessagesController controller = MessagesController.getInstance(account);
        TLRPC.TL_channels_editBanned req = new TLRPC.TL_channels_editBanned();
        req.channel = controller.getInputChannel(chat.id);
        req.participant = controller.getInputPeer(user);
        req.banned_rights = rights;
        ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> org.telegram.messenger.AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                controller.processUpdates((TLRPC.Updates) response, false);
                org.telegram.messenger.AndroidUtilities.runOnUIThread(() -> controller.loadFullChat(chat.id, 0, true), 1000);
                BulletinFactory.of(fragment).createSimpleBulletin(R.raw.ic_ban, successText).show();
            } else {
                BulletinFactory.of(fragment).createSimpleBulletin(R.raw.error, error.text).show();
            }
        }));
    }
}
