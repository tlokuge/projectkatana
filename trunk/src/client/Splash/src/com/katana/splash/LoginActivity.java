package com.katana.splash;

import katana.activities.KatanaActivity;
import katana.constants.KatanaConstants;
import katana.constants.Opcode;
import katana.objects.KatanaPacket;
import katana.receivers.KatanaReceiver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends KatanaActivity {
	
	// LoginActivity Views
	private EditText userField;
	private EditText passField;
	private EditText cpasField;
	
	// -------------------------- //
	// Android Activity Lifecycle //
	// -------------------------- //
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        userField = (EditText)findViewById(R.id.username);
    	passField = (EditText)findViewById(R.id.password);
    	cpasField = (EditText)findViewById(R.id.cpassword);
    }
        
    @Override
    protected void onStart() {
    	super.onStart();
    	doBindService(KatanaReceiver.MODE_LOGIN, false);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	doUnbindService();
    	doUnregisterReceivers();
    	finish();
    }
    
	// ------------------------ //
	// Android Hardware Buttons //
	// ------------------------ //
    
    @Override
	public void onBackPressed() {
    	super.onBackPressed();
    	Log.d("CDA", "onBackPressed Called");
    	KatanaPacket packet = new KatanaPacket(Opcode.C_LOGOUT);
    	sendPacket(packet);
    	doKillService();
    	doUnregisterReceivers();
    }
    
    // ------------------------ //
    // Button onPress Functions //
    // ------------------------ //
    
    public void submit(View view){   	
    	String user = userField.getText().toString().trim();
    	String pass = passField.getText().toString().trim();
    	String cpas = cpasField.getText().toString().trim();
    	checkInput(user,pass,cpas);
    }
    
    // --------------------- //
    // LoginActivity Methods //
    // --------------------- //
    
	public void setUserLoginPrefs(boolean isNewUser){
		String user = userField.getText().toString().trim();
		String pass = passField.getText().toString().trim();
		getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).edit().putString(KatanaConstants.PREFS_LOGIN_USER, user).commit();
	    getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).edit().putString(KatanaConstants.PREFS_LOGIN_PASS, pass).commit();
	    if( isNewUser ) {
	    	String message = "Welcome to  " + KatanaConstants.KATANA_NAME + ", " + user + "!";
			Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
	    } else {
	    	String message = "Welcome back " + user + "!";
			Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
	    }
	    startMyActivity(this, LobbyActivity.class, null);
	}
	
    private void checkInput(String user, String pass, String cpas) {
    	if( user.length() < KatanaConstants.LOGIN_USERMIN || pass.length() < KatanaConstants.LOGIN_PASSMIN || cpas.length() < KatanaConstants.LOGIN_PASSMIN) {
    		// Area to expand on invalid input cases for more customized User Interface
        	Toast.makeText(getApplicationContext(), "Check input fields!", Toast.LENGTH_SHORT).show();
    	} else if ( user.length() > KatanaConstants.LOGIN_USERMAX || pass.length() > KatanaConstants.LOGIN_PASSMAX || cpas.length() > KatanaConstants.LOGIN_PASSMAX){
    		// Area to expand on invalid input cases for more customized User Interface
        	Toast.makeText(getApplicationContext(), "Check input fields!", Toast.LENGTH_SHORT).show();
    	} else {
    		// Input falls within the acceptable length parameters
    		if( !pass.equals(cpas) ){
    			// Password and confirmation do not match
    			Toast.makeText(getApplicationContext(), "Password and confirmation do not match!", Toast.LENGTH_SHORT).show();
    		} else {
    			// Input is valid
    			sendRegisterRequest(user, pass);
    		}
    	}
    }
    
    private void sendRegisterRequest(String user, String pass){	
		// Send username and password to server
		KatanaPacket packet = new KatanaPacket(Opcode.C_REGISTER);
		packet.addData(user);
		packet.addData(pass);
		sendPacket(packet);	 
	}


}