package research.type.keystrokeanalysis.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import research.type.keystrokeanalysis.AlarmBootReceiver;
import research.type.keystrokeanalysis.AlarmReceiver;

/**
 * Created by Adrija on 08-06-2018.
 */

public class NotificationHelper extends IntentService{
    public static int ALARM_TYPE_RTC = 100;
    private static AlarmManager alarmManagerRTC;
    private static PendingIntent alarmIntentRTC;

    Context context=this;
    public int hour;

    public NotificationHelper() {
        super("Notification Helper");
    }



    public static void cancelAlarmRTC() {
        if (alarmManagerRTC!= null) {
            alarmManagerRTC.cancel(alarmIntentRTC);
        }
    }



    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Enable boot receiver to persist alarms set for notifications across device reboots
     */
    public static void enableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Disable boot receiver when user cancels/opt-out from notifications
     */
    public static void disableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //get calendar instance to be able to select what time notification should be scheduled

        Calendar setcalendar = Calendar.getInstance();
        Date date=new Date();
        int hr= date.getHours();
        hour=(((24-hr)%4)+hr)%24;
        if(hr%4==0 && date.getMinutes()>0)
            hour=hr+4;
            setcalendar.set(Calendar.HOUR_OF_DAY,hour);
            setcalendar.set(Calendar.MINUTE,0);
            setcalendar.set(Calendar.SECOND, 0);

        enableReceiver(context);

        //Setting intent to class where Alarm broadcast message will be handled
        Intent intent1 = new Intent(context, AlarmReceiver.class);
        //Setting alarm pending intent
        alarmIntentRTC = PendingIntent.getBroadcast(context, ALARM_TYPE_RTC, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        //getting instance of AlarmManager service
        alarmManagerRTC = (AlarmManager)context.getSystemService(ALARM_SERVICE);

        //Setting alarm to wake up device every day for clock time.
        //AlarmManager.RTC_WAKEUP is responsible to wake up device for sure, which may not be good practice all the time.
        // Use this when you know what you're doing.
        //Use RTC when you don't need to wake up device, but want to deliver the notification whenever device is woke-up
        //We'll be using RTC.WAKEUP for demo purpose only
        alarmManagerRTC.setRepeating(AlarmManager.RTC_WAKEUP, setcalendar.getTimeInMillis(),
                14400000, alarmIntentRTC);
    }
}
