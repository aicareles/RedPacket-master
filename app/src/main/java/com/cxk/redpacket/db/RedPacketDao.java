package com.cxk.redpacket.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.cxk.redpacket.RedPacket;

import java.sql.Date;
import java.util.List;

@Dao
public interface RedPacketDao {

    @Query("SELECT * FROM redpacket")
    List<RedPacket> getAll();

    @Query("SELECT * FROM redpacket where  createDate>:timestamp order by createDate desc")
    List<RedPacket> getTodayAll(long timestamp);

    @Transaction
    @Delete
    void delete(RedPacket... redPackets);

    @Insert
    void insert(RedPacket... redPackets);
}
