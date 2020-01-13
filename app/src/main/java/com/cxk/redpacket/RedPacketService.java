package com.cxk.redpacket;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.cxk.redpacket.utils.LogUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.List;

/**
 * 抢红包Service,继承AccessibilityService
 */
public class RedPacketService extends AccessibilityService {
    private static final String TAG = "RedPacketService";

    //Intent构造器里面的变量是一个字符串，通常在要更新UI的文件中进行定义
    private Intent intent = new Intent(MainActivity.ACTION_SERVICE_STATE_CHANGE);
    /**
     * 微信几个页面的包名+地址。用于判断在哪个页面
     * LAUCHER-微信聊天界面
     * LUCKEY_MONEY_RECEIVER-点击红包弹出的界面
     * LUCKEY_MONEY_DETAIL-红包领取后的详情界面
     */
    private String LAUCHER = "com.tencent.mm.ui.LauncherUI";
//    private String LAUCHER = "com.tencent.mm.ui.chatting.ChattingUI";
    private String LUCKEY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
//    private String LUCKEY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private String LUCKEY_MONEY_RECEIVER = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";

    /**
     * 用于判断是否点击过红包了
     */
    private boolean isOpenRP;

    private boolean isOpenDetail = false;

    /**
     * 用于判断是否屏幕是亮着的
     */
    private boolean isScreenOn;

    /**
     * 获取PowerManager.WakeLock对象
     */
    private PowerManager.WakeLock wakeLock;

    /**
     * KeyguardManager.KeyguardLock对象
     */
    private KeyguardManager.KeyguardLock keyguardLock;

    public void update(long delay){
        AccessibilityServiceInfo info = getServiceInfo();
        info.notificationTimeout = delay;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        LogUtils.e(TAG, "onAccessibilityEvent: eventType:"+eventType);
        switch (eventType) {
            //通知栏来信息，判断是否含有微信红包字样，是的话跳转
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                LogUtils.e(TAG, "onAccessibilityEvent: text>>>>"+event.toString());
                List<CharSequence> texts = event.getText();
                for (CharSequence text : texts) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        //判断是否含有[微信红包]字样
                        if (content.contains("[微信红包]")) {
                            if (!isScreenOn()) {
                                wakeUpScreen();
                            }
                            //如果有则打开微信红包页面
                            openWeChatPage(event);
                            isOpenRP = false;
                        }
                    }
                }
                break;
            //界面跳转的监听
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (TextUtils.isEmpty(event.getClassName()))return;
                String className = event.getClassName().toString();
                LogUtils.e(TAG, "onAccessibilityEvent: className:"+className);
                //判断是否是微信聊天界面
                if (LAUCHER.equals(className)) {
                    LogUtils.e(TAG, "onAccessibilityEvent: 微信聊天页面");
                    //获取当前聊天页面的根布局
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始找红包
                    findRedPacket(rootNode);
                }

                //判断是否是显示‘开’的那个红包界面
                if (LUCKEY_MONEY_RECEIVER.equals(className)) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    //开始抢红包
                    openRedPacket(rootNode);
                }
                //判断是否是红包领取后的详情界面
                if (isOpenDetail && LUCKEY_MONEY_DETAIL.equals(className)) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    RedPacket redPacket = new RedPacket();
                    findMoneyNum(rootNode, redPacket);
//                    sendAction(redPacket);
//                    LiveBus.getDefault().postEvent(MainActivity.RECEIVE_RED_PACKET, redPacket);
                    redPacket.setCreateDate(System.currentTimeMillis());
                    LiveEventBus.get(MainActivity.RECEIVE_RED_PACKET).post(redPacket);
                    isOpenDetail = false;
                    /*//返回桌面
                    back2Home();
                    //如果之前是锁着屏幕的则重新锁回去
                    release();*/
                }
                break;
        }
    }

    private void findMoneyNum(AccessibilityNodeInfo rootNode, RedPacket redPacket) {
        LogUtils.e(TAG, "详细信息:findMoneyNum:--- "+rootNode.getChildCount() );
        int childCount = rootNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (childCount == 6){//详细信息
                if ("android.widget.TextView".equals(node.getClassName())){
                    if (!TextUtils.isEmpty(node.getText())){
                        if (i==0){
                            redPacket.setName(node.getText().toString());
                        }else if (i==1){
                            redPacket.setMessage(node.getText().toString());
                        }else if (i==2){
                            redPacket.setNumber(node.getText().toString());
                        }
//                        LogUtils.e(TAG, "findMoneyNum:redPacket>>>> "+redPacket.toString());
//                        LiveBus.getDefault().postEvent(MainActivity.RECEIVE_RED_PACKET, redPacket);
                    }
                }
            }
            /*LogUtils.e(TAG, "findMoneyNum: node++++++>>>>>"+node.toString());
            if ("android.widget.TextView".equals(node.getClassName())){
                if (!TextUtils.isEmpty(node.getText())){
                    String name = node.getText().toString();
                    if (name.contains("的红包")){
                        redPacket.setName(name);
                    }

                }
            }*/
            findMoneyNum(node, redPacket);
        }
        /*for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if ("android.widget.TextView".equals(node.getClassName())) {
                LogUtils.e(TAG, "findMoneyNum: TextView-----"+node.toString());
                if (TextUtils.isEmpty(node.getText()))return;
                String nodeStr = node.getText().toString();
                LogUtils.e(TAG, "详细信息: " + nodeStr + i);
                RedPacket redPacket = new RedPacket();
                if (i == 0) {
                    //来自xxx
                    redPacket.setName(nodeStr);
                } else if (i == 1) {
                    //红包留言
                    redPacket.setMessage(nodeStr);
                } else if (i == 2 && NumberUtils.isNumber(nodeStr)) {
                    redPacket.setNumber(nodeStr);
                    LogUtils.e(TAG, "收到红包金额: " + nodeStr);
                }
                LiveBus.getDefault().postEvent(MainActivity.RECEIVE_RED_PACKET, redPacket);
            }
            findMoneyNum(node);
        }*/
    }

    /**
     * 开始打开红包
     */
    private void openRedPacket(AccessibilityNodeInfo rootNode) {
        /*if (rootNode != null){
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                if ("android.widget.Button".equals(node.getClassName())) {
                    LogUtils.e(TAG, "openRedPacket: 打开红包>>>>>>");
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    isOpenDetail = true;
                }
                openRedPacket(node);
            }
        }*/
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float dpi = metrics.densityDpi;
        LogUtils.d(TAG, "openPacket！" +  dpi);
        if (android.os.Build.VERSION.SDK_INT <= 23) {
            if (rootNode != null) {
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo node = rootNode.getChild(i);
                    if ("android.widget.Button".equals(node.getClassName())) {
                        LogUtils.e(TAG, "openRedPacket: 打开红包>>>>>>");
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        isOpenDetail = true;
                    }
                    openRedPacket(node);
                }
            }
        } else {
            Path path = new Path();
            path.moveTo(metrics.widthPixels/2, (float) (metrics.heightPixels*0.67));
            /*if (640 == dpi) { //1440
                path.moveTo(720, 1575);
            } else if(320 == dpi){//720p
                path.moveTo(355, 780);
            }else if(480 == dpi){//1080p
                path.moveTo(562, 1276);
            }else {//420.0
                path.moveTo(576, 1451);
            }*/
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gestureDescription = builder.addStroke(new GestureDescription.StrokeDescription(path, 450, 50)).build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    LogUtils.d(TAG, "onCompleted");
                    isOpenDetail = true;
//                    mMutex = false;
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    LogUtils.d(TAG, "onCancelled");
//                    mMutex = false;
                    super.onCancelled(gestureDescription);
                }
            }, null);

        }
    }

    /**
     * 遍历查找红包
     */
    private void findRedPacket(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //从最后一行开始找起
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                //如果node为空则跳过该节点
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                LogUtils.e(TAG, "rootNode  text:"+text);
                if (text != null && text.toString().equals("微信红包")) {
                    AccessibilityNodeInfo parent = node.getParent();
                    //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
                    while (parent != null) {
                        if (parent.isClickable()) {
                            //模拟点击
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            //isOpenRP用于判断该红包是否点击过
                            isOpenRP = true;
                            break;
                        }
                        parent = parent.getParent();
                    }
                } else if(node.getClassName().equals("android.widget.EditText")){// 找到输入框并输入文本
                    if (Config.replayEnable && !TextUtils.isEmpty(Config.replayMsg)){
                        replayText(node, Config.replayMsg);
                        //发送
                        findAndPerformAction(UI.BUTTON, "发送");
                        LogUtils.e(TAG, "自动回复消息成功");
                    }
                }
                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
                if (isOpenRP) {
                    break;
                } else {
                    findRedPacket(node);
                }

            }
        }
    }

    /**
     * 查找UI控件并点击
     * @param widget 控件完整名称, 如android.widget.Button, android.widget.TextView
     * @param text 控件文本
     */
    private void findAndPerformAction(String widget, String text) {
        // 取得当前激活窗体的根节点
        if (getRootInActiveWindow() == null) {
            return;
        }

        // 通过文本找到当前的节点
        List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        if(nodes != null) {
            for (AccessibilityNodeInfo node : nodes) {
                if (node.getClassName().equals(widget) && node.isEnabled()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK); // 执行点击
                    break;
                }
            }
        }
    }

    private void replayText(AccessibilityNodeInfo node, String reply) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.i(TAG, "set text");
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    reply);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
        } else {
            ClipData data = ClipData.newPlainText("reply", reply);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(data);
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS); // 获取焦点
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE); // 执行粘贴
        }
    }

    /**
     * 开启红包所在的聊天页面
     */
    private void openWeChatPage(AccessibilityEvent event) {
        //A instanceof B 用来判断内存中实际对象A是不是B类型，常用于强制转换前的判断
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            //打开对应的聊天界面
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 服务连接
     */
    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "抢红包服务开启", Toast.LENGTH_SHORT).show();
//        AccessibilityServiceInfo info = getServiceInfo();
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        info.notificationTimeout = Config.delay;
//        setServiceInfo(info);
//        info.packageNames = new String[]{"xxx.xxx.xxx", "yyy.yyy.yyy","...."};
        super.onServiceConnected();
    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "我快被终结了啊-----", Toast.LENGTH_SHORT).show();
    }

    /**
     * 服务断开
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "抢红包服务已被关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    /**
     * 返回桌面
     */
    private void back2Home() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    /**
     * 判断是否处于亮屏状态
     *
     * @return true-亮屏，false-暗屏
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        LogUtils.e("isScreenOn", isScreenOn + "");
        return isScreenOn;
    }

    /**
     * 解锁屏幕
     */
    private void wakeUpScreen() {

        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //后面的参数|表示同时传入两个值，最后的是调试用的Tag
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "bright");

        //点亮屏幕
        wakeLock.acquire();

        //得到键盘锁管理器
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = km.newKeyguardLock("unlock");

        //解锁
        keyguardLock.disableKeyguard();
    }

    /**
     * 释放keyguardLock和wakeLock
     */
    public void release() {
        if (keyguardLock != null) {
            keyguardLock.reenableKeyguard();
            keyguardLock = null;
        }
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

}
