package com.katana.splash;

import katana.receivers.KatanaReceiver;
import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;
import katana.shared.KatanaConstants;
import katana.shared.KatanaPacket;
import katana.shared.Opcode;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class SplashActivity extends Activity {

	// Preferences
	private SharedPreferences client_prefs;
	// KatanaService
	private KatanaService katanaService;
	private KatanaReceiver katanaReceiver = new KatanaReceiver(0);
	private boolean serviceBound = false;
	private Context myContext = this;
	
	/** Android hardware buttons */
	// Called on android back button pressed
	@Override 
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("CDA", "onBackPressed Called"); 
		KatanaPacket packet = new KatanaPacket(Opcode.C_LOGOUT);
		katanaService.sendPacket(packet);
		doKillService();
	}

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        Intent intent = new Intent(this,KatanaService.class);
        startService(intent);
        
        Thread splashTimer = new Thread(){
			@SuppressWarnings("static-access")
			public void run() {     
        		Looper.myLooper().prepare();
        		try {
    				doBindService();
        			sleep(2000);
        			
        			// Check preferences file for stored user/pass
        			client_prefs = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE);
        			String user = client_prefs.getString(KatanaConstants.LOGIN_USER, null);
        			String pass = client_prefs.getString(KatanaConstants.LOGIN_PASS, null);
        			
        			if (user == null || pass == null){
        				// No user login stored
        				katanaReceiver.startMyActivity(myContext, LoginActivity.class);
        			} else {
        				// User login found, send to server
        				checkLogin(user,pass);
        			}	
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		} 
        	}
        };
        splashTimer.start();
    }
    
    /** Called when the activity is paused. */
    @Override
    protected void onPause() {
    	super.onPause();
        // Unbind KatanaService and Receiver
        doUnbindService();
        this.finish();
    }
    

    private void doBindService() {
    	serviceBound = true;
        Intent intent = new Intent(this,KatanaService.class);
        bindService(intent, katanaConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }
    
    private void doUnbindService() {
        if (serviceBound) {
            // Detach our existing connection and broadcast receiver
            unbindService(katanaConnection);
            unregisterReceiver(katanaReceiver);
            serviceBound = false;
        }
    }
    
    private void doKillService() {
        unbindService(katanaConnection);
        unregisterReceiver(katanaReceiver);
        stopService(new Intent(SplashActivity.this,KatanaService.class));
        serviceBound = false;
    }
    
    /** Verify user credentials with server */
    private void checkLogin(String user, String pass){
		KatanaPacket packet = new KatanaPacket(Opcode.C_LOGIN);
		packet.addData(user);
		packet.addData(pass);
		katanaService.sendPacket(packet);
    }
    


    private ServiceConnection katanaConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            KatanaSBinder binder = (KatanaSBinder) service;
            katanaService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };
}