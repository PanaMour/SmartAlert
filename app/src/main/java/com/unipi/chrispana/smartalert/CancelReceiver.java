package com.unipi.chrispana.smartalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelReceiver extends BroadcastReceiver {
    //Gets called when user presses "Stop Listening" from the notification that is created through DatabaseListenerService.
    //It starts a service of DatabaseListenerService with action "CLOSE" so that the running service is closed.
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent closeIntent = new Intent(context, DatabaseListenerService.class);
        closeIntent.setAction("CLOSE");
        context.startService(closeIntent);
    }
}
