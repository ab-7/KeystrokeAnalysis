package com.example.adrija.keystrokeanalysis;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPClient;



public class UploadData extends IntentService {

    public UploadData() throws IOException {
        super("UploadData");
    }

    @Override
  protected void onHandleIntent(Intent intent) {


        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {

            String ftpServer = "10.147.45.199";
            String user = "FTP-User";
            String password = "Keystroke18";

            FTPClient ftpClient = new FTPClient();

            try {
                // connect and login to the server
                ftpClient.connect(ftpServer, 21);
                ftpClient.login(user, password);

                // use local passive mode to pass firewall
                ftpClient.enterLocalPassiveMode();

                System.out.println("Connected");

                String remoteDirPath = "/KeystrokeAnalysis/DataFiles/Log";//Path of the destination directory on the server
                File sdCardRoot = Environment.getExternalStorageDirectory();
                File logDir = new File(sdCardRoot, "/KeystrokeAnalysis/DataFiles/Log");
                String localDirPath = String.valueOf(logDir);//Path of the local directory being uploaded
                FTPUtil.uploadDirectory(ftpClient, remoteDirPath, localDirPath, "");

                remoteDirPath = "/KeystrokeAnalysis/DataFiles/Exception";//Path of the destination directory on the server
                File exceptionDir = new File(sdCardRoot, "/KeystrokeAnalysis/DataFiles/Exception");
                localDirPath = String.valueOf(exceptionDir);//Path of the local directory being uploaded

                boolean success=FTPUtil.uploadDirectory(ftpClient, remoteDirPath, localDirPath, "");

                // log out and disconnect from the server
                ftpClient.logout();
                ftpClient.disconnect();

                System.out.println("Disconnected");

                if(success)
                {Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(UploadData.this, "Data is uploaded!", Toast.LENGTH_SHORT).show();
                    }
                });}

            } catch (IOException ex) {
                ex.printStackTrace();
            }


        }else {
            System.out.println("Connect to Internet to Upload Data");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(UploadData.this, "Connect to Internet to Upload Data", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

}
