package com.katana.splash;

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
import android.widget.Toast;

import com.katana.splash.KatanaService.LocalBinder;

public class SplashActivity extends Activity {
	SharedPreferences pref;
	public static final String PREFS_NAME = "UserPreferences";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	
	//Service Vars
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
        			pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        			String user = pref.getString(PREF_USERNAME, null);
        			String pass = pref.getString(PREF_PASSWORD, null);
        			
        			if (user == null || pass == null){
        				Intent i = new Intent();
            			i.setClass(SplashActivity.this, LoginActivity.class);
            			startActivity(i);
            			finish();
        			} else {
        				KatanaPacket packet = new KatanaPacket(0,Opcode.C_LOGIN);
        				packet.addData(user);
        				packet.addData(pass);
        				katanaService.sendPacket(packet);
        			}	
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		} 
        	}
        };
        
        splashTimer.start();
        Intent intent = new Intent(this,KatanaService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }
    
    void doUnbindService() {
        if (mBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            unregisterReceiver(broadcastReceiver);
            stopService(new Intent(SplashActivity.this,KatanaService.class));
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	System.out.println("Service is bound!");
            LocalBinder binder = (LocalBinder) service;
            katanaService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra("opCode").equals(Opcode.S_AUTH_OK.name())){
    			Intent i = new Intent();
    			i.setClass(SplashActivity.this, LobbyActivity.class);
    			startActivity(i);
    			finish();
    		} else if(intent.getStringExtra("opCode").equals(Opcode.S_AUTH_NO.name())){
    			Intent i = new Intent();
    			i.setClass(SplashActivity.this, LoginActivity.class);
    			startActivity(i);
    			finish();
    		} 
    	}
    };
}