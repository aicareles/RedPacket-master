package com.cxk.redpacket.utils;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Description:封装的rxjava切换线程的通用工具，子线程完成任务切换成到主线程
 * Data：2018/10/26-10:39
 * Author: Allen
 */
public class ThreadUtils {
    private static final Handler MAIN_HANDLER       = new Handler(Looper.getMainLooper());

    public static <T> void asyncThreadCallback(final CallBackUI<T> rxCallBackUI) {
        Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                //onNext中的参数不能为null，否则onNext接收不到
                emitter.onNext(rxCallBackUI.execute());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(T t) {
                rxCallBackUI.callBackUI(t);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public static void asyncThread(final CallBack callBack){
        TaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                callBack.execute();
            }
        });
    }

    public static void asyncThreadDelay(final long delay, final CallBack callBack){
        TaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                delay(delay);
                callBack.execute();
            }
        });
    }

    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }

    public static void runOnUiThreadDelay(final Runnable runnable, long delayMillis) {
        MAIN_HANDLER.postDelayed(runnable, delayMillis);
    }

    public static void delay(long delay){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
