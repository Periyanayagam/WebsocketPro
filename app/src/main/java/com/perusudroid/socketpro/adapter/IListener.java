package com.perusudroid.socketpro.adapter;

import android.content.Context;

import com.perusudroid.socketpro.db.Messages;

/**
 * Created by Perusudroid on 3/19/2018.
 */

public interface IListener {
    void onClick(Messages messages);
    void onLongClick(Messages messages, int adapterPosition);
    Context getContext();
}
