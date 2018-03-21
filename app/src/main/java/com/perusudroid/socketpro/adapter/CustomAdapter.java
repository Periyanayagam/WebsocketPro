package com.perusudroid.socketpro.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.perusudroid.socketpro.Constants;
import com.perusudroid.socketpro.R;
import com.perusudroid.socketpro.db.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Perusudroid on 3/19/2018.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private List<Messages> myList;
    private IListener iListener;

    public CustomAdapter(List<Messages> users, IListener iListener) {
        this.myList = users;
        this.iListener = iListener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = null;
        Log.d("Adapter", "onCreateViewHolder: viewType " + viewType);
        switch (viewType) {
            case Constants.common.SEND:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflater_msg_send, parent, false);
                break;
            case Constants.common.RECEIVED:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflater_msg_received, parent, false);
                break;
        }
        return new CustomViewHolder(v);
    }

    @Override
    public int getItemViewType(int position) {
        return myList.get(position).getWho();
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Messages msg = myList.get(position);
        holder.txtMsg.setText(msg.getMsg());
        holder.itemView.setTag(msg);
        if (msg.getSendStatus() == Constants.common.MSG_SENDING) {
            holder.ivStatus.setVisibility(View.VISIBLE);
            holder.ivStatus.setImageResource(R.drawable.ic_access_time_white_24dp);
        } else if (msg.getSendStatus() == Constants.common.MSG_SEND) {
            holder.ivStatus.setVisibility(View.VISIBLE);
            holder.ivStatus.setImageResource(R.drawable.ic_done_white_24dp);
        } else if (msg.getSendStatus() == Constants.common.MSG_RECEIVED) {
          //  holder.ivStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    public void refresh(List<Messages> selectedList) {
        this.myList = selectedList;
        notifyDataSetChanged();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView txtMsg;
        public RelativeLayout rootLay;
        public ImageView ivStatus;

        public CustomViewHolder(View itemView) {
            super(itemView);
            txtMsg = itemView.findViewById(R.id.txt);
            rootLay = itemView.findViewById(R.id.rootLay);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            rootLay.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            iListener.onLongClick((Messages) view.getTag(), getAdapterPosition());
                            return true;
                        }
                    }
            );
        }
    }
}
