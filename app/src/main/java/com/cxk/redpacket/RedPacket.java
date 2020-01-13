package com.cxk.redpacket;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class RedPacket {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String number;
    private String message;
    private long createDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "RedPacket{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", message='" + message + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}
