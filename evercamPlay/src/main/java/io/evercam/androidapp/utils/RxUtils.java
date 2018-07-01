package io.evercam.androidapp.utils;

import rx.Subscription;

public class RxUtils {

    public static void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}