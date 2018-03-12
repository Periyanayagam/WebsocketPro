package com.perusudroid.socketpro.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by Perusudroid on 3/12/2018.
 */
@Entity(nameInDb = "messages")
public class Messages {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "who")
    private Integer who;

    @Property(nameInDb = "msg")
    private String msg;

   @Generated(hash = 810746136)
public Messages(Long id, Integer who, String msg) {
    this.id = id;
    this.who = who;
    this.msg = msg;
}

    @Generated(hash = 826815580)
    public Messages() {
    }

    public Integer getWho() {
        return who;
    }

    public void setWho(Integer who) {
        this.who = who;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getId() {
        return this.id;
    }
}
