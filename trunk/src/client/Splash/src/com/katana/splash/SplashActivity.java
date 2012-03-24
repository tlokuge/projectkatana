package com.katana.splash;

import shared.KatanaConstants;
import shared.KatanaPacket;
import shared.Opcode;
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

import com.katana.splash.KatanaService.LocalBinder;

public class SplashActivity extends Activity {

	// Preferences
	private SharedPreferences pref;
	// KatanaService
	KatanaService katanaService;
	boolean mBound = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        Thread splashTimer = new Thread(){
			@SuppressWarnings("static-access")
			public void run() {     
        		Looper.myLooper().prepare();
        		try {
        			sleep(3000);
        			
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
        
        // Bind to KatanaService
        doBindService();
        
    }
    
    /** Final cleanup when activity is finished */
    @Override
    protected void onDestroy() {
        super.onDestroy();
     // Unbind KatanaService and Receiver
        doUnbindService();
    }
    
    /** Disconnect from KatanaService */
    private void doUnbindService() {
        if (mBound) {
            // Detach our existing connection and broadcast receiver
            unbindService(mConnection);
            unregisterReceiver(broadcastReceiver);
            stopService(new Intent(SplashActivity.this,KatanaService.class));
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
        Intent intent = new Intent(this,KatanaService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            katanaService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    /** on Receive broadcast from KatanaService */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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