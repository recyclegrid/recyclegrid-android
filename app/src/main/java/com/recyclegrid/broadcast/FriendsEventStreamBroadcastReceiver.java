package com.recyclegrid.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.recyclegrid.app.FriendsCheckinsFragment;

public class FriendsEventStreamBroadcastReceiver extends BroadcastReceiver {
    FriendsCheckinsFragment _fragment;

    public FriendsEventStreamBroadcastReceiver(FriendsCheckinsFragment fragment) {
        _fragment = fragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (_fragment != null) {
            _fragment.refresh();
        }
    }
}
