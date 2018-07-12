package research.type.keystrokeanalysis.services;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.view.VelocityTrackerCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import research.type.keystrokeanalysis.GroupAudioDetector;
import research.type.keystrokeanalysis.R;

import static com.google.android.gms.wearable.DataMap.TAG;
import static java.lang.Math.abs;

//Service

public class CustomKeyboard  extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard symbolKeyboard;
    private Keyboard symbolShiftedKeyboard;
    private Keyboard smileyKeyboard;
    Keyboard mCurKeyboard;
    String appName;
    boolean isSymbol = false;
    boolean isSymbolShifted=false;
    boolean isSmiley=false;
    private boolean isCaps = false;
    Date prev_tap_time=null,curr_tap_time=null;
    int amplitude;double dB;
    int ifStatic=0,ifWalking=0,ifVehicle=0;

    double pressure=0.0, duration, velocity, start,end;
    double prev_pressure=0.0;
    private VelocityTracker mvelocity = null;
    double x_vel = 0.0, y_vel = 0.0;
    int n_event=1,np_event=1;
Context mContext;
    public static BufferedWriter out;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    boolean timerrecieved=false;
    File path;
    Date timerStartTime;
    int prev_primary_code;




    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mRecorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
            RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    int BufferElements2Rec = 80000; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    short data[] = new short[BufferElements2Rec];


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_layout, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        symbolKeyboard = new Keyboard(this, R.xml.int_char);
        symbolShiftedKeyboard=new Keyboard(this,R.xml.int_char_2);
        smileyKeyboard=new Keyboard(this,R.xml.smiley);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        isSymbol = false;
        isSymbolShifted=false;
        isSmiley = false;
        registerReceiver(br, new IntentFilter(TimerService.COUNTDOWN_BR));

        kv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int index = event.getActionIndex();
                int pointerId = event.getPointerId(index);

                //check for actions of motion event
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //setup velocity tracker
                    if (mvelocity == null) {
                        // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                        mvelocity = VelocityTracker.obtain();
                    } else {
                        // Reset the velocity tracker back to its initial state.
                        mvelocity.clear();
                    }
                    //obtain pressure
                    pressure = event.getPressure();
                    np_event += 1;
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mvelocity.addMovement(event);
                    mvelocity.computeCurrentVelocity(1000);
                    // Log velocity of pixels per second
                    // Best practice to use VelocityTrackerCompat where possible.
                    x_vel+= abs(VelocityTrackerCompat.getXVelocity(mvelocity,pointerId));
                    y_vel += abs(VelocityTrackerCompat.getYVelocity(mvelocity, pointerId));
                    n_event+=1;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //record time when finger lifted up
                    velocity = Math.sqrt(x_vel * x_vel + y_vel * y_vel);
                }

                // Return false to avoid consuming the touch event
                return false;
            }
        });
        return kv;
    }




    @Override
    public void onRelease(int primaryCode) {
        //record time when finger lifted up
        //calculate duration
        end = System.nanoTime();
        duration = (end-start)/1000000 ;
      //  pressure=pressure/np_event;
        if(prev_primary_code==primaryCode && pressure==0)
            pressure=prev_pressure;

        if (primaryCode == -5 || primaryCode == 32 || primaryCode == 64 || primaryCode == 42 || primaryCode == 94) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
            String currentDateandTime = sdf.format(new Date());
            String keypress = String.valueOf((char) primaryCode );
            String appName=getForegroundApp();
            String keypressed = keypress;
            if (primaryCode  == 32)
                keypressed = "0";
            if (primaryCode  == -5)
                keypressed = "1";
            if (primaryCode  == 64)
                keypressed = "2";
            if (primaryCode  ==94)
                keypressed = "3";
            if (primaryCode  == 42)
                keypressed = "4";

         //   Date date = new Date();

            Log.d("Key Pressed ", keypress);
            System.out.println("Ascii value: " + (int) keypress.charAt(0));
            System.out.println("Current app : " + appName);
            System.out.println("Timestamp: " + currentDateandTime);
            System.out.println("Duration: " + start+","+end+","+duration);
            Log.d("ans", "X velocity: " + x_vel/n_event );
            Log.d("ans", "Y velocity: " + y_vel/n_event);
            Log.d("ans", "velocity: " + velocity);
            Log.d("ans", "pressure: " + pressure);
            Log.d("ans", "duration: " + duration);
            writeToFile(currentDateandTime+ "," + appName + "," + keypressed+","+pressure+","+velocity+","+duration);

        }
    /* start = 0;
        end = 0;*/
    prev_pressure=pressure;
    prev_primary_code=primaryCode;
        x_vel = 0;
        y_vel = 0;
        n_event = 1;
        pressure = 0;
        np_event = 1;
    }


    private String getForegroundApp() {
        String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }
        PackageManager pm = this.getPackageManager();
        PackageInfo foregroundAppPackageInfo = null;
        try {
            foregroundAppPackageInfo = pm.getPackageInfo(currentApp, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            printException(e);
        }
        appName=foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
        return appName;
    }


    @Override
    public void onPress(int i) {
        prev_tap_time=curr_tap_time;
        curr_tap_time=new Date();
        long difference=0;int days1,hours1,min=0;
        if(prev_tap_time!=null)
        { difference = curr_tap_time.getTime() - prev_tap_time.getTime();
        days1 = (int) (difference / (1000*60*60*24));
        hours1 = (int) ((difference - (1000*60*60*24*days1)) / (1000*60*60));
        min = (int) (difference - (1000*60*60*24*days1) - (1000*60*60*hours1)) / (1000*60);}
      if(prev_tap_time==null || min>=1 )
      {
          if(!timerrecieved && prev_tap_time!=null)
          {File file = new File(path+"/"+mFileName);
              if (null != mRecorder) {
                  isRecording = false;
                  mRecorder.stop();
                  mRecorder.release();
                  mRecorder = null;
                  recordingThread = null;
              }
          file.delete();}
          startService(new Intent(this, TimerService.class));
        Log.i(TAG, "Started service");
      startRecording();
      timerrecieved=false;}

        start = System.nanoTime();

    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            long millisUntilFinished=11000;
            if (intent.getExtras() != null) {
                 millisUntilFinished = intent.getLongExtra("countdown", 0);}
            timerrecieved=true;
                    Log.i(TAG, "Timer received");
                   stopRecording();

        }
    };

    private void startRecording() {
       timerStartTime=new Date();
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        mRecorder.startRecording();
        isRecording = true;
      /*  recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");*/
         data = new short[BufferElements2Rec];
        recordingThread = new Thread(new Runnable() {
            public void run() {
                while(isRecording)
                mRecorder.read(data, 0, BufferElements2Rec);
            }
        }, "AudioRecorder Thread");
        recordingThread.start();


    }

    private void stopRecording() {
        Log.i(TAG, "Recorder released");
        stopService(new Intent(this, TimerService.class));
        double[] samples=convertShortToDoubleArray(data);
        amplitude=(int)getMaxAmplitude(samples);
        dB=20 * Math.log10(amplitude );
        if (null != mRecorder) {
            isRecording = false;
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            recordingThread = null;
        }
       Log.d("Amplitude","amp-"+dB);
        if(dB>60) {
            ifStatic = 1;
            ifVehicle=5;
        }
        else {
            ifStatic = 2;
         if (dB >= 40 && dB<= 60)
                ifVehicle=6;
            else
               ifVehicle=7;
        }

        boolean isGroup= GroupAudioDetector.isGroupFunc(samples);
        if(isGroup)
           ifWalking=4;
        else
            ifWalking=3;
        writeToAudioFile(timerStartTime+","+dB+","+ifStatic+","+ifWalking+","+ifVehicle);
        File file= new File(path + "/" + mFileName);
       // file.delete();
        mRecorder = null;
    }

    public static double getMaxAmplitude(double[] arr){
        double max=Double.NEGATIVE_INFINITY;
        for(int i=0;i<arr.length;i++){
            if(max<arr[i])
                max=arr[i];
        }
        return(max);
    }

    public static double[] convertShortToDoubleArray(short[] arr){
        double[] out=new double[arr.length];
        for(int i=0;i<arr.length;i++){
            out[i]=(double)arr[i];
        }
        return(out);
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, TimerService.class));
        Log.i(TAG, "Stopped service");
        super.onDestroy();
    }


    @Override
    public void onKey(int i, int[] ints) {

        InputConnection ic = getCurrentInputConnection();
        String s = "";
        playClick(i);
         try
        {
            switch (i) {
                case Keyboard.KEYCODE_DELETE: {
                    CharSequence selectedText = ic.getSelectedText(0);
                    CharSequence text=  ic.getTextBeforeCursor(2,0);
                    if(text.length()>1 && Character.isSurrogatePair(text.charAt(0), text.charAt(1)))
                        ic.deleteSurroundingText(2, 0);
                   else if (TextUtils.isEmpty(selectedText)) {
                        // no selection, so delete previous character
                        ic.deleteSurroundingText(1, 0);
                    } else {
                        // delete the selection
                       ic.commitText("", 1);
                    }

                }
                break;
                case Keyboard.KEYCODE_SHIFT: {
                    isCaps = !isCaps;
                    if (isCaps) {
                        Keyboard.Key shiftedKey = keyboard.getKeys().get(keyboard.getShiftKeyIndex());
                        //noinspection deprecation
                        shiftedKey.icon = getResources().getDrawable(R.drawable.sym_keyboard_shift_2);
                    } else {
                        Keyboard.Key shiftedKey = keyboard.getKeys().get(keyboard.getShiftKeyIndex());
                        //noinspection deprecation
                        shiftedKey.icon = getResources().getDrawable(R.drawable.sym_keyboard_shift);
                    }
                    keyboard.setShifted(isCaps);
                    kv.invalidateAllKeys();

                }
                break;
                case Keyboard.KEYCODE_DONE: {
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                }
                break;
                case -2: {

                        kv.setKeyboard(symbolKeyboard);
                        mCurKeyboard = symbolKeyboard;
                    kv.setOnKeyboardActionListener(this);
                    isSymbol = !isSymbol;
                    isSmiley=false;
                }
                break;
                case -222: {
                     {
                        kv.setKeyboard(keyboard);
                        mCurKeyboard = keyboard;
                    }
                    kv.setOnKeyboardActionListener(this);
                    isSymbol = !isSymbol;
                    isSmiley=false;
                }
                break;
                case -111:{
                    if (!isSymbolShifted ) {
                        kv.setKeyboard(symbolShiftedKeyboard);
                        mCurKeyboard = symbolShiftedKeyboard;
                    } else {
                        kv.setKeyboard(symbolKeyboard);
                        mCurKeyboard = symbolKeyboard;
                    }
                    kv.setOnKeyboardActionListener(this);
                    isSymbolShifted = !isSymbolShifted;
                }
                break;
                case -9:{
                        kv.setKeyboard(smileyKeyboard);
                        mCurKeyboard = smileyKeyboard;
                    kv.setOnKeyboardActionListener(this);
                    isSmiley=true;
                }
                break;
                default: {
                    if(i>=97 && i<=122) {
                        char code = (char) i;
                        if (Character.isLetter(code) && isCaps)
                            code = Character.toUpperCase(code);
                        ic.commitText(String.valueOf(code), 1);
                    }
                    else{
                        String emoji=new String(Character.toChars(i));
                        ic.commitText(emoji,1);

                    }
                }
            }
        }catch (Exception e){
             e.printStackTrace();
             printException(e);
         }

    }

    @Override
    public void onText(CharSequence charSequence) {

    }


    private void playClick(int i) {

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (i) {
            case 32:
                if (am != null) {
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                }
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                if (am != null) {
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                }
                break;
            case Keyboard.KEYCODE_DELETE:
                if (am != null) {
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                }
                break;
            default:
                if (am != null) {
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
                }
        }
    }


    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    void printException(Exception e) {

        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString() + "\n\n";
        for (int j = 0; j < arr.length; j++) {
            report += "    " + arr[j].toString() + "\n";
        }
        report += "-------------------------------------------------------------------------------------------\n\n";
        File sdCardRoot = Environment.getExternalStorageDirectory();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei_no = telephonyManager.getDeviceId();
        Date date = new Date();
        String date1 = date.getDate() + "-" + date.getMonth() + "-" + date.getYear();
        File exceptionDir = new File(sdCardRoot, "/KeystrokeAnalysis/DataFiles/Exception/");
        String ExceptionTraceFileName = imei_no + " " + date1 + " Exception.log";
        File ExceptionTraceFile = new File(exceptionDir, ExceptionTraceFileName);
        System.out.println(ExceptionTraceFile);
        try {
            FileOutputStream trace = new FileOutputStream(ExceptionTraceFile, true);
            trace.write(report.getBytes());
            trace.close();
        } catch (IOException ioe) {
         ioe.printStackTrace();
        }

    }


    public void writeToFile(String message) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei_no = telephonyManager.getDeviceId();
        Date date = new Date();
        String date1 = date.getDate() + "-" + date.getMonth() + "-" + date.getYear();
        File logDir = new File(sdCardRoot, "/KeystrokeAnalysis/DataFiles/Log/");
        String LogFileName = imei_no + " " + date1 + " Log.txt";
        File LogFile = new File(logDir, LogFileName);
        FileWriter LogWriter = null;
        try {
            LogWriter = new FileWriter(LogFile, true);
        } catch (IOException e) {
            e.printStackTrace();
            printException(e);
        }
        out = new BufferedWriter(LogWriter);
        try {
            out.write(message + "\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            printException(e);
        }

    }

    public void writeToAudioFile(String message) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File Dir = new File(sdCardRoot, "/KeystrokeAnalysis/DataFiles/");
        String LogFileName ="Audio.txt";
        File LogFile = new File(Dir, LogFileName);
        FileWriter LogWriter = null;
        try {
            LogWriter = new FileWriter(LogFile, true);
        } catch (IOException e) {
            e.printStackTrace();
            printException(e);
        }
        out = new BufferedWriter(LogWriter);
        try {
            out.write(message + "\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            printException(e);
        }

    }



}

