package com.cxk.redpacket.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.cxk.redpacket.RedPacket;

@Database(entities = {RedPacket.class}, version = 1)
public abstract class RedPacketDatabase extends RoomDatabase {

    private static volatile RedPacketDatabase INSTANCE;

    public static RedPacketDatabase getDatabase(Context context){
        if (INSTANCE == null) {
            synchronized (RedPacketDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedPacketDatabase.class, "redpacket_database").build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract RedPacketDao redPacketDao();
}
