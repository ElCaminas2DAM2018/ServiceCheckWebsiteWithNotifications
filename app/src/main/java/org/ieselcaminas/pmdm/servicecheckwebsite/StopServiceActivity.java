package org.ieselcaminas.pmdm.servicecheckwebsite;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class StopServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_service);
    }

    public void stopService(View view) {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);

        NotificationManager notManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notManager.cancel(MyService.MY_ID_NOTIF);
    }
}
