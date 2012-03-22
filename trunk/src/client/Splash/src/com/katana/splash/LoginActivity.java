package com.katana.splash;

import shared.KatanaPacket;
import shared.Opcode;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.katana.splash.KatanaService.LocalBinder;

public class LoginActivity extends Activity {
	public static final String PREFS_NAME = "UserPreferences";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	
	private SharedPreferences pref;
	private String user;
	private String pass;
	
	//Service Vars
	KatanaService katanaService;
	boolean mBound = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        Intent intent = new Intent(this,KatanaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        System.out.println(mBound);
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
    
    public void submit(View view){
    	EditText u = (EditText)findViewById(R.id.username);
    	EditText p = (EditText)findViewById(R.id.password);
    	EditText c = (EditText)findViewById(R.id.cpassword);
    	
    	String user = u.getText().toString().trim();
    	String pass = p.getText().toString().trim();
    	String cpas = c.getText().toString().trim();
    	  	
    	if(	user.length() >= PASSMIN && pass.length() >= PASSMIN && cpas.length() >= PASSMIN &&
    		user.length() <= PASSMAX && pass.length() <= PASSMAX && cpas.length() <= PASSMAX) {
	    	if(pass.equals(cpas)){

	    		
	    		
	    		// Send username and password to communication daemon
	    		KatanaPacket packet = new KatanaPacket(0,Opcode.C_REGISTER);
	    		packet.addData(user);
	    		packet.addData(pass);
	    		packet.addData("555,555");
	    		katanaService.sendPacket(packet);
	    		/** NEED TO ADD LOCATION **/
	    		
	    		// Recieve confirmation from communication daemon
	    		
	    		
	    		// If success
	    		String message = user + " has registered successfully!";
	    		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
	    		toast.show();
	    		// Else username already exists and/or password is wrong
	    	} else{
	    		String message = "Password and confirmation do not match!";
	    		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
	    		toast.show();
	    	}
    	} else {
    		String message = "Please fill out all fields!";
    		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
    		toast.show();
    	}
    }
    
    public static final int PASSMIN = 1;
    public static final int PASSMAX = 16;
    		
    public void register(boolean flag){
    	// Recieve confirmation from communication daemon
		
		
		// If success
    	if(flag) {
    		// Save username and password! 
    		getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(PREF_USERNAME, user).commit();
    		getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(PREF_PASSWORD, pass).commit();
    		
    		System.out.println("Success!");
    	} else {
    		System.out.println("Fail!");
    	}
		// Else username already exists and/or password is wrong
		
    }
}