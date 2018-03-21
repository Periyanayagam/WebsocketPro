package com.perusudroid.socketpro.db;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by Perusudroid on 3/12/2018.
 */
@Entity(nameInDb = "messages")
public class Messages implements Parcelable{

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "who")
    private Integer who;

    @Property(nameInDb = "msg")
    private String msg;

    @Property(nameInDb = "offline")
    private Integer offline;

    @Property(nameInDb = "sendStatus")
    private Integer sendStatus;

    @Generated(hash = 1803209798)
    public Messages(Long id, Integer who, String msg, Integer offline, Integer sendStatus) {
        this.id = id;
        this.who = who;
        this.msg = msg;
        this.offline = offline;
        this.sendStatus = sendStatus;
    }

    @Generated(hash = 826815580)
    public Messages() {
    }

    protected Messages(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        if (in.readByte() == 0) {
            who = null;
        } else {
            who = in.readInt();
        }
        msg = in.readString();
        if (in.readByte() == 0) {
            offline = null;
        } else {
            offline = in.readInt();
        }
        if (in.readByte() == 0) {
            sendStatus = null;
        } else {
            sendStatus = in.readInt();
        }
    }

    public static final Creator<Messages> CREATOR = new Creator<Messages>() {
        @Override
        public Messages createFromParcel(Parcel in) {
            return new Messages(in);
        }

        @Override
        public Messages[] newArray(int size) {
            return new Messages[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getWho() {
        return who;
    }

    public void setWho(Integer who) {
        this.who = who;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getOffline() {
        return offline;
    }

    public void setOffline(Integer offline) {
        this.offline = offline;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        if (who == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(who);
        }
        parcel.writeString(msg);
        if (offline == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(offline);
        }
        if (sendStatus == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(sendStatus);
        }
    }
}
