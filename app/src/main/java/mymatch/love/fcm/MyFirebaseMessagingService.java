package mymatch.love.fcm;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import mymatch.love.activities.CurrentPlanActivity;
import mymatch.love.activities.DashboardActivity;
import mymatch.love.activities.ExpressInterestActivity;
import mymatch.love.activities.ManagePhotosActivity;
import mymatch.love.activities.OtherUserProfileActivity;
import mymatch.love.activities.QuickMessageActivity;
import mymatch.love.activities.UploadIdAndHoroscopeActivity;
import mymatch.love.activities.UploadVideoActivity;
import mymatch.love.activities.ViewMyProfileActivity;
import mymatch.love.application.MyApplication;
import mymatch.love.utility.AppConstants;
import mymatch.love.utility.AppDebugLog;
import mymatch.love.utility.SessionManager;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String KEY_OTHER_ID = "other_id";
    private static final String KEY_INTEREST_TAG = "interest_tag";
    private static final String KEY_MATRI_ID = "matri_id";
    private static final String KEY_PHOTO_PASSWORD_TAG = "ppassword_tag";
    private static final String KEY_TITLE = "title";
    private static final String KEY_BODY = "body";
    private static final String KEY_NOTIFICATION_TYPE = "noti_type";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_CHAT = "chat";
    private static final String KEY_PHOTO_URL = "photoUrl";

    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        AppDebugLog.print("FCM Token : " + s);
        SessionManager session = new SessionManager(this);
        session.setUserData(SessionManager.KEY_DEVICE_TOKEN, s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Log.e(TAG, "From: " +remoteMessage.getData());

        if (remoteMessage == null)
            return;

        AppDebugLog.print("remoteMessage in onMessageReceived : " + remoteMessage);
        handleDataMessage(remoteMessage);

    }

    private void handleDataMessage(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();

        if (data.containsKey(KEY_NOTIFICATION_TYPE)) {
            if (data.get(KEY_NOTIFICATION_TYPE).equals(KEY_MESSAGE)) {
                Intent pushNotification1 = new Intent(AppConstants.OUICK_TAG);
                pushNotification1.putExtra(KEY_MESSAGE, new HashMap<String, String>(data));
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification1);

                Intent pushNotification2 = new Intent(AppConstants.CHAT_TAG);
                pushNotification2.putExtra(KEY_MESSAGE, new HashMap<String, String>(data));
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification2);
            }

            if (Objects.requireNonNull(data.get(KEY_NOTIFICATION_TYPE)).equalsIgnoreCase(KEY_CHAT)) {
                Intent pushNotification1 = new Intent(AppConstants.CHAT_LIST_TAG);
                pushNotification1.putExtra(KEY_CHAT, new HashMap<String, String>(data));
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification1);

                Intent pushNotification2 = new Intent(AppConstants.CHAT_CONVERSATION_TAG);
                pushNotification2.putExtra(KEY_CHAT, new HashMap<String, String>(data));
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification2);
            }
        }

        //logout current user if login in other app with same credentials
        if (Objects.requireNonNull(data.get(KEY_NOTIFICATION_TYPE)).equals("session_expire")) {
            SessionManager sessionManager = new SessionManager(MyApplication.getContext());
            sessionManager.logoutUser();
            return;
        }

        Log.e(TAG, "From " + data);
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());

        Intent pendinIntent = null;
        boolean isShowActionInNotification = false;

        switch (Objects.requireNonNull(data.get(KEY_NOTIFICATION_TYPE))) {
            case "payment_received":
                pendinIntent = new Intent(getApplicationContext(), CurrentPlanActivity.class);
                break;

            case "featured_profile":
                pendinIntent = new Intent(getApplicationContext(), ViewMyProfileActivity.class);
                break;

            case "profile_photo_approval":
                pendinIntent = new Intent(getApplicationContext(), ManagePhotosActivity.class);
                break;

            case "id_proof_photo_approval":
                pendinIntent = new Intent(getApplicationContext(), UploadIdAndHoroscopeActivity.class);
                break;

            case "video_approval":
                pendinIntent = new Intent(getApplicationContext(), UploadVideoActivity.class);
                break;

            case "plan_expired":
                pendinIntent = new Intent(getApplicationContext(), CurrentPlanActivity.class);
                break;

            case "viewed_contact_details":
                pendinIntent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                break;

            case "viewed_profile":
                pendinIntent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                break;

            case "add_shortlist":
                pendinIntent = new Intent(getApplicationContext(), OtherUserProfileActivity.class);
                break;

            case "interest_receive":
                pendinIntent = new Intent(getApplicationContext(), ExpressInterestActivity.class);
                break;

            case "message":
                pendinIntent = new Intent(getApplicationContext(), QuickMessageActivity.class);
                break;

            case "reminder_interest_receive":
                pendinIntent = new Intent(getApplicationContext(), ExpressInterestActivity.class);
                break;

            case "accepted_interest_receive":
                pendinIntent = new Intent(getApplicationContext(), ExpressInterestActivity.class);
                break;

            case "rejected_interest_receive":
                pendinIntent = new Intent(getApplicationContext(), ExpressInterestActivity.class);
                break;

            default:
                pendinIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                break;
        }

        if (data.containsKey(KEY_OTHER_ID))
            pendinIntent.putExtra(KEY_OTHER_ID, data.get(KEY_OTHER_ID));
        if (data.containsKey(KEY_INTEREST_TAG))
            pendinIntent.putExtra(KEY_INTEREST_TAG, data.get(KEY_INTEREST_TAG));
        if (data.containsKey(KEY_MATRI_ID))
            pendinIntent.putExtra(KEY_MATRI_ID, data.get(KEY_MATRI_ID));
        if (data.containsKey(KEY_PHOTO_PASSWORD_TAG))
            pendinIntent.putExtra(KEY_PHOTO_PASSWORD_TAG, data.get(KEY_PHOTO_PASSWORD_TAG));

        if (data.containsKey(KEY_PHOTO_URL)) {
            showNotificationMessageWithBigImage(getApplicationContext(), data.get(KEY_TITLE), data.get(KEY_BODY), ts.toString(), pendinIntent, data.get(KEY_PHOTO_URL));
        } else {
            showNotificationMessage(getApplicationContext(), data.get(KEY_TITLE), data.get(KEY_BODY), ts.toString(), pendinIntent);
        }

        //showNotificationMessage(getApplicationContext(), data.get(KEY_TITLE), data.get(KEY_BODY), ts.toString(), pendinIntent);

    }

    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        Spanned spannedMsg = Html.fromHtml(message);
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, spannedMsg.toString(), timeStamp, intent);
    }

    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

}
