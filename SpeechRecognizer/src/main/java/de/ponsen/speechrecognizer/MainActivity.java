package de.ponsen.speechrecognizer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    ImageButton startListeningButton;
    Animation breath;
    TextView speech_error_txt, speech_text, rest_result_txt;

    //settings
    boolean settingRestartSTT;
    boolean settingEnableHTTP;
    String settingServerURL;
    int settingServerPort;
    String settingServerEndpoint;
    int settingWaitInMilis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        speech_error_txt = (TextView) findViewById(R.id.speech_error_txt);
        speech_text = (TextView) findViewById(R.id.txtSpeechInput);

        rest_result_txt = (TextView) findViewById(R.id.rest_result_txt);

        breath = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.breath_anim);

        startListeningButton = (ImageButton) findViewById(R.id.btnSpeak);

        startListeningButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if(!v.isPressed()){
                        v.startAnimation(breath);
                        Log.d(TAG, "startListening");
                        AssetManager am = getAssets();
                        File file = null;
                        try {
                            file = createFileFromInputStream("temporary_audio", am.open("assets/audio.raw"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            NonStreamingRecognizeClient.start(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        v.clearAnimation();
                        Log.d(TAG, "stopListening");

                    }
                    v.setPressed(!v.isPressed());
                }
                return true;//Return true, so there will be no onClick-event
            }
        });
    }

    private File createFileFromInputStream(String my_file_name, InputStream inputStream) {

        try {
            File f = new File(my_file_name);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            //Logging exception
        }
        return null;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        //applying settings here to we get the changes
        settingRestartSTT = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getResources().getString(R.string.pref_continous_key), false);

        settingEnableHTTP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getResources().getString(R.string.pref_switch_sendResult_key), false);

        String milis = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getResources().getString(R.string.pref_wait_milis_key), "1000");
        if(milis.equals(""))
            milis = "1000";
        settingWaitInMilis = Integer.valueOf(milis);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
