package com.katana.splash;

import java.util.ArrayList;

import shared.KatanaConstants;
import shared.KatanaPacket;
import shared.Opcode;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.katana.splash.KatanaService.LocalBinder;

public class LobbyActivity extends Activity {
	
	// LobbyActivity Views
    private ViewFlipper viewFlipper;
    private TextView l_wroomname;
    private TextView l_wroomdiff;
    private TextView l_realmname;
    
    private Button b_join;
    private Button b_create;
    private Button b_leader;
    
    private ListView lv_roomsList;
    
	private Dialog classDialog;
	private Dialog createRoomDialog; 
	private Dialog leaderboardDialog;
	
	private RadioGroup rg_diffSel;
	private RadioGroup rg_plyrSel;
	
	private EditText et_rmName;
    
	// Vars
	private int selection = 0;
	ArrayList<Room> roomList;

	private boolean newRoom = false;
	private boolean inRoom = false;
		
	// Service Vars
	KatanaService katanaService;
	boolean mBound = false;
	
	// Location Manager
	private String lat;
	private String lng;
	
	// Preferences
	private SharedPreferences gamePrefs;
	
	/** Called on android back button pressed */
	@Override 
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called"); 
		if (inRoom == true) {
			inRoom = false;
			viewFlipper.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_right);
			viewFlipper.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_right);
			viewFlipper.showPrevious();
		} else {
			this.finish();
		}
	}
    
	/** Called when the activity is first created */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewflipper);
        
        // Bind Views
        viewFlipper = (ViewFlipper)findViewById(R.id.flipper);
        l_wroomname = (TextView)findViewById(R.id.l_wroomname);
        l_wroomdiff = (TextView)findViewById(R.id.l_wroomdiff);
        l_realmname = (TextView)findViewById(R.id.l_realmname);
        b_join = (Button)findViewById(R.id.b_join);
        b_create = (Button)findViewById(R.id.b_create);
        b_leader = (Button)findViewById(R.id.b_leaderboard);
        lv_roomsList = (ListView)findViewById(R.id.list_roomslist);
        

        
    	// Set up select class Dialog
    	classDialog = new Dialog(this, R.style.DialogTheme);
    	classDialog.setContentView(R.layout.select_class);
    	classDialog.setTitle("Select Class");
    	classDialog.setCancelable(true);
    	
    	classDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				selection = 0;
				classDialog.dismiss();
			}   		
    	}); 
    	
    	// Set up create room dialog
    	createRoomDialog = new Dialog(this, R.style.DialogTheme);
    	createRoomDialog.setContentView(R.layout.create_room);
    	createRoomDialog.setTitle("Create Room");
    	createRoomDialog.setCancelable(true);
    	
    	createRoomDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				createRoomDialog.dismiss();
			}   		
    	});
    	rg_diffSel = (RadioGroup)createRoomDialog.findViewById(R.id.rg_roomdiff);
    	rg_plyrSel = (RadioGroup)createRoomDialog.findViewById(R.id.rg_roommaxplr);
    	et_rmName = (EditText)createRoomDialog.findViewById(R.id.l_roomname);
    	
    	// Load Preferences
    	gamePrefs = getSharedPreferences(KatanaConstants.PREFS_GAME, MODE_PRIVATE);
        b_join.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				newRoom = false;
				showSelectClassDialog();
			}
        });
        
        b_create.setOnClickListener( new OnClickListener() {
        	public void onClick(View v){
        		newRoom = true;
        		showCreateRoomDialog();
        	}
        });
        
        
        b_leader.setOnClickListener( new OnClickListener() {
        	public void onClick(View v){
        		showLeaderboardDialog();
        	}
        });
        
        ArrayList<Room> al_roomList = new ArrayList<Room>();
        al_roomList.add(new Room(0,"Kitty","Easy",3));
        al_roomList.add(new Room(2,"Mao","Standard",1));
        al_roomList.add(new Room(5,"Meow :3","Easy",4));
        al_roomList.add(new Room(8,"Lalala","Hard",2));
        
        // Bind to KatanaService
        doBindService();
        // Start LocationManager
        doStartLocationManager();
        
        // Set ListView Adapter
        lv_roomsList.setAdapter(new RoomListAdapter(this,al_roomList));        
    }
    
	/** Called when the activity is paused */
	protected void onPause(){
		super.onPause();
		// Send logout packet to server
		katanaService.sendPacket(new KatanaPacket(0,Opcode.C_LOGOUT));
	}
	
	/** Called when the activity is stopped */
	protected void onStop(){
		super.onStop();
		// Send logout packet to server
	}
	    
	/** Final cleanup when activity is finished */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind KatanaService and Receiver
        doUnbindService();
    }    
    
    /** Disconnect from KatanaService */
    private void doUnbindService() {
        if (mBound) {
            // Detach our existing connection and broadcast receiver
            unbindService(mConnection);
            unregisterReceiver(broadcastReceiver);
            stopService(new Intent(LobbyActivity.this,KatanaService.class));
            mBound = false;
        }
    }
      
    /** Button onClick Functions **/   
    public void showSelectClassDialog(){
    	// Load class preference
    	int classChoice = gamePrefs.getInt(KatanaConstants.GAME_CLASS, -1);
    	selection = 0;
    	
    	// Set up user interface in select class dialog
    	ImageButton class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
    	ImageButton class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
    	ImageButton close = (ImageButton)classDialog.findViewById(R.id.b_close);
    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
    	
    	desc.setText(KatanaConstants.DEF_DESC);
    	close.setImageResource(R.drawable.close);
    	if(classChoice == 1) {
    		/** Set class1 image to selected! **/
    		class1.setImageResource(R.drawable.class1_selected);
    		class2.setImageResource(R.drawable.class2);
    	} else if (classChoice == 2){
    		/** Set class2 image to selected! **/
    		class1.setImageResource(R.drawable.class1);
    		class2.setImageResource(R.drawable.class2_selected);
    	} else {
    		class1.setImageResource(R.drawable.class1);
    		class2.setImageResource(R.drawable.class2);
    	}
    	
    	// Called when the user presses close
    	close.setOnClickListener( new OnClickListener(){
    		public void onClick(View v) { 
				classDialog.cancel();
    		}
    	});
    	   	
    	// When user pressed class1
    	class1.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton b_class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
		    	ImageButton b_class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
		    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
		    	
				if(selection != 0){
					// A class is already highlighted
					if(selection == 1){
						// Go into game with class 1
						if(!inRoom) {
							joinRoom();
						}
						gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selection).commit();
						classDialog.dismiss();
					} else {
						// Highlight class 1
						selection = 1;
						b_class1.setImageResource(R.drawable.class1_selected);
			    		b_class2.setImageResource(R.drawable.class2);
			    		desc.setText(KatanaConstants.DESC_CLASS1);
					}
				} else {
					// Highlight class 1
					selection = 1;
					b_class1.setImageResource(R.drawable.class1_selected);
		    		b_class2.setImageResource(R.drawable.class2);
					desc.setText(KatanaConstants.DESC_CLASS1);
				}
			}
    	});
    	
    	// When user presses class2
    	class2.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton b_class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
		    	ImageButton b_class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
		    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
		    	
				if(selection != 0){
					// A class is already highlighted
					if(selection == 2){
						// Go into game with class 2
						if(!inRoom)
							joinRoom();
						gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selection).commit();
						classDialog.dismiss();
					} else{
						// Highlight class 2
						selection = 2;
						b_class1.setImageResource(R.drawable.class1);
			    		b_class2.setImageResource(R.drawable.class2_selected);
			    		desc.setText(KatanaConstants.DESC_CLASS2);
					}
				} else {
					// Highlight class 2
					selection = 2;
					b_class1.setImageResource(R.drawable.class1);
		    		b_class2.setImageResource(R.drawable.class2_selected);
		    		desc.setText(KatanaConstants.DESC_CLASS2);
				}
			}
    	});
    	
    	// Show select class dialog
    	classDialog.show();
    }
    
    public void showCreateRoomDialog(){   	
    	ImageButton close = (ImageButton)createRoomDialog.findViewById(R.id.b_close);
    	close.setImageResource(R.drawable.close);
    	
    	String diff = gamePrefs.getString(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
    	String name = gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
    	int maxp 	= gamePrefs.getInt(KatanaConstants.GAME_MAXP, KatanaConstants.DEF_ROOMMAXP);
    	
    	// Populate fields with gameprefs
    	if(diff.equals(KatanaConstants.DIFF_EASY)){
    		rg_diffSel.check(R.id.diff1);
    	} else if(diff.equals(KatanaConstants.DIFF_STD)){
    		rg_diffSel.check(R.id.diff2);
    	} else if(diff.equals(KatanaConstants.DIFF_HARD)){
    		rg_diffSel.check(R.id.diff3);
    	} else {
    		rg_diffSel.check(R.id.diff2);
    	}
    	
    	switch(maxp){
    		case 1: rg_plyrSel.check(R.id.plyr1); break;
    		case 2: rg_plyrSel.check(R.id.plyr2); break;
    		case 3: rg_plyrSel.check(R.id.plyr3); break;
    		case 4: 
    		default: rg_plyrSel.check(R.id.plyr4); break;
    	}

    	et_rmName.setText(name);
    
    	close.setOnClickListener( new OnClickListener(){
    		public void onClick(View v) { 
				createRoomDialog.cancel();
    		}
    	});
    	
    	Button confirm = (Button)createRoomDialog.findViewById(R.id.b_makeroom);
    	confirm.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				makeRoom();
				showSelectClassDialog();
				createRoomDialog.dismiss();				
			}   		
    	});
    	
    	createRoomDialog.show();
    }
    
    public void refreshList(View view){
    	// Send server refresh room list packet
    	KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_LIST);
    	packet.addData(lat);
    	packet.addData(lng);
    	katanaService.sendPacket(packet);
    	Toast.makeText(getApplicationContext(), lat+" "+lng, Toast.LENGTH_SHORT).show();
    }
    
    public void showLeaderboardDialog(){
    	leaderboardDialog = new Dialog(this, R.style.DialogTheme);
    	leaderboardDialog.setContentView(R.layout.leaderboard);
    	leaderboardDialog.setTitle("Leaderboards");
    	leaderboardDialog.setCancelable(true);
    	
    	leaderboardDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				leaderboardDialog.dismiss();
			}   		
    	});
    	
    	ImageButton close = (ImageButton)leaderboardDialog.findViewById(R.id.b_close);
    	
    	close.setImageResource(R.drawable.close);
    	close.setOnClickListener( new OnClickListener(){
    		public void onClick(View v) { 
				leaderboardDialog.cancel();
    		}
    	});
    	
    	leaderboardDialog.show();
    }
    
    /** Called when the user confirms their class choice */
    public void joinRoom(){
    	String roomName = gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
    	String difficulty = gamePrefs.getString(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
    	int maxPlayers = gamePrefs.getInt(KatanaConstants.GAME_MAXP, KatanaConstants.DEF_ROOMMAXP);
    	
    	String message = "";
    	if(newRoom) {
    		message = "Creating room! \n" +
    				roomName + "\n" +
    				difficulty + "\n" +
    				maxPlayers;
    		inRoom = true;
    		
    		l_wroomname.setText(roomName);
    		l_wroomdiff.setText(difficulty);
    		
    		viewFlipper.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_left);
    		viewFlipper.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_left);
    		viewFlipper.showNext();
    		
    	} else {
    		message = "Joining Room!";
    		inRoom = true;
    		TextView l_wroomname = (TextView)findViewById(R.id.l_wroomname);
    		l_wroomname.setText(roomName);
    		
    		viewFlipper.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_left);
    		viewFlipper.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_left);
    		viewFlipper.showNext();
    	}
    	
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /** Called when the user confirms the room preferences */
    public void makeRoom(){
    	// Read room name from textfield
    	String name = et_rmName.getText().toString().trim();
    	
    	// Set room name preferences
    	if(name.length() < KatanaConstants.ROOMNAMEMIN) {
    		// Room name is left blank
    		gamePrefs.edit().putString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME).commit();
    	} else {
    		gamePrefs.edit().putString(KatanaConstants.GAME_NAME, name).commit();
    	}
    	
    	// Set game difficulty preferences
    	int diff = rg_diffSel.getCheckedRadioButtonId();
    	switch(diff){
    		case R.id.diff1: 
    			gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Easy").commit();
    			break;
    		case R.id.diff2: 
    			gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Standard").commit();
    			break;
    		case R.id.diff3: 
    			gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Hard").commit();
    			break;
    		default: 
    			gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Standard").commit();
    			break;
    	}
    	
    	// Set game max player preferences
    	int plyr = rg_plyrSel.getCheckedRadioButtonId();
    	switch(plyr){
			case R.id.plyr1: 
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 1).commit();
				break;
			case R.id.plyr2: 
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 2).commit();
				break;
			case R.id.plyr3: 
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 3).commit();
				break;
			case R.id.plyr4: 
			default: gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 4).commit();
				break;	
    	}
    }
    
    /** Start LocationManager !! Find a way to look up location on click */
    private void doStartLocationManager(){
        LocationManager locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        LocationListener locListener = new LocationListener(){
			@Override
			public void onLocationChanged(Location location) {
				lat = String.valueOf(location.getLatitude());
				lng = String.valueOf(location.getLongitude());
				Toast.makeText(getApplicationContext(), lat+" "+lng, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProviderDisabled(String arg0) {
			}

			@Override
			public void onProviderEnabled(String arg0) {
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}
        };
        
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, KatanaConstants.GPS_MIN_REFRESHTIME, KatanaConstants.GPS_MIN_REFRESHDIST, locListener);
    }
    
    /** Connect to KatanaService */
    private void doBindService(){
        Intent intent = new Intent(this,KatanaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            katanaService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    /** on Receive broadcast from KatanaService */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra(KatanaService.EXTRAS_OPCODE).equals(Opcode.S_ROOM_LIST.name())){
    			System.out.println("Got room list :D");
    			String locName = intent.getStringExtra(KatanaService.EXTRAS_LOCNAME);
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_ROOMSLIST);
    			showRoomList(locName, al);
    		}
    	}
    };
    
    /** Refresh room list with server response */
    private void showRoomList(String locName, ArrayList<String> al){
    	roomList = new ArrayList<Room>();
    	
    	for (int i = 0; i < al.size(); i++){
    		String[] lines = al.get(i).split(";");
    		if(lines.length < 4)
    			continue;
    		roomList.add(new Room(Integer.parseInt(lines[0]),lines[1],lines[2],Integer.parseInt(lines[3])));
    	}
    	
    	// Update user interface
    	l_realmname.setText(locName);
    	lv_roomsList.setAdapter(new RoomListAdapter(this,roomList));       
    }
}

