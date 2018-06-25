package research.type.keystrokeanalysis.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import research.type.keystrokeanalysis.LastUploaded;
import research.type.keystrokeanalysis.MainActivity;
import research.type.keystrokeanalysis.R;

public class UploadData extends IntentService {
  public int lupdata=0;
    int serverResponseCode = 0;
    public Date curr_time;

    String upLoadServerUri;

    public UploadData() {
        super("UploadData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            upLoadServerUri = getString(R.string.keystrokeanalysis_server_uri)+getString(R.string.upload_data_page);
            ConnectivityManager cm =
                    (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if(isConnected) {
                File sdCardRoot = Environment.getExternalStorageDirectory();
                File dataDir = new File(sdCardRoot, getString(R.string.data_file_path));
                File zipDir = new File(sdCardRoot, getString(R.string.keystrokeanalysis_file_path));
                System.out.println("Path Testing :--- "+dataDir);
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String imei_no = telephonyManager.getDeviceId();
                final String filepath = zipDir + "/" +imei_no + "_datafiles.zip";
                File outputFile = new File(filepath);
                try {
                    packZip(outputFile, dataDir);
                    uploadFile(filepath);
                    if (outputFile.delete())
                        System.out.println("File Delete");
                    else
                        System.out.println("File Not Delete");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                lupdata=0;
                System.out.println("Connect to Internet to Upload Data");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UploadData.this,"Connect to Internet to Upload Data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    public static void packZip(File output, File source) throws IOException {
        System.out.println("Packaging to " + output.getName());
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
        zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);

        if (source.isDirectory()) {
            zipDir(zipOut, "", source);
        } else {
            zipFile(zipOut, "", source);
        }

        zipOut.flush();
        zipOut.close();
        System.out.println("Done");

    }

    private static String buildPath(String path, String file) {
        if (path == null || path.isEmpty()) {
            return file;
        } else {
            return path + "/" + file;
        }
    }

    //For Directories
    private static void zipDir(ZipOutputStream zos, String path, File dir) throws IOException {
        if (!dir.canRead()) {
            System.out.println("Cannot read " + dir.getCanonicalPath() + " (maybe because of permissions)");
            return;
        }

        File[] files = dir.listFiles();
        path = buildPath(path, dir.getName());
        System.out.println("Adding Directory " + path);

        for (File source : files) {
            if (source.isDirectory()) {
                zipDir(zos, path, source);
            } else {
                zipFile(zos, path, source);
            }
        }
    }

    //For Files
    private static void zipFile(ZipOutputStream zos, String path, File file) throws IOException {
        if (!file.canRead()) {
            System.out.println("Cannot read " + file.getCanonicalPath() + " (maybe because of permissions)");
            return;
        }
        if(file.getName().toLowerCase().endsWith(".apk")) {
            System.out.println("Skipping " + file.getName());
            return;
        }
        zos.putNextEntry(new ZipEntry(buildPath(path, file.getName())));

        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[4092];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
        }
        fis.close();
        zos.closeEntry();
    }

    public int uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {

            //  dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :" + fileName);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(UploadData.this, "Source File not exist :", Toast.LENGTH_SHORT).show();
                }
            });

            return 0;

        } else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                System.out.println("Conn:"+conn);
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                LastUploaded appState =new LastUploaded();
                appState.setLupdata(0);
                 Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(UploadData.this, "File Upload Completed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    lupdata=1;

                       appState.setLupdata(1);
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                long upload_data_interval = 1000 * 60 * 60 * Integer.parseInt(getResources().getString(R.string.upload_data_interval));
                 curr_time = new Date();
                appState.setCurrTime(curr_time);
                MainActivity.storeLastDate(appState);
                Date next_upload_data_time = new Date(curr_time.getTime() + upload_data_interval);
                store_time(getString(R.string.upload_data_timestamp), next_upload_data_time);

            } catch (MalformedURLException ex) {

                ex.printStackTrace();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();
                System.out.println("Upload file to server Exception" + "\nException : " + e.getMessage() + "\n\n" + e);
            }
            return serverResponseCode;
        }
    }
    public void store_time(String time_variable, Date value) {

        SharedPreferences eda_pref;

        eda_pref = getApplicationContext().getSharedPreferences(getResources().getString(R.string.sharedpref_file), Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor eda_editor =eda_pref.edit();
        eda_editor.putString(time_variable, convert_to_string(value));
        eda_editor.apply();
        eda_editor.commit();

    }


    public String convert_to_string(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
        String date_string = sdf.format(date);

        return date_string;
    }
}