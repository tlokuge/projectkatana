package katana.receivers;

import java.util.ArrayList;

import katana.services.KatanaService;
import katana.shared.Opcode;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.katana.splash.LobbyActivity;
import com.katana.splash.LoginActivity;

public class KatanaReceiver extends BroadcastReceiver {
	private int mode;
	
	public KatanaReceiver(int m){
		mode = m;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(mode == 0) {
			// SplashActivity
    		if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			startMyActivity(context, LobbyActivity.class);
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_NO.name())){
    			startMyActivity(context, LoginActivity.class);
    		} 
		} else if(mode == 1) {
			// LoginActivity
			LoginActivity loginActivity = (LoginActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_REG_OK.name())){
    			// Registered new user on server    			
    			loginActivity.setUserLoginPrefs(true);
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			loginActivity.setUserLoginPrefs(false);
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_REG_NO.name())){
    			Toast.makeText(loginActivity, "Username already exists or wrong password!", Toast.LENGTH_SHORT).show();
    		} 
		} else if(mode == 2) {
			// LobbyActivity
			LobbyActivity lobby = (LobbyActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_LIST.name())){
				lobby.client_inValidLocation = true;
    			String locName = intent.getStringExtra(KatanaService.EXTRAS_LOCNAME);
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_ROOMSLIST);
    			lobby.lobbyShowRooms(locName, al);
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_JOIN_OK.name())) {
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_PLAYERLIST);
    			lobby.waitingRoomShowPlayers(al);
    			lobby.lobbyTransitionToWaitingRoom(lobby.lobby_selectedRoom);
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_JOIN_NO.name())) {
    			Toast.makeText(lobby, "You cant join this room.", Toast.LENGTH_SHORT).show();
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_PLAYER_JOIN.name())) {
    			lobby.waitingRoomAddPlayer(intent);
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_PLAYER_LEAVE.name())) {
    			lobby.waitingRoomRemovePlayer(Integer.parseInt((intent.getStringExtra(KatanaService.EXTRAS_PLAYERNAME))));
    		}
    		else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_CREATE_OK.name()))	{
    			lobby.setCreatedRoomId(Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_ROOMID)));
    			lobby.setRoomLeader(true);
    			lobby.lobbyJoinCreatedRoom();
    			lobby.lobbySendRoomListRequest(null);
    		}
    		else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_CREATE_NO.name()))	{
    			lobby.setSelectedRoom(null);
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_PLAYER_UPDATE_CLASS.name())) {
    			if(lobby.client_inRoom){
    				int playerid = intent.getIntExtra(KatanaService.EXTRAS_PLAYERID, -1);
    				int playerclass = intent.getIntExtra(KatanaService.EXTRAS_PLAYERCLASS, -1);
    				lobby.waitingRoomClassUpdate(playerid, playerclass);
    			}
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_DESTROY.name())) {
    			if(lobby.client_inRoom){
    				lobby.lobbySendRoomListRequest(null);
    				lobby.setInRoom(false);
    				lobby.setRoomLeader(false);
    				lobby.transitionToLobby();
    			}
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_LEADERBOARD.name())) {
    			lobby.setLeaderboardScores(intent.getStringExtra(KatanaService.EXTRAS_SCORES));
    			lobby.showLeaderboardDialog();
    		}
		} else if(mode == 3) {
			// GameActivity
			// Instantiate in onEngineStart() using 
			//		private KatanaReceiver katanaReceiver = new KatanaReceiver(3);
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_CREATE_NO.name())) { 
				// TODO Handle operations here
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void startMyActivity(Context context, Class myClass) {
		Intent intent = new Intent();
		intent.setClass(context, myClass);
		context.startActivity(intent);
	}
}