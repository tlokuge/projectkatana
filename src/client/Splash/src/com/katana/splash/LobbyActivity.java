package com.katana.splash;

import java.util.ArrayList;
import java.util.HashMap;

import katana.adapters.PlayerListAdapter;
import katana.dialogs.CreateRoomDialog;
import katana.dialogs.LeaderboardDialog;
import katana.dialogs.SelectClassDialog;
import katana.objects.Lobby;
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
import android.graphics.Typeface;
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
	/** Lobby Class **/
	public Lobby lobby;
	
	/** Lobby Variables */
	Room lobby_selectedRoom;
	ArrayList<Room> lobby_roomList;
	String lobby_lbScores;
	String lobby_lbNames;
	
	/** Waiting room Variables */
	private ArrayList<Player> room_playerList;
	private HashMap<Integer, Integer> room_playerRef;
	
	/** Client Variables */
	private boolean client_inRoom;
	private boolean client_inValidLocation;
	private boolean client_roomLeader;
	private double client_latitude;
	private double client_longitude;
	private SharedPreferences client_gamePrefs;
	
	/** Android Power Manager */
	private PowerManager.WakeLock wakeLockManager;
	
	/** HARDCODING STUFF :D **/
	Typeface font;
	
	/** Android Activity Lifecycle */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hard code themes for now :D
		
		// NOTEBOOK
		//setContentView(R.layout.notebook_vf);
		//font = Typeface.createFromAsset(getAssets(), "fonts/mvboli.ttf");
		
		// RYERSON
		setContentView(R.layout.ryerson_vf);
		font = Typeface.createFromAsset(getAssets(), "fonts/majalla.ttf");
		
		// Create Lobby
		lobby = new Lobby(this);
		
		// Load Preferences
		client_gamePrefs = getSharedPreferences(KatanaConstants.PREFS_GAME, MODE_PRIVATE);
		
		GridView lobby_roomGridView = (GridView) findViewById(R.id.gv_roomslist);
		lobby_roomGridView.setOnItemClickListener(selectRoomListener);
		
		TextView realmName = (TextView) findViewById(R.id.l_realmname);
		realmName.setTypeface(font);
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
	}
	
	/** Android Hardware Buttons */
	@Override
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called");
		if (client_inRoom) {
			client_inRoom = false;
			if(client_roomLeader) {
				client_roomLeader = false;
				KatanaPacket packet = new KatanaPacket(Opcode.C_ROOM_DESTROY);
				katanaService.sendPacket(packet);
				lobbyRefresh(null);
				transitionToLobby();
			} else {
				katanaService.sendPacket(new KatanaPacket(Opcode.C_ROOM_LEAVE));
				transitionToLobby();
				return;
			}
		} else {
			super.onBackPressed();
			katanaService.sendPacket(new KatanaPacket(Opcode.C_LOGOUT));
			this.finish();
		}
	}
	
	/** Android Menus */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	
    	MenuInflater inflater = getMenuInflater();
        if(!client_inRoom) {
        	inflater.inflate(R.menu.menu_lobby, menu);
        }
        else {
        	inflater.inflate(R.menu.menu_wroom, menu);
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
            	if(client_inValidLocation)
            		katanaService.sendPacket(new KatanaPacket(Opcode.C_LEADERBOARD));
                return true;
            case R.id.refresh:
            	lobbyRefresh(null);
            	return true;
            case R.id.change_class:
            	showSelectClassDialog();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } 
    
    /** Button View onPress */
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
    	katanaService.sendPacket(lobby.getRefreshPacket(client_latitude, client_longitude));
    }
    
    /** Lobby Methods */
	public void lobbyShowRooms(String locName, ArrayList<String> al){
		lobby.populate(al);
		lobby.showRooms();
		
		TextView realmName = (TextView) findViewById(R.id.l_realmname);
		realmName.setText(locName);
	}
    
	public void lobbySendJoinRequest(){		
		katanaService.sendPacket(lobby.getJoinRequestPacket(client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1)));
	}
	
	public void lobbySendCreateRequest(){
		String name = client_gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		int diff = client_gamePrefs.getInt(KatanaConstants.GAME_DIFF, KatanaConstants.DEF_ROOMDIFF);
		int maxp = client_gamePrefs.getInt(KatanaConstants.GAME_MAXP, KatanaConstants.DEF_ROOMMAXP);
		int classid = client_gamePrefs.getInt(KatanaConstants.GAME_CLASS, 1);
		
		katanaService.sendPacket(lobby.getCreateRequestPacket(name,diff,maxp,classid));
	}

	public void lobbyTransitionToWaitingRoom(){
		client_inRoom = true;
		String name;
		if(client_roomLeader){
			waitingRoomShowPlayers(new ArrayList<String>());
			name = client_gamePrefs.getString(KatanaConstants.GAME_NAME, KatanaConstants.DEF_ROOMNAME);
		} else {
			name = lobby.getSelectedName();
		}
		
		lobby.setSelectedRoom(-1);
		TextView room_gameName = (TextView)findViewById(R.id.l_wroomname);
		room_gameName.setTypeface(font);
		room_gameName.setText(name);
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
    
    public void waitingRoomAddPlayer(String lines[]){
    	int ref = room_playerList.size();
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
		
		KatanaPacket packet = new KatanaPacket(Opcode.C_CLASS_CHANGE);
		packet.addData(Integer.toString(i));
		katanaService.sendPacket(packet);
		
		waitingRoomUpdatePlayers();
    }
    
    public void waitingRoomUpdatePlayers() {
    	GridView room_playerGridView = (GridView)findViewById(R.id.gv_wroomlist);
		room_playerGridView.setAdapter(new PlayerListAdapter(this, room_playerList));
	}
	
    public void waitingRoomStartGame(View view) {
		KatanaPacket packet = new KatanaPacket(Opcode.C_GAME_START);
		katanaService.sendPacket(packet);
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
			if(lobby.getSelectedRoom() != position) {
				lobby.setSelectedRoom(position);
			} else {
				if(lobby.getSelectedRoom() == position) {
					showSelectClassDialog();
				}
			}
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
    
	@SuppressWarnings("unused")
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
    
    public void setLeaderboardNames(String s) {
    	lobby_lbNames = s;
    }
    
    public String getLeaderboardNames() {
    	return lobby_lbNames;
    }
    
    public void setLeaderboardScores(String s){
		lobby_lbScores = s;
	}
	
	public String getLeaderboardScores(){
		return lobby_lbScores;
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