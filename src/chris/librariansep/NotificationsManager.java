package chris.librariansep;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public class NotificationsManager {
	
	private Context appContext;
	private Service service;
	
	public NotificationsManager(Context applicationContext, Service service) {
		this.appContext = applicationContext;
		this.service = service;
	}

	public Notification generateNotification(String songTitle) {
		
		String nowPlaying = appContext.getResources().getString(R.string.now_playing);
	   	String result = nowPlaying + " " + songTitle;
	   	
//		mManager = (NotificationManager) this.appContext.getSystemService(this.appContext.NOTIFICATION_SERVICE);
	       Intent intent1 = new Intent(service, MainActivity.class);
	     
	       Notification notification = new Notification(R.drawable.ic_launcher, result, System.currentTimeMillis());
	       intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
	 
	       PendingIntent pendingNotificationIntent = PendingIntent.getActivity(service, 0, intent1, 0);
	       notification.flags |= Notification.FLAG_NO_CLEAR;
	       notification.setLatestEventInfo(service, "The Librarians", result, pendingNotificationIntent);
	 
	       return notification;
//	       mManager.notify(0, notification);
	}
	
}
