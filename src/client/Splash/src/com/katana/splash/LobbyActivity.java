package com.katana.splash;

import java.util.ArrayList;
import java.util.HashMap;

import katana.adapters.PlayerListAdapter;
import katana.adapters.RoomListAdapter;
import katana.objects.Player;
import katana.objects.Room;
import katana.receivers.KatanaReceiver;
import katana.receivers.LocationReceiver;
import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;
import katana.shared.KatanaConstants;
import katana.shared.KatanaPacket;
import katana.shared.Opcode;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class LobbyActivity extends Activity {
	
	// LobbyActivity Views
    private ViewFlipper viewFlipper;
    private TextView l_wroomname;
    private TextView l_wroomdiff;
    private TextView l_realmname;
    
    // WaitingRoom
    private TextView l_roomname;
    private TextView l_difficulty;
    private TextView l_maxplayers;
    
    private Button b_join;
    private Button b_create;
    
    private GridView gv_roomsList;
    private GridView gv_wroomList;
    
	private Dialog classDialog;
	private Dialog createRoomDialog; 
	private Dialog leaderboardDialog;
	
	private RadioGroup rg_diffSel;
	private RadioGroup rg_plyrSel;
	
	private EditText et_rmName;
	
	private PowerManager.WakeLock mWakeLock;
	// Vars
	private int selection = 0;
	ArrayList<Room> roomList;
	public Room selectedRoom;
	
	private boolean roomLeader = false;
	public boolean inRoom = false;
	public boolean inValidLoc = false;
	
	public int createdRoomId = -1;

	// Service Vars
	KatanaService katanaService; 
	boolean serviceBound = false;
	private KatanaReceiver katanaReceiver = new KatanaReceiver(2);
	private LocationReceiver katanaLocReceiver = new LocationReceiver();
	
	// Location Manager
	private double latitude;
	private double longitude;
	
	// Preferences
	private SharedPreferences gamePrefs;
	
	ArrayList<Player> playerList;
	HashMap<Integer, Integer> playerRef;
	
	/** Android hardware buttons */
	@Override 
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called"); 
		
		if (inRoom == true) {
			inRoom = false;
			if(roomLeader == true) {
				roomLeader = false;
				System.out.println("DESTROY ALL THE ROOM!");
				KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_DESTROY);
				katanaService.sendPacket(packet);
				lobbySendRoomListRequest(null);
				transitionToLobby();
				return;
			} else {
				katanaService.sendPacket(new KatanaPacket(0,Opcode.C_ROOM_LEAVE));
				transitionToLobby();
				return;
			}
		} else {
			super.onBackPressed();
			this.finish();
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(!inRoom) {
        	inflater.inflate(R.menu.lobby_menu, menu);
        }
        else {
        	inflater.inflate(R.menu.wroom_menu, menu);
        }
        return true;
    }
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	MenuInflater inflater = getMenuInflater();
        if(!inRoom) {
        	inflater.inflate(R.menu.lobby_menu, menu);
        }
        else {
        	inflater.inflate(R.menu.wroom_menu, menu);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                return true;
            case R.id.leaderboards:
            	katanaService.sendPacket(new KatanaPacket(0, Opcode.C_LEADERBOARD));
                return true;
            case R.id.change_class:
            	showSelectClassDialog(false);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	/** Activity Lifecycle */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
        mWakeLock.acquire();
        
        setContentView(R.layout.viewflipper);
        
        // Bind Views
        viewFlipper = (ViewFlipper)findViewById(R.id.flipper);
        l_wroomname = (TextView)findViewById(R.id.l_wroomname);
        l_wroomdiff = (TextView)findViewById(R.id.l_wroomdiff);
        l_realmname = (TextView)findViewById(R.id.l_realmname);
        b_join = (Button)findViewById(R.id.b_join);
        b_create = (Button)findViewById(R.id.b_create);
        gv_roomsList = (GridView)findViewById(R.id.gv_roomslist);
        gv_wroomList = (GridView)findViewById(R.id.gv_wroomlist);

        l_roomname = (TextView)findViewById(R.id.l_roomname);
        l_difficulty = (TextView)findViewById(R.id.l_difficulty);
        l_maxplayers = (TextView)findViewById(R.id.l_maxplayer);
        
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
    	
    	// Set onClickListeners
        b_join.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
    			if(!inValidLoc)
    			{
    				Toast.makeText(getApplicationContext(), R.string.b_norealm, Toast.LENGTH_SHORT).show();
    				return;
    			}
				lobbyJoinRoom();
			}
        });
        
        b_create.setOnClickListener( new OnClickListener() {
        	public void onClick(View v){
    			if(!inValidLoc)
    			{
    				Toast.makeText(getApplicationContext(), R.string.b_norealm, Toast.LENGTH_SHORT).show();
    				return;
    			}
        		lobbyCreateRoom();
        	}
        });
        
        gv_roomsList.setOnItemClickListener(selectRoomListener);
    }
    
	@Override
	protected void onStart(){
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
        // Bind to KatanaService
		// Start KatanaReceiver
		doBindService();
    	// Call KatanaService to get current location
    	// Display "Loading" until current location is found by service *or timeout?*
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		mWakeLock.release();
		// Unbind KatanaService and Receiver
		doUnbindService();
		// Send logout packet to server
		katanaService.sendPacket(new KatanaPacket(0,Opcode.C_LOGOUT));
		this.finish();
	}
    
    /** KatanaService binding */
	private void doBindService(){
	    Intent intent = new Intent(this,KatanaService.class);
	    bindService(intent, katanaServiceConn, Context.BIND_AUTO_CREATE);
	    registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
	    registerReceiver(katanaLocReceiver, new IntentFilter(KatanaService.BROADCAST_LOCATION));
	}

	private void doUnbindService() {
	    if (serviceBound) {
	        // Detach our existing connection and broadcast receiver
	        unbindService(katanaServiceConn);
	        unregisterReceiver(katanaReceiver);
	        unregisterReceiver(katanaLocReceiver);
	        stopService(new Intent(LobbyActivity.this,KatanaService.class));
	        serviceBound = false;
	    }
	}

	private ServiceConnection katanaServiceConn = new ServiceConnection() {
	    @Override
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // We've bound to LocalService, cast the IBinder and get LocalService instance
	        KatanaSBinder binder = (KatanaSBinder) service;
	        katanaService = binder.getService();
	        serviceBound = true;
	    }
	
	    @Override
	    public void onServiceDisconnected(ComponentName arg0) {
	        serviceBound = false;
	    }
	};

	/** Button onClick Functions **/   
    public void showSelectClassDialog(boolean newRoom){
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
    	   	
    	if(newRoom) {
        	class1.setOnClickListener(class1newRoomListener);
        	class2.setOnClickListener(class2newRoomListener);
    	} else {
    		class1.setOnClickListener(class1Listener);
    		class2.setOnClickListener(class2Listener);
    	}
    	
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
				showSelectClassDialog(true);
				createRoomDialog.dismiss();				
			}   		
    	});
    	createRoomDialog.show();
    }
    
    public void showLeaderboardDialog(String scores){
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
    	
    	TextView tv = (TextView)leaderboardDialog.findViewById(R.id.l_lb);
    	tv.setText(scores);
    	leaderboardDialog.show();
    }
    
    /** Game lobby methods */
    public void lobbySendRoomListRequest(View view){
		// Send server refresh room list packet
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_LIST);
		packet.addData(Double.toString(latitude));
		packet.addData(Double.toString(longitude));
		katanaService.sendPacket(packet);
	}

	public void lobbyShowRooms(String locName, ArrayList<String> al){
		roomList = new ArrayList<Room>();
		
		for (int i = 0; i < al.size(); i++){
			String[] lines = al.get(i).split(";");
			if(lines.length < 4)
				continue;
			roomList.add(new Room(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2]),Integer.parseInt(lines[3])));
		}
		l_realmname.setText(locName);
		gv_roomsList.setAdapter(new RoomListAdapter(this,roomList));       
	}

	private void lobbyJoinRoom() {
		// Show select class dialog
		showSelectClassDialog(false);
		// Send info to server and wait for response
	}

	public void lobbySendJoinRequest(){
		// Send a join room packet to server
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_JOIN);
		packet.addData(Integer.toString(selectedRoom.getId()));
		packet.addData(Integer.toString(gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1)));
		katanaService.sendPacket(packet);
	}

	private void lobbyCreateRoom(){
		// Show room options dialog
		showCreateRoomDialog();
		// Show select class dialog
		// Send info to server and wait for response
	}

	public void lobbySendCreateRequest(){
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
		int difficulty = 1;
		switch(diff){
			case R.id.diff1: 
				gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Easy").commit();
				break;
			case R.id.diff2:
				difficulty = 2;
				gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Standard").commit();
				break;
			case R.id.diff3: 
				difficulty = 3;
				gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Hard").commit();
				break;
			default: 
				difficulty = 2;
				gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Standard").commit();
				break;
		}
		
		// Set game max player preferences
		int plyr = rg_plyrSel.getCheckedRadioButtonId();
		int max = 1;
		switch(plyr){
			case R.id.plyr1: 
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 1).commit();
				break;
			case R.id.plyr2:
				max = 2;
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 2).commit();
				break;
			case R.id.plyr3:
				max = 3;
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 3).commit();
				break;
			case R.id.plyr4: 
			default: 
				max = 4;
				gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 4).commit();
				break;	
		}
		
		selectedRoom = new Room(-1, name, difficulty, max);
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_CREATE);
		packet.addData(name);
		packet.addData(difficulty + "");
		packet.addData(max + "");
		packet.addData(gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1) + "");
		katanaService.sendPacket(packet);
	}

    public void lobbyJoinCreatedRoom() {
		inRoom = true;
		roomLeader = true;
		this.waitingRoomShowPlayers(new ArrayList<String>());
		String name = gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		String diff = gamePrefs.getString(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
		l_wroomname.setText(name);
		l_wroomdiff.setText(diff);
		transitionToWaitingRoom();
	}

	public void lobbyTransitionToWaitingRoom(Room selected){
		inRoom = true;
		l_wroomname.setText(selected.getName());
		l_wroomdiff.setText(Integer.toString(selected.getDifficulty()));
		transitionToWaitingRoom();
	}

	/** Waiting room methods */
	public void waitingRoomShowPlayers(ArrayList<String> al) {
		// Update user interface and bounce user to waiting room
		playerList = new ArrayList<Player>();
		playerRef = new HashMap<Integer, Integer>();
		int position = 0;
		for(int i = 0; i < al.size(); i++) {
			System.out.println(al.get(i));
			String[] lines = al.get(i).split(";");
			if(lines.length < 3)
				continue;
			position = i;
			playerRef.put(Integer.parseInt(lines[0]), i);
			playerList.add(new Player(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2])));
		}
		int pid = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE).getInt(KatanaConstants.PLAYER_ID, 0);
		if(al.isEmpty()){
			playerRef.put(pid, 0);
		} else {
			playerRef.put(pid, position + 1);
		}
		playerList.add(new Player(pid, getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE).getString(KatanaConstants.LOGIN_USER, ""), gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1)));
		waitingRoomUpdatePlayers();
	}

	public void waitingRoomClassUpdate(int playerid, int classid) {
    	System.out.println("Player " + playerid + " is changing class from " + playerList.get(playerRef.get(playerid)).getClassId() + " to " + classid);
    	playerList.get(playerRef.get(playerid)).setClassId(classid);
    	waitingRoomUpdatePlayers();
    }
    
    public void waitingRoomAddPlayer(Intent intent){
    	String s = intent.getStringExtra(KatanaService.EXTRAS_PLAYERNAME);
    	int ref = playerList.size();
    	String lines[] = s.split(";");
    	playerRef.put(Integer.parseInt(lines[0]), ref);
    	playerList.add(new Player(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2])));
    	waitingRoomUpdatePlayers();
    }
    
    public void waitingRoomRemovePlayer(int player_id) {
    	int ref = playerRef.get(player_id);
    	System.out.println(ref);
    	System.out.println(playerList.size());
    	
    	for(int value : playerRef.values()) {
    		if(value  > ref) {
    			value--;
    		}
    	}
    	playerList.remove(ref);
    	playerRef.remove(player_id);
    	waitingRoomUpdatePlayers();
    }
    
    private void waitingRoomRequestClassChange(int i){
		int pid = getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).getInt(KatanaConstants.PLAYER_ID, 0);
		System.out.println("my id: " + playerRef.get(pid));
		System.out.println("playerlist size: " + playerList.size());
		playerList.get(playerRef.get(pid)).setClassId(i);
		gv_wroomList.setAdapter(new PlayerListAdapter(this,playerList));
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_CLASS_CHANGE);
		packet.addData(Integer.toString(i));
		katanaService.sendPacket(packet);
    }
    
    public void waitingRoomUpdatePlayers() {
		gv_wroomList.setAdapter(new PlayerListAdapter(this, playerList));
	}

	/** public get/set methods methods */
    public void setLatitude(double d) {
    	latitude = d;
    }
    
    public void setLongitude(double d) {
    	longitude = d;
    }

	public void setCreatedRoomId(int i) {
		createdRoomId = i;
	}

	public void setSelectedRoom(Room r) {
		selectedRoom = r;
	}

	public void setInRoom(boolean b) {
		inRoom = b;
		
	}

	public void setRoomLeader(boolean b) {
		roomLeader = b;
		
	}
	
	/** Viewflipper transitions */
	public void transitionToLobby() {
		viewFlipper.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_right);
		viewFlipper.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_right);
		viewFlipper.showPrevious();
	}
	
	public void transitionToWaitingRoom() {
		viewFlipper.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_left);
		viewFlipper.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_left);
		viewFlipper.showNext();
	}

	/** OnClickListeners */
	private OnClickListener class1Listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
	    	
			if(selection != 0){
				// A class is already highlighted
				if(selection == 1){
					// Go into game with class 1
					gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selection).commit();
					if(!inRoom) 
						lobbySendJoinRequest();
					else {
						waitingRoomRequestClassChange(1);
					}
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
	};
	
	private OnClickListener class2Listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
	    	
			if(selection != 0){
				// A class is already highlighted
				if(selection == 2){
					// Go into game with class 2
					gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selection).commit();
					if(!inRoom)
						lobbySendJoinRequest();
					else {
						waitingRoomRequestClassChange(2);
					}
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
	};
	
	private OnClickListener class1newRoomListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
	    	
			if(selection != 0){
				// A class is already highlighted
				if(selection == 1){
					gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selection).commit();
					lobbySendCreateRequest();
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
	};
	
	private OnClickListener class2newRoomListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
	    	
			if(selection != 0){
				// A class is already highlighted
				if(selection == 2){
					gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selection).commit();
					lobbySendCreateRequest();
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
	};
	
	private OnItemClickListener selectRoomListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			selectedRoom = (Room) roomList.get(position);
			l_roomname.setText(selectedRoom.getName());
			l_difficulty.setText(Integer.toString(selectedRoom.getDifficulty()));
			l_maxplayers.setText(Integer.toString(selectedRoom.getMaxPlyr()));
		}
	};
}

