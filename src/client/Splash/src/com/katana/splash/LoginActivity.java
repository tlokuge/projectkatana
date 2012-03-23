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
	
    public static final int PASSMIN = 1;
    public static final int PASSMAX = 16;
    
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
        
        registerReceiver(broadcastReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }
    
    void doUnbindService() {
        if (mBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            unregisterReceiver(broadcastReceiver);
            stopService(new Intent(LoginActivity.this,KatanaService.class));
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
    
    public void submit(View view){
    	EditText u = (EditText)findViewById(R.id.username);
    	EditText p = (EditText)findViewById(R.id.password);
    	EditText c = (EditText)findViewById(R.id.cpassword);
    	
    	user = u.getText().toString().trim();
    	pass = p.getText().toString().trim();
    	String cpas = c.getText().toString().trim();
    	  	
    	if(	user.length() >= PASSMIN && pass.length() >= PASSMIN && cpas.length() >= PASSMIN &&
    		user.length() <= PASSMAX && pass.length() <= PASSMAX && cpas.length() <= PASSMAX) {
	    	if(pass.equals(cpas)){
	    		
	    		// Save username and password! 
	    		getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(PREF_USERNAME, user).commit();
	    		getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(PREF_PASSWORD, pass).commit();
	    		
	    		// Send username and password to communication daemon
	    		KatanaPacket packet = new KatanaPacket(0,Opcode.C_REGISTER);
	    		packet.addData(user);
	    		packet.addData(pass);
	    		katanaService.sendPacket(packet);
	    		/** NEED TO ADD LOCATION **/
	    		
	    		// Recieve confirmation from communication daemon
	    		
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
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra("opCode").equals(Opcode.S_REG_OK.name())){
    			Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
    			Intent i = new Intent();
    			i.setClass(LoginActivity.this, LobbyActivity.class);
    			startActivity(i);
    			finish();
    		} else if(intent.getStringExtra("opCode").equals(Opcode.S_AUTH_OK.name())){
    			Toast.makeText(LoginActivity.this, "Success! Welcome back! :)", Toast.LENGTH_SHORT).show();
    			Intent i = new Intent();
    			i.setClass(LoginActivity.this, LobbyActivity.class);
    			startActivity(i);
    			finish();
    		} else if(intent.getStringExtra("opCode").equals(Opcode.S_REG_NO.name())){
    			Toast.makeText(LoginActivity.this, "Username already exists or wrong password!", Toast.LENGTH_SHORT).show();
    		} 
    		
    	}
    };
    
}