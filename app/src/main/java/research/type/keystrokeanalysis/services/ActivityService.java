package research.type.keystrokeanalysis.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import research.type.keystrokeanalysis.R;

/**
 * Created by soumya on 24/3/18.
 */

public class ActivityService extends IntentService {

    public ActivityService() {
        super("ActivityRecognizedService");
    }

    public ActivityService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                handleDetectedActivities(result.getMostProbableActivity());
            }
        }
        catch(Exception ex)
        {

        }
    }
    private void handleDetectedActivities(DetectedActivity mostProbableActivities) {
        try {
            String activityData = "";
            double confidence=0.0;
            DetectedActivity activity=mostProbableActivities;
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "In Vehicle: " + Double.toString(confidence));
                    activityData = "In Vehicle: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "On Bicycle: " + Double.toString(confidence));
                    activityData = "On Bicycle: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "On Foot: " + Double.toString(confidence));
                    activityData = "On Foot: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.RUNNING: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "Running: " + Double.toString(confidence));
                    activityData = "Running: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.STILL: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "Still: " + Double.toString(confidence));
                    activityData = "Static: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.TILTING: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "Tilting: " + Double.toString(confidence));
                    activityData = "Tilting: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.WALKING: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "Walking: " + Double.toString(confidence));
                    activityData = "Walking: " + Double.toString(confidence);
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    confidence=activity.getConfidence();
                    Log.e("ActivityRecogition", "Unknown: " + Double.toString(confidence));
                    activityData = "Unknown: " + Double.toString(confidence);
                    break;
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.time_format));
            String currentDateandTime = sdf.format(new Date());
            activityData = currentDateandTime + "|" + activityData + "\n";
            File sdCardRoot = Environment.getExternalStorageDirectory();
            File dataDir = new File(sdCardRoot, getResources().getString(R.string.data_file_path));

            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            String activity_file_name = "activities.txt";
            File alarm_file = new File(dataDir, activity_file_name);
            byte[] ground_truth = activityData.getBytes();
            try {

                FileOutputStream fos;
                fos = new FileOutputStream(alarm_file, true);
                fos.write(ground_truth);
                fos.close();
            } catch (IOException e) {
                //Log.e("Exception", "File write failed: " + e.toString());
            }
        }
        catch(Exception ex)
        {

        }
    }
}

