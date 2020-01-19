package com.e.bookmarktimer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.IntentService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.media.VolumeProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private EditText input;
    private Button set;
    private Button reset;
    private Button start;
    private TextView display;

    private int setTime_minutes = 0;
    private String displayText;

    private CountDownTimer clock;

    private boolean running = false;

    private ProgressBar progressBarCircle;

    private State currentState;
    private long remainingTime_mille;
    private LinearLayout buttonLayout;

    private SharedPreferences storage;
    private SharedPreferences.Editor storageEditor;
    public static final String PREFS_NAME = "StorageFile";

    private Map<Button, Integer> buttonToMinutes;




  private HashMap<Button, Integer> buttonPresetTime = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.edit_text_input);
        set = findViewById(R.id.button_set);
        reset = findViewById(R.id.button_reset);
        start = findViewById(R.id.button_start_pause);
        display = findViewById(R.id.text_view_countdown);
        progressBarCircle = findViewById(R.id.progressBarCircle);
        currentState = State.initial;
        buttonLayout = findViewById(R.id.ButtonLayout);
        storage = getSharedPreferences(PREFS_NAME, 0);
        storageEditor = storage.edit();

        buttonToMinutes = new HashMap<>();

        Set<String> presetTimers = new HashSet<String>();
        if (!storage.contains("pTimers"))
        {
            Log.d("presets", "not contains");
            storageEditor.putStringSet("pTimers", presetTimers);
            storageEditor.apply();
        }
        else{
            Log.d("presets", "contains");
            presetTimers = storage.getStringSet("pTimers", presetTimers);
        }

        Log.d("howmuch", Integer.toString(storage.getAll().size()));
        for (String s : presetTimers){
            Log.d("presetButtons", s);
            int minutes = storage.getInt(s, 0);

            saveButton(minutes);
        }



    }


    public void onSave(View v){
        storageEditor = storage.edit();

        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibe.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibe.vibrate(150);
        }

        // Output yes if can vibrate, no otherwise
        if (vibe.hasVibrator()) {
            Log.v("Can Vibrate", "YES");
        } else {
            Log.v("Can Vibrate", "NO");
        }

//        if (storage.contains(Integer.toString(setTime_minutes)))
//        {
//
//        }

            saveButton(setTime_minutes);
            storageEditor.putInt(Integer.toString(setTime_minutes), setTime_minutes);
            Set<String> presetTimers = storage.getStringSet("pTimers", new HashSet<String>());
            presetTimers.add(Integer.toString(setTime_minutes));
            storageEditor.putStringSet("pTimers",presetTimers);
            storageEditor.apply();
//        }

    }

    public void saveButton(int setTime_min){
        Log.d("tried to make button", Integer.toString(setTime_min));
        int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getApplicationContext().getResources().getDisplayMetrics());
        StringBuilder SB = new StringBuilder();

        Button b = new Button(getApplicationContext());
        b.setLayoutParams(new ViewGroup.LayoutParams(

                dp*150,
//                ViewGroup.LayoutParams.MATCH_PARENT,
                dp*150
        ));

        b.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        b.setGravity(Gravity.CENTER);
        //b.setPadding();
        b.setText(makeDisplay(setTime_min / 60, setTime_min % 60, 0));
        b.setTextColor(Color.parseColor("#ffffff"));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibe.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    vibe.vibrate(150);
                }

                // Output yes if can vibrate, no otherwise
                if (vibe.hasVibrator()) {
                    Log.v("Can Vibrate", "YES");
                } else {
                    Log.v("Can Vibrate", "NO");
                }

                if ((Button) v == null){
                    Log.d("error", "you were wrong about buttons");
                }
                int buttonTime = buttonToMinutes.get(v);

                buttonPresetTime.remove(v);
                buttonLayout.removeView(v);

                storageEditor.remove(Integer.toString(buttonTime));

                Set<String> presetTimers = storage.getStringSet("pTimers", new HashSet<String>());
                presetTimers.remove(Integer.toString(buttonTime));
                storageEditor.putStringSet("pTimers",presetTimers);
                storageEditor.apply();

                return true;
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReset(v);
                setTime_minutes = buttonPresetTime.get(v);
                onStart_Pause(v);
            }
        });
        buttonToMinutes.put(b, setTime_min);

        buttonPresetTime.put(b, setTime_min);
        buttonLayout.addView(b);
//        buttonList.add(b);
    }

    public void onSet(View v){
        String inputText = input.getText().toString();
        if(inputText == null || inputText.equals("")){
            setTime_minutes = 0;
            setDisplay(0,0,0);
            return;
        }


        setTime_minutes = Integer.parseInt(inputText);

        int displayedHours = setTime_minutes / 60;
        int displayedMinutes = setTime_minutes % 60;

        setDisplay(displayedHours, displayedMinutes, 0);


    }

    public void setDisplay(int hours, int minutes, int seconds){
        display.setText(makeDisplay(hours, minutes, seconds));
    }

    public void onReset(View v){
        if (clock != null)
            clock.cancel();
        setDisplay(0,0,0);
        setTime_minutes = 0;
//        progressBarCircle.setProgress((int) (setTime_minutes * 60 * 1000) / 1000);
        progressBarCircle.setProgress(0);
        currentState = State.initial;

    }
    public String makeDisplay(int h, int m, int s){
        StringBuilder sb = new StringBuilder();
        if (h < 10){
            sb.append("0");
        }
        sb.append(h);
        sb.append(":");
        if (m < 10){
            sb.append("0");
        }
        sb.append(m);
        sb.append(":");
        if (s < 10){
            sb.append("0");
        }
        sb.append(s);
        return sb.toString();
    }

    public void onStart_Pause(View v){
        switch(currentState){
            case initial:
                startTimer(setTime_minutes);
                setProgressBarValues();
                break;
            case running:
                pauseTimer();
                setProgressBarValues();
                break;
            case paused:
                unpauseTimer();
                break;
        }
    }

    public void startTimer(int setTime){

        int timerTimer_mille = 1000*60*setTime;

        if (setTime == 0){
            timerTimer_mille = 5000;
        }

        currentState = State.running;
        Log.d("button", "start");

        clock = new CountDownTimer(timerTimer_mille, 1000){
            @Override
            public void onTick(long millisUntilFinished) {

                long totalSeconds = millisUntilFinished/1000;
                long seconds = totalSeconds % 60;
                long minutes = (totalSeconds / 60) % 60;
                long hours = totalSeconds / 3600;

                remainingTime_mille = millisUntilFinished;
                setDisplay((int) hours, (int) minutes, (int) seconds);
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                currentState = State.initial;
                setProgressBarValues();
                finish();
            }
        };
        clock.start();
    }

    public void pauseTimer(){
        currentState = State.paused;
        Log.d("button", "pause");
        clock.cancel();
    }

    private void setProgressBarValues(){
        progressBarCircle.setMax((int) (setTime_minutes * 1000 * 60) / 1000);
        progressBarCircle.setProgress((int) remainingTime_mille / 1000);
    }

    public void unpauseTimer(){
        Log.d("button", "unpause");
        currentState = State.running;
        clock = new CountDownTimer(remainingTime_mille, 1000){
            @Override
            public void onTick(long millisUntilFinished) {

                long totalSeconds = millisUntilFinished/1000;
                long seconds = totalSeconds % 60;
                long minutes = (totalSeconds / 60) % 60;
                long hours = totalSeconds / 3600;

                remainingTime_mille = millisUntilFinished;
                setDisplay((int) hours, (int) minutes, (int) seconds);
                progressBarCircle.setProgress((int) (remainingTime_mille / 1000));
            }

            @Override
            public void onFinish() {
                currentState = State.initial;
                finish();
            }
        };
        clock.start();
    }


    public enum State{
        initial,
        running,
        paused
    }

    public void finish(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(6000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(6000);
        }

        // Output yes if can vibrate, no otherwise
        if (v.hasVibrator()) {
            Log.v("Can Vibrate", "YES");
        } else {
            Log.v("Can Vibrate", "NO");
        }

//        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 2500);
//        toneGen.startTone(ToneGenerator.TONE_SUP_RINGTONE, 2000);

        try{
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    r.stop();
                }
            }, 6000);
        }
        catch (Exception e){
            e.printStackTrace();
        }



    }
}
