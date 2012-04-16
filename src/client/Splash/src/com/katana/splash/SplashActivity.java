package com.katana.splash;

import katana.activities.KatanaActivity;
import katana.constants.KatanaConstants;
import katana.constants.Opcode;
import katana.objects.KatanaPacket;
import katana.receivers.KatanaReceiver;
import katana.services.KatanaService;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class SplashActivity extends KatanaActivity {
	
	private SharedPreferences client_prefs;
	private Activity myContext = this;

	// -------------------------- //
	// Android Activity Lifecycle //
	// -------------------------- //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
        startService(new Intent(this,KatanaService.class));
        Thread splashTimer = new Thread(){
			@SuppressWarnings("static-access")
			public void run() {     
        		Looper.myLooper().prepare();
        		try {
    				doBindService(KatanaReceiver.MODE_SPLASH, false);
    				sleep(3500);
    				int slept = 0;
    				while(!isServiceBound()) {
    					if(slept > 5000) {
    						// Sleeping for too long! Quit!
    						myContext.finish();
    					}
    					System.out.println("Sleeping: Waiting for service to be bound");
    					sleep(500);
    					slept += 500;
    				}
        			
        			client_prefs = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE);
        			String user = client_prefs.getString(KatanaConstants.PREFS_LOGIN_USER, null);
        			String pass = client_prefs.getString(KatanaConstants.PREFS_LOGIN_PASS, null);
        			
        			if (user == null || pass == null){
        				// No user login stored
        				startMyActivity(myContext, LoginActivity.class, null);
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

    @Override
    protected void onStop() {
    	super.onStop();
    	doUnbindService();
    	finish();
    }
    
	// ------------------------ //
	// Android Hardware Buttons //
	// ------------------------ //
	
	@Override 
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("CDA", "onBackPressed Called"); 
		logout();
	}
	
	// ---------------------- //
	// SplashActivity Methods //
	// ---------------------- //

    private void checkLogin(String user, String pass){
		KatanaPacket packet = new KatanaPacket(Opcode.C_LOGIN);
		packet.addData(user);
		packet.addData(pass);
		sendPacket(packet);
    }
}