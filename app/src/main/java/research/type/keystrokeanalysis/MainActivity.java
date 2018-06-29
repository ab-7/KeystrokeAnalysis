package research.type.keystrokeanalysis;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import research.type.keystrokeanalysis.services.ActivityService;
import research.type.keystrokeanalysis.services.NotificationHelper;
import research.type.keystrokeanalysis.services.UploadData;

import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    Button startApp,stopApp;
    public static BufferedWriter out;
    public GoogleApiClient mApiClient;
    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent transitionPendingIntent;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private Context mContext;
    TextView t;
    static int uploaded;static Date dateuploaded;

    // Requesting permission to RECORD_AUDIO


    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }

    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t=findViewById(R.id.lastupdated);
        mContext = getApplicationContext();
        NotificationHelper.enableBootReceiver(mContext);
        startApp=findViewById(R.id.btnStart_service);
        stopApp=findViewById(R.id.btnStop_service);
     //   ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        if(dateuploaded!=null)
        t.setText("Last Uploaded: "+dateuploaded);
        else
            t.setText("Last Uploaded: Never");

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
            String date1 = date.getDate() + "-" + date.getMonth() + "-" + date.getYear();
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
        Intent intent = new Intent(this, NotificationHelper.class);
        startService(intent);

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
                 if(uploaded==1)
                     t.setText("Last Uploaded: "+dateuploaded);

            }

        });


        try{mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();}
        catch(Exception e){}

        activityRecognitionClient = ActivityRecognition.getClient(mContext);

        requestAudioPermissions();

    }

    public static void storeLastDate(LastUploaded lupdate){
 dateuploaded=lupdate.getCurrTime();
 uploaded=lupdate.getLupdata();
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
