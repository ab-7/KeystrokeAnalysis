package com.example.adrija.keystrokeanalysis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.inputmethod.InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
import static java.lang.Math.abs;

//Service

public class CustomKeyboard  extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard symbolKeyboard;
    private Keyboard symbolShiftedKeyboard;
    private Keyboard smileyKeyboard;
    Keyboard mCurKeyboard;
    Context context;
    String appName;
    boolean isSymbol = false;
    boolean isSymbolShifted=false;
    boolean isSmiley=false;
    private boolean isCaps = false;


    public static BufferedWriter out;


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
        return kv;
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
        }
        appName=foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
        return appName;
    }


    @Override
    public void onPress(int i) {


    }

    @Override
    public void onRelease(int i) {


    }

    @Override
    public void onKey(int i, int[] ints) {

        InputConnection ic = getCurrentInputConnection();
        String s = "";
        playClick(i);

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
        }

        if (i == -5 || i == 32 || i == 64 || i == 42 || i == 94) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
            String currentDateandTime = sdf.format(new Date());
            String keypress = String.valueOf((char) i);
            String appName=getForegroundApp();
            String keypressed = keypress;
            if (i == -5)
                keypressed = "backspace";
            if (i == 32)
                keypressed = "space";

            Date date = new Date();

            Log.d("Key Pressed ", keypressed);
            System.out.println("Ascii value: " + (int) keypress.charAt(0));
            System.out.println("Current app : " + appName);
            System.out.println("Timestamp: " + currentDateandTime);
            writeToFile(date + "\t" + appName + "\t" + keypressed);
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
// ...
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
        }
        out = new BufferedWriter(LogWriter);
        try {
            out.write(message + "\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

