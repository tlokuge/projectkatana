package com.katana.splash;

import katana.services.KatanaService;
import katana.services.KatanaService.LocalBinder;
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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	// Service Vars
	KatanaService katanaService;
	boolean mBound = false;
	
	// LoginActivity Views
	private EditText userField;
	private EditText passField;
	private EditText cpasField;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        // Bind to KatanaService
        doBindService();
        
        // Bind Views
        userField = (EditText)findViewById(R.id.username);
    	passField = (EditText)findViewById(R.id.password);
    	cpasField = (EditText)findViewById(R.id.cpassword);
    }
       
    /** Called when the activity is paused. */
    protected void onPause() {
    	super.onPause();
        // Unbind KatanaService and Receiver
    	doUnbindService();
    }
    
    /** Called when the activity is destroyed. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    /** Called when android back button is pressed. */
    @Override
	public void onBackPressed() {
    	super.onBackPressed();
    	Log.d("CDA", "onBackPressed Called");
    	KatanaPacket packet = new KatanaPacket(0, Opcode.C_LOGOUT);
    	katanaService.sendPacket(packet);
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
    
    /** Button onPress Functions */
    public void submit(View view){   	
    	String user = userField.getText().toString().trim();
    	String pass = passField.getText().toString().trim();
    	String cpas = cpasField.getText().toString().trim();
    	
    	if( user.length() < KatanaConstants.USERMIN || pass.length() < KatanaConstants.PASSMIN || cpas.length() < KatanaConstants.PASSMIN) {
    		// Input is too short
    		inputShort(user,pass,cpas);
    	} else if ( user.length() > KatanaConstants.USERMAX || pass.length() > KatanaConstants.PASSMAX || cpas.length() > KatanaConstants.PASSMAX){
    		// Input is too long
    		inputLong(user,pass,cpas);
    	} else {
    		// Input falls within the acceptable length parameters
    		if( !pass.equals(cpas) ){
    			// Password and confirmation do not match
    			Toast.makeText(getApplicationContext(), "Password and confirmation do not match!", Toast.LENGTH_SHORT).show();
    		} else {
    			// Input is valid
    			inputValid(user, pass);
    		}
    	}
    }
    
    /** Input validation functions */
    private void inputShort(String user, String pass, String cpas){
    	// Area to expand on invalid input cases for more customized User Interface
    	Toast.makeText(getApplicationContext(), "Check input fields!", Toast.LENGTH_SHORT).show();
    }
    
    private void inputLong(String user, String pass, String cpas){
    	// Area to expand on invalid input cases for more customized User Interface
    	Toast.makeText(getApplicationContext(), "Check input fields!", Toast.LENGTH_SHORT).show();
    }
    
    private void inputValid(String user, String pass){	
		// Send username and password to server
    	KatanaPacket packet = new KatanaPacket(0, Opcode.C_REGISTER);
		packet.addData(user);
		packet.addData(pass);
		katanaService.sendPacket(packet);	 
    }
    
    /** Connect to KatanaService **/
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
            LocalBinder binder = (LocalBinder) service;
            katanaService = binder.getService();
            System.out.println(katanaService);
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    /** on Receive broadcast from KatanaService **/
    private BroadcastReceiver katanaReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra(KatanaService.EXTRAS_OPCODE).equals(Opcode.S_REG_OK.name())){
    			// Registered new user on server    			
    			userLoggedIn(true);
    		} else if(intent.getStringExtra(KatanaService.EXTRAS_OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			userLoggedIn(false);
    		} else if(intent.getStringExtra(KatanaService.EXTRAS_OPCODE).equals(Opcode.S_REG_NO.name())){
    			Toast.makeText(LoginActivity.this, "Username already exists or wrong password!", Toast.LENGTH_SHORT).show();
    		} 
    		
    	}
    };
    
    /** User is validated by server and is logged in */
    private void userLoggedIn(boolean isNewUser){
    	String user = userField.getText().toString().trim();
    	String pass = passField.getText().toString().trim();
    	
    	// Save user login
	    getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).edit().putString(KatanaConstants.LOGIN_USER, user).commit();
	    getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).edit().putString(KatanaConstants.LOGIN_PASS, pass).commit();
    	
    	if ( isNewUser ){
    		String message = "Welcome to  " + KatanaConstants.GAMENAME + ", " + user + "!";
    		Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    		
			startLobbyActivity();
    	} else {
    		String message = "Welcome back " + user + "!";
    		Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    		
    		startLobbyActivity();
    	}
    }
    
    /** Switch user to appropriate activities */
    private void startLobbyActivity(){
    	Intent i = new Intent();
		i.setClass(LoginActivity.this, LobbyActivity.class);
		startActivity(i);
		finish();
    }
}