package com.example.adrija.keystrokeanalysis;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button startApp,stopApp,dispStat;
    public static BufferedWriter out;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startApp=findViewById(R.id.btnStart_service);
        stopApp=findViewById(R.id.btnStop_service);
        //dispStat=findViewById(R.id.btnDisp_Stat);

        File sdCardRoot = Environment.getExternalStorageDirectory();

        File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));
        File logDir = new File(sdCardRoot, getResources()
                .getString(R.string.data_file_path) + getResources().getString(R.string.log_file_path));
        File exceptionDir = new File(sdCardRoot, getResources()
                .getString(R.string.data_file_path) + getResources().getString(R.string.exception_file_path));



        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        if (!exceptionDir.exists()) {
            exceptionDir.mkdirs();
        }


        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei_no = telephonyManager.getDeviceId();



        if(sdCardRoot.canWrite()){
            Date date = new Date();
            String date1=date.getDate()+"-"+date.getMonth()+"-"+date.getYear();
            String LogFileName = imei_no+" "+date1+" Log.txt";
            File LogFile = new File(logDir, LogFileName);
            FileWriter LogWriter = null;
            try {
                LogWriter = new FileWriter(LogFile,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = new BufferedWriter(LogWriter);
            try {
                out.write("Time"+"\tApp Name\t"+"Key Pressed\t"+"Pressure\t"+"Velocity\t"+"Duration\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }







        startApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,UserConsent.class);
                startActivity(intent);

            }
        });

        stopApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              /* stopService(new Intent(getBaseContext(),CustomKeyboard.class));*/
                Intent intent = new Intent(MainActivity.this,UploadData.class);
                 startService(intent);

            }

        });

  /*      dispStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,Statistics.class);
                startActivity(intent);


            }
        });*/
    }





}
