package com.cxk.redpacket;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.cxk.redpacket.adapter.RedPacketAdapter;
import com.cxk.redpacket.db.RedPacketDatabase;
import com.cxk.redpacket.utils.CallBackUI;
import com.cxk.redpacket.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryRecordActivity extends AppCompatActivity {

    private ListView listView;
    private RedPacketAdapter adapter;
    private List<RedPacket> datas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        listView = findViewById(R.id.lv);
        datas = new ArrayList<>();
        adapter = new RedPacketAdapter(this, datas);
        listView.setAdapter(adapter);

        getAllDatas();
    }

    private void getAllDatas() {
        ThreadUtils.asyncThreadCallback(new CallBackUI<List<RedPacket>>() {
            @Override
            public List<RedPacket> execute() {
                return RedPacketDatabase.getDatabase(HistoryRecordActivity.this).redPacketDao().getAll();
            }

            @Override
            public void callBackUI(List<RedPacket> redPackets) {
                datas.addAll(redPackets);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
