package xardas.gamestracker.service;

import xardas.gamestracker.database.GameDAO;
import xardas.gamestracker.giantbomb.api.Game;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DatabaseUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (action.equals("STOP")) {
			long game = intent.getLongExtra("gameId", -1);
			GameDAO dao = new GameDAO(context);
			Game g = dao.getGame(game);
			g.setNotify(false);
			dao.updateGame(g);
			notificationManager.cancel((int) game);
		} else if (action.equals("DELETE")) {
			long game = intent.getLongExtra("gameId", -1);
			GameDAO dao = new GameDAO(context);
			Game g = dao.getGame(game);
			dao.deleteGame(g);
			notificationManager.cancel((int) game);
		} else if (action.equals("VOID")) {
			long game = intent.getLongExtra("gameId", -1);
			notificationManager.cancel((int) game);
		}
	}

}
