package research.type.keystrokeanalysis;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import research.type.keystrokeanalysis.services.ActivityService;
import research.type.keystrokeanalysis.services.UploadData;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    Button startApp,stopApp;
    public static BufferedWriter out;
    public GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startApp=findViewById(R.id.btnStart_service);
        stopApp=findViewById(R.id.btnStop_service);

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
            String LogFileName = imei_no + " " + date1 + " Log.txt";
            File LogFile = new File(logDir, LogFileName);
            FileWriter LogWriter = null;
           try {
                LogWriter = new FileWriter(LogFile,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out = new BufferedWriter(LogWriter);
            try {
                out.write("");
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


        try{mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();}
        catch(Exception e){}

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            Intent intent = new Intent(this, ActivityService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 300000, pendingIntent);
        }
        catch(Exception ex)
        {

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
