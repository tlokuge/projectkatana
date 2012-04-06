package com.katana.splash;

import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;
import katana.shared.KatanaConstants;
import katana.shared.KatanaPacket;
import katana.shared.Opcode;
import android.app.Activity;
import android.content.BroadcastReceiver;
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
	private SharedPreferences pref;
	// KatanaService
	KatanaService katanaService;
	boolean mBound = false;
	
	
	/** Android hardware buttons */
	// Called on android back button pressed
	@Override 
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("CDA", "onBackPressed Called"); 
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_LOGOUT);
		katanaService.sendPacket(packet);
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        			sleep(5000);
        			
        			// Check preferences file for stored user/pass
        			pref = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE);
        			String user = pref.getString(KatanaConstants.LOGIN_USER, null);
        			String pass = pref.getString(KatanaConstants.LOGIN_PASS, null);
        			
        			if (user == null || pass == null){
        				// No user login stored
        				startLoginActivity();
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
    }
    
    /** Called when the activity is stopped. */
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    /** Final cleanup when activity is finished */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    /** Disconnect from KatanaService */
    private void doUnbindService() {
        if (mBound) {
            // Detach our existing connection and broadcast receiver
            unbindService(katanaConnection);
            unregisterReceiver(katanaReceiver);
            mBound = false;
        }
    }
    
    /** Verify user credentials with server */
    private void checkLogin(String user, String pass){
		KatanaPacket packet = new KatanaPacket(0,Opcode.C_LOGIN);
		packet.addData(user);
		packet.addData(pass);
		katanaService.sendPacket(packet);
    }
    
    /** Connect to KatanaService */
    private void doBindService() {
    	mBound = true;
        Intent intent = new Intent(this,KatanaService.class);
        bindService(intent, katanaConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }

    private ServiceConnection katanaConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            KatanaSBinder binder = (KatanaSBinder) service;
            katanaService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    /** on Receive broadcast from KatanaService */
    private BroadcastReceiver katanaReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra(KatanaService.EXTRAS_OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			startLobbyActivity();
    		} else if(intent.getStringExtra(KatanaService.EXTRAS_OPCODE).equals(Opcode.S_AUTH_NO.name())){
    			startLoginActivity();
    		} 
    	}
    };
    
    /** Switch user to appropriate activities */
    private void startLoginActivity(){
    	Intent i = new Intent();
		i.setClass(SplashActivity.this, LoginActivity.class);
		startActivity(i);
		finish();
    }
    
    private void startLobbyActivity(){
    	Intent i = new Intent();
		i.setClass(SplashActivity.this, LobbyActivity.class);
		startActivity(i);
		finish();
    }
}