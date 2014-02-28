package xardas.gamestracker.service;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;

import xardas.gamestracker.MainActivity;
import xardas.gamestracker.R;
import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.Game;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ReleaseDateNotificationService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int toNotify = loadDaysToNotify();
		GameDAO gameDAO = new GameDAO(this);
		List<Game> games = gameDAO.getAllGames();
		Intent stopNotifyIntent = new Intent(this, DatabaseUpdateReceiver.class);
		stopNotifyIntent.setAction("STOP");
		stopNotifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Intent deleteIntent = new Intent(this, DatabaseUpdateReceiver.class);
		deleteIntent.setAction("DELETE");
		deleteIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Intent voidIntent = new Intent(this, DatabaseUpdateReceiver.class);
		voidIntent.setAction("VOID");
		voidIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Intent openMain = new Intent(this, MainActivity.class);
		PendingIntent openMainPendingIntent = PendingIntent.getActivity(this, 0, openMain, 0);
		DateTime now = new DateTime();
		for (Game game : games) {
			if (game.isNotify()) {
				stopNotifyIntent.putExtra("gameId", game.getId());
				deleteIntent.putExtra("gameId", game.getId());
				voidIntent.putExtra("gameId", game.getId());
				PendingIntent stopNotifyPendingIntent = PendingIntent.getBroadcast(this, 0, stopNotifyIntent, PendingIntent.FLAG_ONE_SHOT);
				PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_ONE_SHOT);
				PendingIntent voidPendingIntent = PendingIntent.getBroadcast(this, 0, voidIntent, PendingIntent.FLAG_ONE_SHOT);
				DateTime releaseDate = game.getReleaseDate();
				Days days = Days.daysBetween(now, releaseDate);
				int daysDifference = days.getDays();
				if (toNotify >= daysDifference) {
					Notification n = new NotificationCompat.Builder(this).setContentTitle(game.getName()).setContentText(getResources().getString(R.string.game_release) + " " + game.getReleaseDate().toLocalDate().toString()).setSmallIcon(R.drawable.app_icon).setAutoCancel(true).setContentIntent(openMainPendingIntent).addAction(R.drawable.timer_again, "", voidPendingIntent).addAction(R.drawable.timer_stop, "", stopNotifyPendingIntent).addAction(R.drawable.star_delete, "", deletePendingIntent).build();
					n.defaults |= Notification.DEFAULT_VIBRATE;
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.notify((int) game.getId(), n);
				}
			}
		}
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private int loadDaysToNotify() {
		return 7; // TODO
	}

}
