package com.unipi.chrispana.smartalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent closeIntent = new Intent(context, DatabaseListenerService.class);
        closeIntent.setAction("CLOSE");
        context.startService(closeIntent);
    }
}
