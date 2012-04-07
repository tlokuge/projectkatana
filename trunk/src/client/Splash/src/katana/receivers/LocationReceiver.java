package katana.receivers;

import katana.services.KatanaService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.katana.splash.LobbyActivity;

public class LocationReceiver extends BroadcastReceiver {	
	
	public LocationReceiver(){
		
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		LobbyActivity lobbyActivity = (LobbyActivity) context;
		lobbyActivity.setLatitude(intent.getDoubleExtra(KatanaService.EXTRAS_LATITUDE, 0.0));
		lobbyActivity.setLongitude(intent.getDoubleExtra(KatanaService.EXTRAS_LONGITUDE, 0.0));
		lobbyActivity.refreshList(null);
	}
}