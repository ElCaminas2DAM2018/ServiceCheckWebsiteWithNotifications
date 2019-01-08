package org.ieselcaminas.pmdm.servicecheckwebsite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    List<TaskCheck> taskChecks = new ArrayList<TaskCheck>();
    public static final int MY_ID_NOTIF = 1234;

    public class TaskCheck extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            int seconds = Integer.parseInt(strings[1]);
            String website = strings[0];
            while (!isCancelled()) {

                try {
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException ex) {
                    //
                }
                if ((!openHttpConnection(website)) && (!isCancelled())) {
                    notifyDown(website);
                } else {
                    publishProgress(null);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... website) {
            if (website == null) { // everything ok
                Log.d("Check", "connected Ok");
            } else {
                Toast.makeText(getApplicationContext(), "Not connected to " + website[0],
                        Toast.LENGTH_LONG).show();
            }
        }

        private void notifyDown(String web) {

            NotificationManager notManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT>=26) { //new
                NotificationChannel mChannel = new NotificationChannel("channel1",
                        "Web site checker", NotificationManager.IMPORTANCE_HIGH);
                mChannel.setDescription("My description");
                notManager.createNotificationChannel(mChannel);
            }

            int icon = android.R.drawable.stat_sys_warning;
            CharSequence textState = "Attention!";
            CharSequence textContent = "Website " + web + "not available";
            long time = System.currentTimeMillis();

            Intent notIntent = new Intent(getApplicationContext(), StopServiceActivity.class);
            // We use a PendingIntent to create the notification
            PendingIntent contIntent = PendingIntent.getActivity(getApplicationContext(),
                    0, notIntent, 0);

            Notification notification = new NotificationCompat.Builder(
                    getApplicationContext(), "channel1")
                    .setSmallIcon(icon)
                    .setContentTitle(textState)
                    .setContentText(textContent)
                    .setWhen(time)
                    .setContentIntent(contIntent)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.defaults |= Notification.DEFAULT_LIGHTS;

            //We send the notification
            notManager.notify(MY_ID_NOTIF, notification);
            // It can take a while until the server answers.
        }


        private boolean openHttpConnection(String urlString) {
            int response;

            try {
                URL url = new URL(urlString);
                URLConnection conn = url.openConnection();
                if (!(conn instanceof HttpURLConnection))
                    throw new IOException("Not an HTTP connection");
                HttpURLConnection httpConn = (HttpURLConnection) conn;
                httpConn.setInstanceFollowRedirects(true);
                httpConn.connect();
                response = httpConn.getResponseCode();
                httpConn.disconnect();
                if (response == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            } catch (Exception ex) {

            }
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        Bundle bundle = intent.getExtras();
        String website = bundle.getString("website");
        String seconds = bundle.getString("seconds");
        TaskCheck taskCheck = new TaskCheck();
        // If we want the app can start service on several web sites (several startService calls)
        taskCheck.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, website, seconds);
        taskChecks.add(taskCheck);

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
        // If we want it to work when the app is swiped out
        //return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (TaskCheck taskCheck: taskChecks) {
            taskCheck.cancel(true);
        }
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}