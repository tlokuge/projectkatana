package com.katana.login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.lang.String;

import shared.KatanaPacket;
import shared.KatanaSocket;
import shared.Opcode;

public class LoginScreenActivity extends Activity {
	SharedPreferences pref;
	public static final String PREFS_NAME = "UserPreferences";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	
	// Server information
	private static final String SERVER = "projectkatana.no-ip.org";
	private static final int PORT = 7777;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
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

	    		// Save username and password! 
	    		getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(PREF_USERNAME, user).commit();
	    		getSharedPreferences(PREFS_NAME,MODE_PRIVATE).edit().putString(PREF_PASSWORD, pass).commit();
	    		
	    		// Send username and password to communication daemon
	    		KatanaPacket packet = new KatanaPacket(0, Opcode.C_REGISTER);
	    		packet.addData(user);
	    		packet.addData(pass);
	    		packet.addData("555,555");
	    		
	    		
	    		KatanaSocket.sendPacket(SERVER, PORT, packet);
	    		System.out.println(packet);
	    		
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
    		
}