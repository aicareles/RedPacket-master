package com.cxk.redpacket.adapter;

import android.content.Context;
import android.widget.TextView;

import com.cxk.redpacket.R;
import com.cxk.redpacket.RedPacket;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class RedPacketAdapter extends BaseAdapter<RedPacket> {

    public RedPacketAdapter(Context context, List<RedPacket> mDatas) {
        super(context, mDatas, R.layout.item_redpacket);
    }

    @Override
    public void convert(ViewHolder holder, RedPacket item) {
        TextView tv_name = holder.getView(R.id.tv_name);
        TextView tv_number = holder.getView(R.id.tv_number);
        TextView tv_time = holder.getView(R.id.tv_time);
        tv_name.setText(item.getName());
        tv_number.setText(item.getNumber()+"元");
        tv_time.setText(longToString(item.getCreateDate()));
    }

    public String longToString(long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

}
