package io.evercam.androidapp;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by zulqarnainmustafa on 1/26/17.
 */

public class AppIndexingService extends IntentService {

//    public AppIndexingService(String name) {
//
//        super(name);
//    }

    public AppIndexingService() {
        super("AppIndexingService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
