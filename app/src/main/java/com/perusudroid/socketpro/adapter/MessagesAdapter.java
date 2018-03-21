package com.perusudroid.socketpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.perusudroid.socketpro.Constants;
import com.perusudroid.socketpro.R;
import com.perusudroid.socketpro.db.Messages;

import java.util.List;

/**
 * Created by Aravindraj on 11/21/2017.
 */
public class MessagesAdapter extends ArrayAdapter<Messages> {


    public MessagesAdapter(Context context, List<Messages> users) {
        super(context, 0, users);
    }


    public void refresh(List<Messages> messagesList) {
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Messages messages = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            if(messages.getWho() == Constants.common.SEND){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.inflater_send_msg, parent, false);
            }else if(messages.getWho() == Constants.common.RECEIVED){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.inflater_received_msg, parent, false);
            }

        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.name);

        // Populate the data into the template view using the data object
        name.setText(messages.getMsg());

        // Return the completed view to render on screen
        return convertView;
    }

}