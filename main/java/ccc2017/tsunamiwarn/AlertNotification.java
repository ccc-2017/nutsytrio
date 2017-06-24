package ccc2017.tsunamiwarn;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Herman on 6/22/2017.
 */

public class AlertNotification extends IntentService{


    public AlertNotification() {
        super("AlertNotification");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        String xmlData = null;
        List noaaInfo = null;
        
        try {
            URL url = new URL("https://alerts.weather.gov/cap/hi.php?x=0");
            //URL url = new URL("https://alerts.weather.gov/cap/wwaatmget.php?x=HIZ007&y=0");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                xmlData = stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }

        try{
            noaaXMLParser xmlParser = new noaaXMLParser();
            InputStream xmlDataReader = new ByteArrayInputStream(xmlData.getBytes());
            try {
                noaaInfo = xmlParser.parse(xmlDataReader);
            }finally{
                xmlDataReader.close();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        checkTsunami(noaaInfo);
    }

    public int onStartCommand(Intent intent, int flags, int startID){
        return super.onStartCommand(intent,flags,startID);
    }

    private void checkTsunami(List<Entry> feed){
        for(Entry entry : feed){
            if(entry.event.equals("Tsunami Warning")){
                sendNotification(entry.time);
                return;
            }
        }
    }

    public void sendNotification(String time){
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent open = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.alert)
                .setAutoCancel(true)
                .setContentTitle("Tsunami Warning!")
                .setContentText("Warning Effective Until " + time
                        + '\n' + "Press notification for more details.")
                .setContentIntent(open);

        NotificationManager nManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        nManager.notify(001, mBuilder.build());
    }

}
