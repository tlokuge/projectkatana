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
    private TextView lobby_realmName;
    private TextView lobby_roomName;
    private TextView lobby_difficultyLabel;
    private TextView lobby_maxPlayerLabel;
    private Button lobby_joinButton;
    private Button lobby_createButton;
    private GridView lobby_roomGridView;
	private ArrayList<Room> lobby_roomList;
	public Room lobby_selectedRoom;
    
    // WaitingRoom
    private TextView room_gameName;
    private TextView room_difficulty;
    private GridView room_playerGridView;
	private ArrayList<Player> room_playerList;
	private HashMap<Integer, Integer> room_playerRef;
    
    // Dialogs
	private Dialog selectClassDialog;
	private int selectClassDialog_selection = 0;
	
	private Dialog createRoomDialog; 
	private EditText createRoomDialog_name;
	private RadioGroup createRoomDialog_difficulty;
	private RadioGroup createRoomDialog_maxPlayer;
	
	private Dialog leaderboardDialog;

	// Vars
	private PowerManager.WakeLock wakeLockManager;
	
	private SharedPreferences client_gamePrefs;
	private boolean client_roomLeader = false;
	public int client_createdRoomId = -1;
	public boolean client_inRoom = false;
	public boolean client_inValidLocation = false;
	
	private double client_latitude;
	private double client_longitude;

	// Service Vars
	KatanaService katanaService; 
	boolean serviceBound = false;
	private KatanaReceiver katanaReceiver = new KatanaReceiver(2);
	private LocationReceiver katanaLocReceiver = new LocationReceiver();
	
	/** Android hardware buttons */
	@Override 
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called"); 
		
		if (client_inRoom == true) {
			client_inRoom = false;
			if(client_roomLeader == true) {
				client_roomLeader = false;
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
        if(!client_inRoom) {
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
        if(!client_inRoom) {
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
        wakeLockManager = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wakeLockManager.acquire();
        
        setContentView(R.layout.viewflipper);
        
        // Bind Views
        viewFlipper = (ViewFlipper)findViewById(R.id.flipper);
        room_gameName = (TextView)findViewById(R.id.l_wroomname);
        room_difficulty = (TextView)findViewById(R.id.l_wroomdiff);
        lobby_realmName = (TextView)findViewById(R.id.l_realmname);
        lobby_joinButton = (Button)findViewById(R.id.b_join);
        lobby_createButton = (Button)findViewById(R.id.b_create);
        lobby_roomGridView = (GridView)findViewById(R.id.gv_roomslist);
        room_playerGridView = (GridView)findViewById(R.id.gv_wroomlist);

        lobby_roomName = (TextView)findViewById(R.id.l_roomname);
        lobby_difficultyLabel = (TextView)findViewById(R.id.l_difficulty);
        lobby_maxPlayerLabel = (TextView)findViewById(R.id.l_maxplayer);
        
    	// Set up select class Dialog
    	selectClassDialog = new Dialog(this, R.style.DialogTheme);
    	selectClassDialog.setContentView(R.layout.select_class);
    	selectClassDialog.setTitle("Select Class");
    	selectClassDialog.setCancelable(true);
    	
    	selectClassDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				selectClassDialog_selection = 0;
				selectClassDialog.dismiss();
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
    	createRoomDialog_difficulty = (RadioGroup)createRoomDialog.findViewById(R.id.rg_roomdiff);
    	createRoomDialog_maxPlayer = (RadioGroup)createRoomDialog.findViewById(R.id.rg_roommaxplr);
    	createRoomDialog_name = (EditText)createRoomDialog.findViewById(R.id.l_roomname);
    	
    	// Load Preferences
    	client_gamePrefs = getSharedPreferences(KatanaConstants.PREFS_GAME, MODE_PRIVATE);
    	
    	// Set onClickListeners
        lobby_joinButton.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
    			if(!client_inValidLocation)
    			{
    				Toast.makeText(getApplicationContext(), R.string.b_norealm, Toast.LENGTH_SHORT).show();
    				return;
    			}
				lobbyJoinRoom();
			}
        });
        
        lobby_createButton.setOnClickListener( new OnClickListener() {
        	public void onClick(View v){
    			if(!client_inValidLocation)
    			{
    				Toast.makeText(getApplicationContext(), R.string.b_norealm, Toast.LENGTH_SHORT).show();
    				return;
    			}
        		lobbyCreateRoom();
        	}
        });
        
        lobby_roomGridView.setOnItemClickListener(selectRoomListener);
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
		wakeLockManager.release();
		// Unbind KatanaService and Receiver
		doUnbindService();
		// Send logout packet to server
		katanaService.sendPacket(new KatanaPacket(0,Opcode.C_LOGOUT));
		this.finish();
	}
    
    /** Button onClick Functions **/   
    public void showSelectClassDialog(boolean newRoom){
    	// Load class preference
    	int classChoice = client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, -1);
    	selectClassDialog_selection = 0;
    	
    	// Set up user interface in select class dialog
    	ImageButton class1 = (ImageButton)selectClassDialog.findViewById(R.id.b_class1);
    	ImageButton class2 = (ImageButton)selectClassDialog.findViewById(R.id.b_class2);
    	ImageButton close = (ImageButton)selectClassDialog.findViewById(R.id.b_close);
    	TextView desc = (TextView)selectClassDialog.findViewById(R.id.l_classdesc);
    	
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
				selectClassDialog.cancel();
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
    	selectClassDialog.show();
    }
    
    public void showCreateRoomDialog(){   	
    	ImageButton close = (ImageButton)createRoomDialog.findViewById(R.id.b_close);
    	close.setImageResource(R.drawable.close);
    	String diff = client_gamePrefs.getString(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
    	String name = client_gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
    	int maxp 	= client_gamePrefs.getInt(KatanaConstants.GAME_MAXP, KatanaConstants.DEF_ROOMMAXP);
    	// Populate fields with gameprefs
    	if(diff.equals(KatanaConstants.DIFF_EASY)){
    		createRoomDialog_difficulty.check(R.id.diff1);
    	} else if(diff.equals(KatanaConstants.DIFF_STD)){
    		createRoomDialog_difficulty.check(R.id.diff2);
    	} else if(diff.equals(KatanaConstants.DIFF_HARD)){
    		createRoomDialog_difficulty.check(R.id.diff3);
    	} else {
    		createRoomDialog_difficulty.check(R.id.diff2);
    	}
    	switch(maxp){
    		case 1: createRoomDialog_maxPlayer.check(R.id.plyr1); break;
    		case 2: createRoomDialog_maxPlayer.check(R.id.plyr2); break;
    		case 3: createRoomDialog_maxPlayer.check(R.id.plyr3); break;
    		case 4: 
    		default: createRoomDialog_maxPlayer.check(R.id.plyr4); break;
    	}
    	createRoomDialog_name.setText(name);
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
		packet.addData(Double.toString(client_latitude));
		packet.addData(Double.toString(client_longitude));
		katanaService.sendPacket(packet);
	}

	public void lobbyShowRooms(String locName, ArrayList<String> al){
		lobby_roomList = new ArrayList<Room>();
		
		for (int i = 0; i < al.size(); i++){
			String[] lines = al.get(i).split(";");
			if(lines.length < 4)
				continue;
			lobby_roomList.add(new Room(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2]),Integer.parseInt(lines[3])));
		}
		lobby_realmName.setText(locName);
		lobby_roomGridView.setAdapter(new RoomListAdapter(this,lobby_roomList));       
	}

	private void lobbyJoinRoom() {
		// Show select class dialog
		showSelectClassDialog(false);
		// Send info to server and wait for response
	}

	public void lobbySendJoinRequest(){
		// Send a join room packet to server
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_JOIN);
		packet.addData(Integer.toString(lobby_selectedRoom.getId()));
		packet.addData(Integer.toString(client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1)));
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
		String name = createRoomDialog_name.getText().toString().trim();
		
		// Set room name preferences
		if(name.length() < KatanaConstants.ROOMNAMEMIN) {
			// Room name is left blank
			client_gamePrefs.edit().putString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME).commit();
		} else {
			client_gamePrefs.edit().putString(KatanaConstants.GAME_NAME, name).commit();
		}
		
		// Set game difficulty preferences
		int diff = createRoomDialog_difficulty.getCheckedRadioButtonId();
		int difficulty = 1;
		switch(diff){
			case R.id.diff1: 
				client_gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Easy").commit();
				break;
			case R.id.diff2:
				difficulty = 2;
				client_gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Standard").commit();
				break;
			case R.id.diff3: 
				difficulty = 3;
				client_gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Hard").commit();
				break;
			default: 
				difficulty = 2;
				client_gamePrefs.edit().putString(KatanaConstants.GAME_DIFF, "Standard").commit();
				break;
		}
		
		// Set game max player preferences
		int plyr = createRoomDialog_maxPlayer.getCheckedRadioButtonId();
		int max = 1;
		switch(plyr){
			case R.id.plyr1: 
				client_gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 1).commit();
				break;
			case R.id.plyr2:
				max = 2;
				client_gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 2).commit();
				break;
			case R.id.plyr3:
				max = 3;
				client_gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 3).commit();
				break;
			case R.id.plyr4: 
			default: 
				max = 4;
				client_gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, 4).commit();
				break;	
		}
		
		lobby_selectedRoom = new Room(-1, name, difficulty, max);
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_CREATE);
		packet.addData(name);
		packet.addData(difficulty + "");
		packet.addData(max + "");
		packet.addData(client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1) + "");
		katanaService.sendPacket(packet);
	}

    public void lobbyJoinCreatedRoom() {
		client_inRoom = true;
		client_roomLeader = true;
		this.waitingRoomShowPlayers(new ArrayList<String>());
		String name = client_gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		String diff = client_gamePrefs.getString(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
		room_gameName.setText(name);
		room_difficulty.setText(diff);
		transitionToWaitingRoom();
	}

	public void lobbyTransitionToWaitingRoom(Room selected){
		client_inRoom = true;
		room_gameName.setText(selected.getName());
		room_difficulty.setText(Integer.toString(selected.getDifficulty()));
		transitionToWaitingRoom();
	}

	/** Waiting room methods */
	public void waitingRoomShowPlayers(ArrayList<String> al) {
		// Update user interface and bounce user to waiting room
		room_playerList = new ArrayList<Player>();
		room_playerRef = new HashMap<Integer, Integer>();
		int position = 0;
		for(int i = 0; i < al.size(); i++) {
			System.out.println(al.get(i));
			String[] lines = al.get(i).split(";");
			if(lines.length < 3)
				continue;
			position = i;
			room_playerRef.put(Integer.parseInt(lines[0]), i);
			room_playerList.add(new Player(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2])));
		}
		int pid = getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE).getInt(KatanaConstants.PLAYER_ID, 0);
		if(al.isEmpty()){
			room_playerRef.put(pid, 0);
		} else {
			room_playerRef.put(pid, position + 1);
		}
		room_playerList.add(new Player(pid, getSharedPreferences(KatanaConstants.PREFS_LOGIN, MODE_PRIVATE).getString(KatanaConstants.LOGIN_USER, ""), client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1)));
		waitingRoomUpdatePlayers();
	}

	public void waitingRoomClassUpdate(int playerid, int classid) {
    	System.out.println("Player " + playerid + " is changing class from " + room_playerList.get(room_playerRef.get(playerid)).getClassId() + " to " + classid);
    	room_playerList.get(room_playerRef.get(playerid)).setClassId(classid);
    	waitingRoomUpdatePlayers();
    }
    
    public void waitingRoomAddPlayer(Intent intent){
    	String s = intent.getStringExtra(KatanaService.EXTRAS_PLAYERNAME);
    	int ref = room_playerList.size();
    	String lines[] = s.split(";");
    	room_playerRef.put(Integer.parseInt(lines[0]), ref);
    	room_playerList.add(new Player(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2])));
    	waitingRoomUpdatePlayers();
    }
    
    public void waitingRoomRemovePlayer(int player_id) {
    	int ref = room_playerRef.get(player_id);
    	System.out.println(ref);
    	System.out.println(room_playerList.size());
    	
    	for(int value : room_playerRef.values()) {
    		if(value  > ref) {
    			value--;
    		}
    	}
    	room_playerList.remove(ref);
    	room_playerRef.remove(player_id);
    	waitingRoomUpdatePlayers();
    }
    
    private void waitingRoomRequestClassChange(int i){
		int pid = getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).getInt(KatanaConstants.PLAYER_ID, 0);
		System.out.println("my id: " + room_playerRef.get(pid));
		System.out.println("playerlist size: " + room_playerList.size());
		room_playerList.get(room_playerRef.get(pid)).setClassId(i);
		room_playerGridView.setAdapter(new PlayerListAdapter(this,room_playerList));
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_CLASS_CHANGE);
		packet.addData(Integer.toString(i));
		katanaService.sendPacket(packet);
    }
    
    public void waitingRoomUpdatePlayers() {
		room_playerGridView.setAdapter(new PlayerListAdapter(this, room_playerList));
	}

	/** public get/set methods methods */
    public void setLatitude(double d) {
    	client_latitude = d;
    }
    
    public void setLongitude(double d) {
    	client_longitude = d;
    }

	public void setCreatedRoomId(int i) {
		client_createdRoomId = i;
	}

	public void setSelectedRoom(Room r) {
		lobby_selectedRoom = r;
	}

	public void setInRoom(boolean b) {
		client_inRoom = b;
		
	}

	public void setRoomLeader(boolean b) {
		client_roomLeader = b;
		
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

	/** KatanaService binding */
	private void doBindService(){
	    Intent intent = new Intent(this,KatanaService.class);
	    bindService(intent, katanaConnection, Context.BIND_AUTO_CREATE);
	    registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
	    registerReceiver(katanaLocReceiver, new IntentFilter(KatanaService.BROADCAST_LOCATION));
	}

	private void doUnbindService() {
	    if (serviceBound) {
	        // Detach our existing connection and broadcast receiver
	        unbindService(katanaConnection);
	        unregisterReceiver(katanaReceiver);
	        unregisterReceiver(katanaLocReceiver);
	        stopService(new Intent(LobbyActivity.this,KatanaService.class));
	        serviceBound = false;
	    }
	}

	private ServiceConnection katanaConnection = new ServiceConnection() {
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
	/** OnClickListeners */
	private OnClickListener class1Listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)selectClassDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)selectClassDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)selectClassDialog.findViewById(R.id.l_classdesc);
	    	
			if(selectClassDialog_selection != 0){
				// A class is already highlighted
				if(selectClassDialog_selection == 1){
					// Go into game with class 1
					client_gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selectClassDialog_selection).commit();
					if(!client_inRoom) 
						lobbySendJoinRequest();
					else {
						waitingRoomRequestClassChange(1);
					}
					selectClassDialog.dismiss();
				} else {
					// Highlight class 1
					selectClassDialog_selection = 1;
					b_class1.setImageResource(R.drawable.class1_selected);
		    		b_class2.setImageResource(R.drawable.class2);
		    		desc.setText(KatanaConstants.DESC_CLASS1);
				}
			} else {
				// Highlight class 1
				selectClassDialog_selection = 1;
				b_class1.setImageResource(R.drawable.class1_selected);
	    		b_class2.setImageResource(R.drawable.class2);
				desc.setText(KatanaConstants.DESC_CLASS1);
			}
		}
	};
	
	private OnClickListener class2Listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)selectClassDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)selectClassDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)selectClassDialog.findViewById(R.id.l_classdesc);
	    	
			if(selectClassDialog_selection != 0){
				// A class is already highlighted
				if(selectClassDialog_selection == 2){
					// Go into game with class 2
					client_gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selectClassDialog_selection).commit();
					if(!client_inRoom)
						lobbySendJoinRequest();
					else {
						waitingRoomRequestClassChange(2);
					}
					selectClassDialog.dismiss();
				} else{
					// Highlight class 2
					selectClassDialog_selection = 2;
					b_class1.setImageResource(R.drawable.class1);
		    		b_class2.setImageResource(R.drawable.class2_selected);
		    		desc.setText(KatanaConstants.DESC_CLASS2);
				}
			} else {
				// Highlight class 2
				selectClassDialog_selection = 2;
				b_class1.setImageResource(R.drawable.class1);
	    		b_class2.setImageResource(R.drawable.class2_selected);
	    		desc.setText(KatanaConstants.DESC_CLASS2);
			}
		}
	};
	
	private OnClickListener class1newRoomListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)selectClassDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)selectClassDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)selectClassDialog.findViewById(R.id.l_classdesc);
	    	
			if(selectClassDialog_selection != 0){
				// A class is already highlighted
				if(selectClassDialog_selection == 1){
					client_gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selectClassDialog_selection).commit();
					lobbySendCreateRequest();
					selectClassDialog.dismiss();
				} else {
					// Highlight class 1
					selectClassDialog_selection = 1;
					b_class1.setImageResource(R.drawable.class1_selected);
		    		b_class2.setImageResource(R.drawable.class2);
		    		desc.setText(KatanaConstants.DESC_CLASS1);
				}
			} else {
				// Highlight class 1
				selectClassDialog_selection = 1;
				b_class1.setImageResource(R.drawable.class1_selected);
	    		b_class2.setImageResource(R.drawable.class2);
				desc.setText(KatanaConstants.DESC_CLASS1);
			}
		}
	};
	
	private OnClickListener class2newRoomListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageButton b_class1 = (ImageButton)selectClassDialog.findViewById(R.id.b_class1);
	    	ImageButton b_class2 = (ImageButton)selectClassDialog.findViewById(R.id.b_class2);
	    	TextView desc = (TextView)selectClassDialog.findViewById(R.id.l_classdesc);
	    	
			if(selectClassDialog_selection != 0){
				// A class is already highlighted
				if(selectClassDialog_selection == 2){
					client_gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, selectClassDialog_selection).commit();
					lobbySendCreateRequest();
					selectClassDialog.dismiss();
				} else{
					// Highlight class 2
					selectClassDialog_selection = 2;
					b_class1.setImageResource(R.drawable.class1);
		    		b_class2.setImageResource(R.drawable.class2_selected);
		    		desc.setText(KatanaConstants.DESC_CLASS2);
				}
			} else {
				// Highlight class 2
				selectClassDialog_selection = 2;
				b_class1.setImageResource(R.drawable.class1);
	    		b_class2.setImageResource(R.drawable.class2_selected);
	    		desc.setText(KatanaConstants.DESC_CLASS2);
			}
		}
	};
	
	private OnItemClickListener selectRoomListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			lobby_selectedRoom = (Room) lobby_roomList.get(position);
			lobby_roomName.setText(lobby_selectedRoom.getName());
			lobby_difficultyLabel.setText(Integer.toString(lobby_selectedRoom.getDifficulty()));
			lobby_maxPlayerLabel.setText(Integer.toString(lobby_selectedRoom.getMaxPlyr()));
		}
	};
}

