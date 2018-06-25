package research.type.keystrokeanalysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import research.type.keystrokeanalysis.services.ActivityService;
import research.type.keystrokeanalysis.services.NotificationHelper;

/**
 * Created by Adrija on 08-06-2018.
 */

public class AlarmBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //only enabling one type of notifications for demo purposes
          //  NotificationHelper.scheduleRepeatingRTCNotification(context);
            Intent myIntent = new Intent(context, ActivityService.class);
            context.startService(myIntent);
            Intent intent1 = new Intent(context, NotificationHelper.class);
            context.startService(intent1);
        }
    }
}
