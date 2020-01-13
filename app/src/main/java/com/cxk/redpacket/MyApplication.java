package com.cxk.redpacket;

import android.app.Application;
import android.content.Context;

import com.pgyersdk.Pgyer;
import com.pgyersdk.PgyerActivityManager;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.crash.PgyerCrashObservable;
import com.pgyersdk.crash.PgyerObserver;

public class MyApplication extends Application {

    private static MyApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        initPgy();
    }

    private void initPgy() {
        PgyCrashManager.register();
        PgyerCrashObservable.get().attach(new PgyerObserver() {
            @Override
            public void receivedCrash(Thread thread, Throwable throwable) {

            }
        });
        PgyerActivityManager.set(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        Pgyer.setAppId("ff01b86d6133015dd88db7d262957040");
    }

    public static MyApplication getInstance() {
        return mApplication;
    }

}