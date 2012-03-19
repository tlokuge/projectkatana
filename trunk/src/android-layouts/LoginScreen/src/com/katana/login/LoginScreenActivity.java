package com.katana.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.lang.String;

public class LoginScreenActivity extends Activity {
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
    		user.length() <= PASSMAX  && pass.length() <= PASSMAX  && cpas.length() <= PASSMAX) {
	    	if(pass.equals(cpas)){
	    		// Send username and password to communication daemon
	    		
	    		
	    		// Recieve confirmation from communication daemon
	    		
	    		
	    		// If success
	    		String message = user + " has registered successfully!";
	    		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
	    		toast.show();
	    		// Else username already exists and password is wrong
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