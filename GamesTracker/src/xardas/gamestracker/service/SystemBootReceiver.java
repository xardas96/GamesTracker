package xardas.gamestracker.service;

import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent service = new Intent(context, ReleaseDateNotificationService.class);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, service, 0);
			AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			DateTime dateTime = new DateTime();
			alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, dateTime.getMillis(), 6 * AlarmManager.INTERVAL_HOUR, pendingIntent);
		}
	}
}
