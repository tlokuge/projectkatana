package com.katana.main;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
	private int classChoice = 0;
	private int selectClass = 0;
	
	private String roomName = "";
	private String difficulty = "";
	private int maxPlayers = 0;
	
	private Dialog classDialog;
	private Dialog createRoomDialog; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ArrayList<String> items = new ArrayList<String>();
        items.add("abc");
        items.add("def");
        items.add("ghi");
        items.add("jkl");
        items.add("mno");
        items.add("pqr");
        items.add("stu");
        items.add("vwx");
        items.add("yz!");
        
        ListView listView = (ListView)findViewById(R.id.roomsList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
        
        Button join_b = (Button)findViewById(R.id.join);
        join_b.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				joinRoom();
			}
        });
    }
    
    public void joinRoom(){
    	// set up dialog
    	classDialog = new Dialog(this, R.style.SelectClassTheme);
    	classDialog.setContentView(R.layout.select_class);
    	classDialog.setTitle("Select Class");
    	classDialog.setCancelable(true);
    	
    	classDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				selectClass = 0;
				String message = "You are class: " + classChoice;
				Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT); 
				toast.show();
				classDialog.dismiss();
			}   		
    	});
    	  	
    	ImageButton class1 = (ImageButton)classDialog.findViewById(R.id.class1_b);
    	ImageButton class2 = (ImageButton)classDialog.findViewById(R.id.class2_b);
    	ImageButton close = (ImageButton)classDialog.findViewById(R.id.close_b);
    	
    	close.setImageResource(R.drawable.close);
    	close.setOnClickListener( new OnClickListener(){
    		public void onClick(View v) { 
				classDialog.cancel();
    		}
    	});
    	
    	
    	if(classChoice == 1) {
    		/** Set class1 image to selected! **/
    		class1.setImageResource(R.drawable.class1);
    		class2.setImageResource(R.drawable.class2);
    	} else if (classChoice == 2){
    		/** Set class2 image to selected! **/
    		class1.setImageResource(R.drawable.class1);
    		class2.setImageResource(R.drawable.class2);
    	} else {
    		class1.setImageResource(R.drawable.class1);
    		class2.setImageResource(R.drawable.class2);
    	}
    	
    	class1.setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				if(selectClass != 1) {
					selectClass = 1;
					TextView desc = (TextView)classDialog.findViewById(R.id.classDesc);
					desc.setText("This is the mage. He can heal :D");
					
					/** Set class images to indicate change!**/
					
					
				} else if(selectClass == 1){
					classChoice = 1;
					selectClass = 0;
					// Join game!
					String message = "Joining the game as class 1!";
					Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
					toast.show();
					classDialog.dismiss();
				}
			}
    	});
    	
    	class2.setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				if(selectClass != 2) {
					selectClass = 2;
					TextView desc = (TextView)classDialog.findViewById(R.id.classDesc);
					desc.setText("This is the warrior. He is strong :D");
					
					/** Set class images to indicate change!**/
					
				} else if(selectClass == 2){
					classChoice = 2;
					selectClass = 0;
					
					// Join game!
					String message = "Joining the game as class 2!";
					Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
					toast.show();
					classDialog.dismiss();
				}
			}
    	});
    	
    	classDialog.show();
    	
    	// get user class selection
    	// join selected room
    }
    
    public void createRoom(View view){
    	createRoomDialog = new Dialog(this, R.style.SelectClassTheme);
    	createRoomDialog.setContentView(R.layout.create_room);
    	createRoomDialog.setTitle("Create Room");
    	createRoomDialog.setCancelable(true);
    	
    	createRoomDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				createRoomDialog.dismiss();
			}   		
    	});
    	
    	ImageButton close = (ImageButton)createRoomDialog.findViewById(R.id.close_b);
    	close.setImageResource(R.drawable.close);
    	close.setOnClickListener( new OnClickListener(){
    		public void onClick(View v) { 
				createRoomDialog.cancel();
    		}
    	});
    	
    	Button confirm = (Button)createRoomDialog.findViewById(R.id.confirmRoom);
    	confirm.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				makeRoom();
				joinRoom();
				createRoomDialog.dismiss();
			}   		
    	});
    	
    	createRoomDialog.show();
    }
    
    public void makeRoom(){
    	RadioGroup diffSel = (RadioGroup)MainActivity.this.createRoomDialog.findViewById(R.id.diffSel);
    	RadioGroup plyrSel = (RadioGroup)MainActivity.this.createRoomDialog.findViewById(R.id.plyrSel);
    	EditText rmName = (EditText)MainActivity.this.createRoomDialog.findViewById(R.id.room_name);
    	
    	roomName = rmName.getText().toString();
    	int diff = diffSel.getCheckedRadioButtonId();
    	switch(diff){
    		case R.id.diff1: difficulty = "Easy"; break;
    		case R.id.diff2: difficulty = "Standard"; break;
    		case R.id.diff3: difficulty = "Hard"; break;
    		default: break;
    	}
    	
    	int plyr = plyrSel.getCheckedRadioButtonId();
    	switch(plyr){
		case R.id.plyr1: maxPlayers = 1; break;
		case R.id.plyr2: maxPlayers = 2; break;
		case R.id.plyr3: maxPlayers = 3; break;
		case R.id.plyr4: maxPlayers = 4; break;
		default: break;
	}
    	
    	String message = roomName + "\n" + difficulty + "\n" + maxPlayers;
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();    	
    }

    public void refreshList(View view){
    	String message = "Refresh rooms list!";
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
    }

    public void showLeaderboard(View view){
    	String message = "Show leaderboard!";
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
    }
    
    public void selectClass(View view){
    	String message = "You picked class 1!";
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
    }
    
    public void onRadioButtonClicked(View v) {
    	RadioButton rb = (RadioButton) v;
    	//Toast.makeText(MainActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
    }
}

