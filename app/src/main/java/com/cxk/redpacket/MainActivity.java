package com.cxk.redpacket;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.cxk.redpacket.adapter.RedPacketAdapter;
import com.cxk.redpacket.base.BaseActivity;
import com.cxk.redpacket.db.RedPacketDatabase;
import com.cxk.redpacket.update.UpdateManager;
import com.cxk.redpacket.utils.CallBack;
import com.cxk.redpacket.utils.CallBackUI;
import com.cxk.redpacket.utils.SPUtils;
import com.cxk.redpacket.utils.ThreadUtils;
import com.cxk.redpacket.utils.Utils;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String RECEIVE_RED_PACKET = "receive_red_packet";

    public static final String ACTION_SERVICE_STATE_CHANGE = "ACTION_SERVICE_STATE_CHANGE";
    private static final String TAG = "MainActivity";
    private ListView listView;
    private Switch switchReplay;
    private TextView tvTotalMoney;
    private EditText etReplay;
    private FloatingActionButton floatingButton;
    private RedPacketAdapter adapter;
    private List<RedPacket> datas;
    private double currentMoneyTotal = 0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);

        initView();
        initData();
        initSP();
        initLisenter();
        getTodayDatas();
        update();
    }

    private void initLisenter() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RedPacket redPacket = datas.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("红包详情")
                        .setMessage(redPacket.getName()+"\n"+redPacket.getNumber()+"元\n"+redPacket.getMessage())
                        .create()
                        .show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final RedPacket redPacket = datas.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("是否删除记录")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                datas.remove(redPacket);
                                adapter.notifyDataSetChanged();
                                ThreadUtils.asyncThread(new CallBack() {
                                    @Override
                                    public void execute() {
                                        RedPacketDatabase.getDatabase(MainActivity.this).redPacketDao().delete(redPacket);
                                    }
                                });
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });

        switchReplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Config.replayEnable = isChecked;
                SPUtils.put(MainActivity.this, Config.KEY_REPLAY_ENABLE, isChecked);
            }
        });

        etReplay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e(TAG, "afterTextChanged: "+s.toString());
                Config.replayMsg = s.toString();
                SPUtils.put(MainActivity.this, Config.KEY_REPLAY_TEXT, s.toString());
            }
        });

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HistoryRecordActivity.class));
            }
        });

        LiveEventBus.get(RECEIVE_RED_PACKET, RedPacket.class)
                .observeSticky(this, new Observer<RedPacket>() {
                    @Override
                    public void onChanged(@Nullable final RedPacket redPacket) {
                        Log.e(TAG, "onChanged: "+redPacket.toString());
                        currentMoneyTotal = Utils.sum(currentMoneyTotal, Double.valueOf(redPacket.getNumber()));
                        tvTotalMoney.setText("总收入: "+currentMoneyTotal+"元");
                        datas.add(0, redPacket);
                        adapter.notifyDataSetChanged();
                        ThreadUtils.asyncThread(new CallBack() {
                            @Override
                            public void execute() {
                                RedPacketDatabase.getDatabase(MainActivity.this).redPacketDao().insert(redPacket);
                            }
                        });
                    }
                });
    }

    private void update() {
        requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                "读写SD卡权限被拒绝,将会影响自动更新版本功能哦!", new GrantedResult() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted){
                            /*new PgyUpdateManager.Builder()
                                    .setForced(false)                //设置是否强制更新
                                    .setUserCanRetry(false)         //失败后是否提示重新下载
                                    .setDeleteHistroyApk(true)     // 检查更新前是否删除本地历史 Apk
                                    .register();*/
                            new UpdateManager(MainActivity.this).versionUpdate();
                        }
                    }
                });
    }

    private void initSP() {
        boolean replay = SPUtils.get(this, Config.KEY_REPLAY_ENABLE, false);
        Config.replayEnable = replay;
        switchReplay.setChecked(replay);
        Config.replayMsg = SPUtils.get(this, Config.KEY_REPLAY_TEXT, Config.replayMsg);
        etReplay.setText(Config.replayMsg);
    }

    private void initData() {
        datas = new ArrayList<>();
        adapter = new RedPacketAdapter(this, datas);
        listView.setAdapter(adapter);
    }

    private void getTodayDatas() {
        ThreadUtils.asyncThreadCallback(new CallBackUI<List<RedPacket>>() {
            @Override
            public List<RedPacket> execute() {
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                return RedPacketDatabase.getDatabase(MainActivity.this).redPacketDao().getTodayAll(Date.valueOf(format.format(date)).getTime());
            }

            @Override
            public void callBackUI(List<RedPacket> redPackets) {
                datas.addAll(redPackets);
                adapter.notifyDataSetChanged();
                for (RedPacket redPacket: redPackets) {
                    Log.e(TAG, "callBackUI: "+redPacket.getNumber());
                    currentMoneyTotal = Utils.sum(currentMoneyTotal, Double.valueOf(redPacket.getNumber()));
                }
                Log.e(TAG, "callBackUI: "+currentMoneyTotal);
                tvTotalMoney.setText("总收入: "+currentMoneyTotal+"元");
            }
        });
    }

    private void initView() {
        listView = findViewById(R.id.lv);
        switchReplay = findViewById(R.id.switch_replay);
        tvTotalMoney = findViewById(R.id.tv_total_money);
        floatingButton = findViewById(R.id.floatingButton);
        etReplay = findViewById(R.id.et_replay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("帮助")
                        .setMessage("1.检查是否能够收到通知栏消息\n2.所在的群不能设置消息免打扰\n" +
                                "3.锁屏时不能设置锁屏密码\n4.尽量不要在微信页面(否则收不到消息通知)\n" +
                                "5.开启辅助服务时,部分低端手机可能会有点卡顿,如果偶尔自动抢失败,建议重启下手机(开启辅助服务会有性能影响)")
                        .setPositiveButton("知道了", null)
                        .create()
                        .show();
                break;
            case R.id.menu_share:
                Utils.shareAPK(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}
