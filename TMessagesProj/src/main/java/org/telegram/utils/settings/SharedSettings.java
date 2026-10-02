package org.telegram.utils.settings;

import org.telegram.messenger.BuildConfig;
import org.telegram.utils.camera.roundvideo.RoundVideoSession;

public final class SharedSettings {

    public static final BooleanSetting experimentalSettingsAllowed =
        BooleanSetting.of("experimental_settings_allowed", BuildConfig.DEBUG_VERSION);

    public static final BooleanSetting roundVideoCamera2Enabled =
        BooleanSetting.of("round_video_camera2_enabled", BuildConfig.DEBUG_VERSION);

    public static final EnumSetting<RoundVideoSession.OutputResolution> roundVideoOutputResolution =
        EnumSetting.of("round_video_output_resolution", RoundVideoSession.OutputResolution.P480);

    public static final EnumSetting<RoundVideoSession.CameraResolution> roundVideoCameraResolution =
        EnumSetting.of("round_video_camera_resolution", RoundVideoSession.CameraResolution.HIGH);

    public static final EnumSetting<RoundVideoSession.FrameRate> roundVideoFrameRate =
        EnumSetting.of("round_video_frame_rate", RoundVideoSession.FrameRate.FPS_30);

    public static final IntSetting roundVideoVideoBitrate =
        IntSetting.of("round_video_video_bitrate", 1_000_000);

    public static final BooleanSetting roundVideoComposition =
        BooleanSetting.of("round_video_composition", true);

    public static final EnumSetting<RoundVideoSession.CameraFacing> roundVideoLastCamera =
        EnumSetting.of("round_video_last_camera", RoundVideoSession.CameraFacing.FRONT);

    public static final BooleanSetting yooQuickModeration =
        BooleanSetting.of("yoo_quick_moderation", true);

    public static final StringSetting yooBadgesPayload =
        StringSetting.of("yoo_badges_payload", null);

    public static final StringSetting yooBadgesEtag =
        StringSetting.of("yoo_badges_etag", null);

    public static final IntSetting yooBadgesVersion =
        IntSetting.of("yoo_badges_version", 0);

    public static final LongSetting yooBadgesLastUpdate =
        LongSetting.of("yoo_badges_last_update", 0L);

    // ---- YooGram: General ----
    public static final BooleanSetting yooNoNumberRounding =
        BooleanSetting.of("yoo_no_number_rounding", false);

    public static final BooleanSetting yooTimeSeconds =
        BooleanSetting.of("yoo_time_seconds", false);

    /** 0 = default, 1 = fast, 2 = ultra. */
    public static final IntSetting yooDownloadBoost =
        IntSetting.of("yoo_download_boost", 0);

    public static final BooleanSetting yooUploadBoost =
        BooleanSetting.of("yoo_upload_boost", false);

    public static final BooleanSetting yooKeepDeleted =
        BooleanSetting.of("yoo_keep_deleted", false);

    public static final BooleanSetting yooHideChatHeaderBg =
        BooleanSetting.of("yoo_hide_chat_header_bg", false);

    public static final BooleanSetting yooHidePhone =
        BooleanSetting.of("yoo_hide_phone", false);

    // ---- YooGram: Appearance ----
    public static final BooleanSetting yooForceGlass =
        BooleanSetting.of("yoo_force_glass", true);

    /** Avatar corner rounding: 0 = square, 100 = circle. */
    public static final IntSetting yooAvatarRounding =
        IntSetting.of("yoo_avatar_rounding", 100);

    /** Apply the avatar rounding to every avatar, including forums. */
    public static final BooleanSetting yooUnifiedRounding =
        BooleanSetting.of("yoo_unified_rounding", false);

    public static final BooleanSetting yooOnlineDot =
        BooleanSetting.of("yoo_online_dot", false);

    /** Locally chosen badge for the own account: 0 = none, otherwise 1-based index into YooBadges.PRESETS. */
    public static final IntSetting yooMyBadge =
        IntSetting.of("yoo_my_badge", 0);

    public static final BooleanSetting yooForceSnow =
        BooleanSetting.of("yoo_force_snow", false);

    public static final BooleanSetting yooHideStories =
        BooleanSetting.of("yoo_hide_stories", false);

    public static final BooleanSetting yooHeaderCenter =
        BooleanSetting.of("yoo_header_center", false);

    /** 0 = app name, 1 = "Chats", 2 = first name, 3 = username, 4 = full name. */
    public static final IntSetting yooHeaderText =
        IntSetting.of("yoo_header_text", 0);

    // ---- YooGram: Chats ----
    public static final BooleanSetting yooHideGreeting =
        BooleanSetting.of("yoo_hide_greeting", false);

    public static final BooleanSetting yooCommaAfterMention =
        BooleanSetting.of("yoo_comma_after_mention", false);

    public static final BooleanSetting yooEditedIcon =
        BooleanSetting.of("yoo_edited_icon", false);

    public static final BooleanSetting yooHideStickerTime =
        BooleanSetting.of("yoo_hide_sticker_time", false);

    public static final BooleanSetting yooAlwaysHd =
        BooleanSetting.of("yoo_always_hd", false);

    public static final BooleanSetting yooNoTail =
        BooleanSetting.of("yoo_no_tail", false);

    /** Sticker size, default 14 (scale = value / 14). */
    public static final IntSetting yooStickerSize =
        IntSetting.of("yoo_sticker_size", 14);

    /** Seconds to seek on double tap in the video player: 5, 10 or 20. */
    public static final IntSetting yooSeekSeconds =
        IntSetting.of("yoo_seek_seconds", 10);

    public static final BooleanSetting yooNoReplyEmoji =
        BooleanSetting.of("yoo_no_reply_emoji", false);

    public static final BooleanSetting yooNoReplyColors =
        BooleanSetting.of("yoo_no_reply_colors", false);

    public static final BooleanSetting yooHideFolderCounters =
        BooleanSetting.of("yoo_hide_folder_counters", false);

    public static final BooleanSetting yooConfirmSticker =
        BooleanSetting.of("yoo_confirm_sticker", false);

    public static final BooleanSetting yooNoDoubleTapReaction =
        BooleanSetting.of("yoo_no_double_tap_reaction", false);

    private SharedSettings() {
    }
}
