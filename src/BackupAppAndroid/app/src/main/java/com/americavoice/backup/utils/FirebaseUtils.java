package com.americavoice.backup.utils;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by javier on 9/19/17.
 */

public class FirebaseUtils {

    public static final String MENU_BUTTON_CONTENT_TYPE = "menu button";
    public static final String LOGIN_METHOD_PHONE_NUMBER = "phone number";

    public static void createFirebaseEvent(
            FirebaseAnalytics firebaseAnalytics,
            String itemId,
            String itemName,
            String contentType,
            String event
    ) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        firebaseAnalytics.logEvent(event, bundle);
    }

    public static void createLoginEvent(FirebaseAnalytics firebaseAnalytics, String signupMethod) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, signupMethod);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }
}
