package katana.activities;

import katana.constants.Opcode;
import katana.objects.KatanaPacket;
import katana.receivers.KatanaReceiver;
import katana.receivers.LocationReceiver;
import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

public class KatanaActivity extends Activity {
	
	// -------------------------- //
	// Android Activity Lifecycle //
	// -------------------------- //
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		doUnregisterReceivers();
	}
	
	// ------------------------ //
	// Android Hardware Buttons //
	// ------------------------ //
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	// ---------------------- //
	// Required for Wake Lock //
	// ---------------------- //
	
	private PowerManager.WakeLock wakeLockManager;
	private boolean wakeLockBound;
	
	public void doAcquireWakeLock() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLockManager = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wakeLockManager.acquire();	
        wakeLockBound = true;
	}
	
	public void doReleaseWakeLock() {
		wakeLockManager.release();
		wakeLockBound = false;
	}
	
	public boolean isWakeLockBound() {
		return wakeLockBound;
	}
	
	// ---------------------------//
	// Required for KatanaService //
	// ---------------------------//
	
	private KatanaService katanaService;
	private KatanaReceiver katanaReceiver;
	private LocationReceiver katanaLocReceiver;
	
	private boolean locationMode;
	private boolean serviceBound;
	
   	public void doBindService(int receiverMode, boolean lMode) {
    	locationMode = lMode;
    	katanaReceiver = new KatanaReceiver(receiverMode);
    	bindService(new Intent(this,KatanaService.class), 
    				katanaConnection, 
    				Context.BIND_AUTO_CREATE);
    	registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    	if(locationMode) {
    		katanaLocReceiver = new LocationReceiver();
    		registerReceiver(katanaLocReceiver, new IntentFilter(KatanaService.BROADCAST_LOCATION));
    	}
    }
    
    public void doUnbindService() {
    	unbindService(katanaConnection);
        serviceBound = false;
    }
    
	public void doKillService() {
		unbindService(katanaConnection);
        stopService(new Intent(this, KatanaService.class));
        serviceBound = false;
    }
	
	public void doUnregisterReceivers() {
		System.err.println("Removing katanaReceiver " + katanaReceiver + " ...");
		unregisterReceiver(katanaReceiver);
	    if(locationMode) {
	    	System.err.println("Location was registered, removing " + katanaLocReceiver + " ...");
	    	unregisterReceiver(katanaLocReceiver);
	    }
	}
	
	public Location getLastKnownLocation(){
		return katanaService.getLastKnownLocation();
	}
	
	public void sendPacket(KatanaPacket packet) {
		katanaService.sendPacket(packet);
	}
	
	@SuppressWarnings("rawtypes")
	public void startMyActivity(Context context, Class myClass, Bundle myBundle) {
		katanaReceiver.startMyActivity(context, myClass, myBundle);
	}
	
	public boolean isServiceBound() {
		return serviceBound;
	}
	
    private ServiceConnection katanaConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            KatanaSBinder binder = (KatanaSBinder) service;
            katanaService = binder.getService();
            serviceBound = true;
            System.err.println("KatanaService is bound " + katanaService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };
    
    // ---------------------- //
    // KatanaActivity methods //
    // ---------------------- //
    public void logout() {
    	sendPacket(new KatanaPacket(Opcode.C_LOGOUT));
    	doKillService();
    	finish();
    }
}