package com.katana.splash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;

public class SplashActivity extends Activity {
	SharedPreferences pref;
	public static final String PREFS_NAME = "UserPreferences";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	
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
        				Intent i = new Intent();
            			i.setClass(SplashActivity.this, LobbyActivity.class);
            			startActivity(i);
            			finish();
        			}	
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		} 
        	}
        };
        
        splashTimer.start();
    }
}