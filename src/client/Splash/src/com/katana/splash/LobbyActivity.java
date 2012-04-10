package com.katana.splash;

import java.util.ArrayList;
import java.util.HashMap;

import katana.adapters.PlayerListAdapter;
import katana.adapters.RoomListAdapter;
import katana.dialogs.CreateRoomDialog;
import katana.dialogs.LeaderboardDialog;
import katana.dialogs.SelectClassDialog;
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
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class LobbyActivity extends Activity {
	/** Lobby Variables */
	Room lobby_selectedRoom;
	ArrayList<Room> lobby_roomList;
	String lobby_leaderboard;
	
	/** Waiting room Variables */
	private ArrayList<Player> room_playerList;
	private HashMap<Integer, Integer> room_playerRef;
	
	/** Client Variables */
	private boolean client_inRoom;
	private boolean client_inValidLocation;
	private boolean client_roomLeader;
	@SuppressWarnings("unused")
	private int client_createdRoomId = -1; // Probably not needed anymore :D
	private double client_latitude;
	private double client_longitude;
	private SharedPreferences client_gamePrefs;
	
	/** Android Power Manager */
	private PowerManager.WakeLock wakeLockManager;
	
	/** Android Activity Lifecycle */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewflipper);
		// Load Preferences
		client_gamePrefs = getSharedPreferences(KatanaConstants.PREFS_GAME, MODE_PRIVATE);
		
		GridView lobby_roomGridView = (GridView) findViewById(R.id.gv_roomslist);
		lobby_roomGridView.setOnItemClickListener(selectRoomListener);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		doBindService();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLockManager = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wakeLockManager.acquire();	
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		doUnbindService();
		wakeLockManager.release();
		this.finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//doKillService();
	}
	
	/** Android Hardware Buttons */
	@Override
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called");
		if (client_inRoom) {
			client_inRoom = false;
			if(client_roomLeader) {
				client_roomLeader = false;
				KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_DESTROY);
				katanaService.sendPacket(packet);
				lobbyRefresh(null);
				transitionToLobby();
			} else {
				katanaService.sendPacket(new KatanaPacket(0,Opcode.C_ROOM_LEAVE));
				transitionToLobby();
				return;
			}
		} else {
			super.onBackPressed();
			katanaService.sendPacket(new KatanaPacket(0,Opcode.C_LOGOUT));
			this.finish();
		}
	}
	
	/** Android Menus */
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
            	showSelectClassDialog();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } 
    
    /** Button View onPress */
    public void lobbyJoinButton(View v) {
    	if(!client_inValidLocation) {
    		Toast.makeText(getApplicationContext(), R.string.b_norealm, Toast.LENGTH_SHORT);
    		return;
    	}
    	showSelectClassDialog();
    }
    
    public void lobbyCreateButton(View v) {
    	if(!client_inValidLocation) {
    		Toast.makeText(getApplicationContext(), R.string.b_norealm, Toast.LENGTH_SHORT);
    		return;
    	}
    	setRoomLeader(true);
    	showCreateRoomDialog();
    }
    
    public void lobbyRefresh(View v) {
    	// Use last known location
    	client_latitude = katanaService.getLastKnownLocation().getLatitude();
    	client_longitude = katanaService.getLastKnownLocation().getLongitude();
    	
    	// Send server packet
    	KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_LIST);
		packet.addData(Double.toString(client_latitude));
		packet.addData(Double.toString(client_longitude));
		katanaService.sendPacket(packet);
    }
    
    public void waitingRoomStartGame(View view) {
    	katanaReceiver.startMyActivity(this, GameActivity.class);
    }
    
    /** Lobby Methods */
	public void lobbyShowRooms(String locName, ArrayList<String> al){
		lobby_roomList = new ArrayList<Room>();
		for (int i = 0; i < al.size(); i++){
			String[] lines = al.get(i).split(";");
			if(lines.length < 4)
				continue;
			lobby_roomList.add(new Room(Integer.parseInt(lines[0]),lines[1],Integer.parseInt(lines[2]),Integer.parseInt(lines[3])));
		}
		
		TextView realmName = (TextView) findViewById(R.id.l_realmname);
		realmName.setText(locName);
		GridView lobby_roomGridView = (GridView) findViewById(R.id.gv_roomslist);
		lobby_roomGridView.setAdapter(new RoomListAdapter(this,lobby_roomList));       
	}
    
	public void lobbySendJoinRequest(){
		// Send a join room packet to server
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_JOIN);
		packet.addData(Integer.toString(lobby_selectedRoom.getId()));
		packet.addData(Integer.toString(client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1)));
		katanaService.sendPacket(packet);
	}
	
	public void lobbySendCreateRequest(){
		String name = client_gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		int diff = client_gamePrefs.getInt(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
		int maxp = client_gamePrefs.getInt(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMMAXP);
		int classid = client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1);
		
		lobby_selectedRoom = new Room(-1, name, diff, maxp);
		
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_CREATE);
		packet.addData(name);
		packet.addData(diff + "");
		packet.addData(maxp + "");
		packet.addData(classid + "");
		katanaService.sendPacket(packet);
	}
	
	public void lobbyJoinCreatedRoom() {
		client_inRoom = true;
		client_roomLeader = true;
		waitingRoomShowPlayers(new ArrayList<String>());
		String name = client_gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		int diff = client_gamePrefs.getInt(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
		TextView room_gameName = (TextView)findViewById(R.id.l_wroomname);
        TextView room_difficulty = (TextView)findViewById(R.id.l_wroomdiff);
		room_gameName.setText(name);
		room_difficulty.setText(diff + "");
		transitionToWaitingRoom();
	}
	
	public void lobbyTransitionToWaitingRoom(Room selected){
		client_inRoom = true;
		TextView room_gameName = (TextView)findViewById(R.id.l_wroomname);
        TextView room_difficulty = (TextView)findViewById(R.id.l_wroomdiff);
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
    

    public void waitingRoomRequestClassChange(int i){
		int pid = getSharedPreferences(KatanaConstants.PREFS_LOGIN,MODE_PRIVATE).getInt(KatanaConstants.PLAYER_ID, 0);
		System.out.println("my id: " + room_playerRef.get(pid));
		System.out.println("playerlist size: " + room_playerList.size());
		room_playerList.get(room_playerRef.get(pid)).setClassId(i);
		
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_CLASS_CHANGE);
		packet.addData(Integer.toString(i));
		katanaService.sendPacket(packet);
		
		waitingRoomUpdatePlayers();
    }
    
    public void waitingRoomUpdatePlayers() {
    	GridView room_playerGridView = (GridView)findViewById(R.id.gv_wroomlist);
		room_playerGridView.setAdapter(new PlayerListAdapter(this, room_playerList));
	}
	
    /** Show Dialog Methods */
    public void showSelectClassDialog() {
    	SelectClassDialog dialog = new SelectClassDialog(this, R.style.DialogTheme);
    	dialog.show();
    }
    
    public void showCreateRoomDialog() {
    	CreateRoomDialog dialog = new CreateRoomDialog(this, R.style.DialogTheme);
    	dialog.show();
    }
    
    public void showLeaderboardDialog(){
    	LeaderboardDialog dialog = new LeaderboardDialog(this, R.style.DialogTheme);
    	dialog.show();
    }
	
    /** onClickListeners */
	private OnItemClickListener selectRoomListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			TextView roomName = (TextView) findViewById(R.id.l_roomname);
			TextView roomDiff = (TextView) findViewById(R.id.l_difficulty);
			TextView roomMaxp = (TextView) findViewById(R.id.l_maxplayer);
			
			lobby_selectedRoom = (Room) lobby_roomList.get(position);
			roomName.setText(lobby_selectedRoom.getName());
			roomDiff.setText(Integer.toString(lobby_selectedRoom.getDifficulty()));
			roomMaxp.setText(Integer.toString(lobby_selectedRoom.getMaxPlyr()));
		}
	};
    
	/** Required for KatanaService */
	private KatanaService katanaService;
	private KatanaReceiver katanaReceiver;
	private LocationReceiver katanaLocReceiver;
	private boolean serviceBound;
	
    private void doBindService() {
    	katanaReceiver = new KatanaReceiver(2);
    	katanaLocReceiver = new LocationReceiver();
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
            serviceBound = false;
        }
    }
    
	private void doKillService() {
        unbindService(katanaConnection);
        unregisterReceiver(katanaReceiver);
        unregisterReceiver(katanaLocReceiver);
        stopService(new Intent(this, KatanaService.class));
        serviceBound = false;
    }
	
    private ServiceConnection katanaConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            KatanaSBinder binder = (KatanaSBinder) service;
            katanaService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };
    
	/** Viewflipper transitions */
	public void transitionToLobby() {
		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		viewFlipper.setInAnimation(this, R.anim.transition_infrom_right);
		viewFlipper.setOutAnimation(this, R.anim.transition_outfrom_right);
		viewFlipper.showPrevious();
	}
	
	public void transitionToWaitingRoom() {
		ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
		viewFlipper.setInAnimation(this, R.anim.transition_infrom_left);
		viewFlipper.setOutAnimation(this, R.anim.transition_outfrom_left);
		viewFlipper.showNext();
	}
	
    /** Public getter/setter methods */
    public void setRoomLeader(boolean b) {
    	client_roomLeader = b;
    }
    
    public void setInRoom(boolean b) {
		client_inRoom = b;
	}
    
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
    
    public void setLeaderboardScores(String s){
		lobby_leaderboard = s;
	}
	
	public String getLeaderboardScores(){
		return lobby_leaderboard;
	}
	
	public SharedPreferences getGamePrefs() {
		return client_gamePrefs;
	}
	
	public void setPlayerClass(int classid) {
		client_gamePrefs.edit().putInt(KatanaConstants.GAME_CLASS, classid).commit();
	}
	
	public void setGamePrefs(SharedPreferences gamePrefs) {
		String gp_name = gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		int gp_diff = gamePrefs.getInt(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
		int gp_maxp = gamePrefs.getInt(KatanaConstants.GAME_MAXP, KatanaConstants.DEF_ROOMMAXP);
		
		client_gamePrefs.edit().putString(KatanaConstants.GAME_NAME, gp_name);
		client_gamePrefs.edit().putInt(KatanaConstants.GAME_DIFF, gp_diff);
		client_gamePrefs.edit().putInt(KatanaConstants.GAME_MAXP, gp_maxp);
	}
	
	public Room getSelectedRoom() {
		return lobby_selectedRoom;
	}
	
	public void setInValidLocation(boolean b) {
		client_inValidLocation = b;
	}
	
	public boolean isInValidLocation() {
		return client_inValidLocation;
	}
	
	public boolean isInRoom(){
		return client_inRoom;
	}
	
	public boolean isRoomLeader() {
		return client_roomLeader;
	}
}

