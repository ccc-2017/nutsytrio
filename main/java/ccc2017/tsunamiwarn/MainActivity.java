package ccc2017.tsunamiwarn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button xmlShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xmlShow = (Button) findViewById(R.id.button);
        Intent intent = new Intent(this, AlertNotification.class);
        setAlarm(intent, this);
        Handler handler =new Handler();

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                new getNoaaAPI().execute();
            }
        };
        handler.post(r);
        handler.postDelayed(r, 15*60*1000);
    }


    //Checks for tsunami warning every 30 minutes, if there is, sends a notification.
    private void setAlarm(Intent intent, Context context){
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime()+(30*60*1000), 30*60*1000, alarmIntent);
    }

    //Sample of how to access browser and open up link
    public void goToMaps(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.honolulu.gov/demevacuate/tsunamimaps.html"));
        startActivity(browserIntent);
    }

    public void goToHelp(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ready.gov/tsunamis"));
        startActivity(browserIntent);
    }


    class getNoaaAPI extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL("https://alerts.weather.gov/cap/hi.php?x=0");
                //URL url = new URL("https://alerts.weather.gov/cap/hi.php?x=0");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String xmlData){
            List noaaInfo = null;

            try{
                noaaXMLParser xmlParser = new noaaXMLParser();
                InputStream xmlDataReader = new ByteArrayInputStream(xmlData.getBytes());
                try {
                    noaaInfo = xmlParser.parse(xmlDataReader);
                }finally{
                        xmlDataReader.close();
                }
            } catch (Exception e) {
                xmlShow.setText(e.getMessage());
            }

            checkTsunami(noaaInfo);
        }

        private void checkTsunami(List<Entry> feed){
            for(Entry entry : feed){
                if(entry.event.equals("Tsunami Warning")){
                    xmlShow.setText("Hawaii is currently under a " + entry.event
                            + "\nWarning Effective Until " + entry.time);
                    xmlShow.setBackgroundColor(Color.RED);
                    return;
                }
            }
            xmlShow.setText("There is no Tsunami Warning currently effective");
            xmlShow.setBackgroundColor(Color.BLUE);
        }
    }
}
