package com.cxk.redpacket.utils;

/**
 * Description:
 * Data：2018/10/26-10:47
 * Author: Allen
 */
public interface CallBackUI<T> {

    T execute();

    void callBackUI(T t);
}
