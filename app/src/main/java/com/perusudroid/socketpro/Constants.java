package com.perusudroid.socketpro;

/**
 * Created by Perusudroid on 3/11/2018.
 */

public class Constants {

    public interface common {
        int SYNCED = 1;
        int NOT_SYNCED = 0;
        int SEND = 1;
        int RECEIVED = 2;
        int MSG_SENDING = 0;
        int MSG_SEND = 1;
        int MSG_RECEIVED = 2;
    }

    public interface bundleKeys {
        String REFERSH_DATA = "REFERSH_DATA";
        String SOCKET_DATA_STRING = "SOCKET_DATA_STRING";
        String SOCKET_DATA_INTEGER = "SOCKET_DATA_INTEGER";
        String SOCKET_DATA_LIST= "SOCKET_DATA_LIST";
        String UPDATED_OFFLINE_MSG_LIST= "UPDATED_OFFLINE_MSG_LIST";
    }

    public interface broadcasts {
        String DO_REFRESH = "DO_REFRESH";
        String SOCKET = "SOCKET";
        String SOCKET_MSG_RECEIVED = "SOCKET_MSG_RECEIVED";
        String MSG_SEND_REFRESH = "MSG_SEND_REFRESH";
    }

}
