package com.example.geolocalizacin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SegundoPlano extends BroadcastReceiver{
    public void onReceive(Context context,Intent intent) {

        Intent i = new Intent(context, Inicio.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }
}
