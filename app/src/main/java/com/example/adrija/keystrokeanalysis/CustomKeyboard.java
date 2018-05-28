package com.example.adrija.keystrokeanalysis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.telephony.TelephonyManager;
import android.text.InputType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

//Service

public class CustomKeyboard  extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard symbolKeyboard;
    Keyboard mCurKeyboard;
    Context context;
    String appName;
    boolean isSymbol = false;

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
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        isSymbol = false;
        return kv;
    }


    private ActivityManager.RunningAppProcessInfo getForegroundApp() {
        ActivityManager.RunningAppProcessInfo result = null, info;
        ActivityManager mActivityManager = (ActivityManager) CustomKeyboard.this.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager == null)
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = null;
        if (mActivityManager != null) {
            l = mActivityManager.getRunningAppProcesses();
        }
        Iterator<ActivityManager.RunningAppProcessInfo> i = null;
        if (l != null) {
            i = l.iterator();
        }
        if (i != null) {
            while (i.hasNext()) {
                info = i.next();
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    result = info;
                    break;
                }
            }
        }
        return result;
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
                    ic.deleteSurroundingText(1, 0);
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
                case Keyboard.KEYCODE_MODE_CHANGE: {
                    if (!isSymbol) {
                        kv.setKeyboard(symbolKeyboard);
                        mCurKeyboard = symbolKeyboard;
                    } else {
                        kv.setKeyboard(keyboard);
                        mCurKeyboard = keyboard;
                    }
                    kv.setOnKeyboardActionListener(this);
                    isSymbol = !isSymbol;
                }
                break;
                default: {
                    char code = (char) i;
                    if(Character.isLetter(code) && isCaps)
                        code = Character.toUpperCase(code);
                    ic.commitText(String.valueOf(code),1);
                }
            }
        }

        if (i == -5 || i == 32 || i == 64 || i == 42 || i == 94) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
            String currentDateandTime = sdf.format(new java.util.Date());
            String keypress = String.valueOf((char) i);
            String keypressed = keypress;
            if (i == -5)
                keypressed = "backspace";
            if (i == 32)
                keypressed = "space";
            ActivityManager.RunningAppProcessInfo currentFg = getForegroundApp();

            PackageManager pm = this.getPackageManager();
            CharSequence c = null;
            try {
                c = pm.getApplicationLabel(pm.getApplicationInfo(currentFg.processName, PackageManager.GET_META_DATA));
                appName = c.toString();
            } catch (PackageManager.NameNotFoundException e) {
                appName = currentFg.processName;
                e.printStackTrace();
                printException(e);


            }
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

