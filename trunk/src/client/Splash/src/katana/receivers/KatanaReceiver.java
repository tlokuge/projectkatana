package katana.receivers;

import java.util.ArrayList;

import katana.services.KatanaService;
import katana.shared.KatanaConstants;
import katana.shared.Opcode;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.katana.splash.GameActivity;
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
    			startMyActivity(context, LobbyActivity.class, null);
    			context.getSharedPreferences(KatanaConstants.PREFS_LOGIN, Context.MODE_PRIVATE).edit().putInt(
    					KatanaConstants.PLAYER_ID, 
    					Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID))
    					);
    			
    			KatanaService.player_id = Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_NO.name())){
    			startMyActivity(context, LoginActivity.class, null);
    		} 
		} else if(mode == 1) {
			// LoginActivity
			LoginActivity loginActivity = (LoginActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_REG_OK.name())){
    			// Registered new user on server    			
    			loginActivity.setUserLoginPrefs(true);
    			context.getSharedPreferences(KatanaConstants.PREFS_LOGIN, Context.MODE_PRIVATE).edit().putInt(
    					KatanaConstants.PLAYER_ID, 
    					Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID))
    					);
    			
    			KatanaService.player_id = Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_AUTH_OK.name())){
    			loginActivity.setUserLoginPrefs(false);
    			context.getSharedPreferences(KatanaConstants.PREFS_LOGIN, Context.MODE_PRIVATE).edit().putInt(
    					KatanaConstants.PLAYER_ID, 
    					Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID))
    					);
    			KatanaService.player_id = Integer.parseInt(intent.getStringExtra(KatanaService.EXTRAS_PLAYERID));
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_REG_NO.name())){
    			Toast.makeText(loginActivity, "Username already exists or wrong password!", Toast.LENGTH_SHORT).show();
    		}
		} else if(mode == 2) {
			// LobbyActivity
			LobbyActivity lobby = (LobbyActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_LIST.name())){
				lobby.setInValidLocation(true);
    			String locName = intent.getStringExtra(KatanaService.EXTRAS_LOCNAME);
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_ROOMSLIST);
    			lobby.lobbyShowRooms(locName, al);
    		} else if (intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_JOIN_OK.name())) {
    			ArrayList<String> al = intent.getStringArrayListExtra(KatanaService.EXTRAS_PLAYERLIST);
    			lobby.waitingRoomShowPlayers(al);
    			lobby.lobbyTransitionToWaitingRoom(lobby.getSelectedRoom());
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
    			lobby.lobbyRefresh(null);
    		}
    		else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_CREATE_NO.name()))	{
    			lobby.setSelectedRoom(null);
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_PLAYER_UPDATE_CLASS.name())) {
    			if(lobby.isInRoom()){
    				int playerid = intent.getIntExtra(KatanaService.EXTRAS_PLAYERID, -1);
    				int playerclass = intent.getIntExtra(KatanaService.EXTRAS_PLAYERCLASS, -1);
    				lobby.waitingRoomClassUpdate(playerid, playerclass);
    			}
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_ROOM_DESTROY.name())) {
    			if(lobby.isInRoom()){
    				lobby.lobbyRefresh(null);
    				lobby.setInRoom(false);
    				lobby.setRoomLeader(false);
    				lobby.transitionToLobby();
    			}
    		} else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_LEADERBOARD.name())) {
    			lobby.setLeaderboardScores(intent.getStringExtra(KatanaService.EXTRAS_SCORES));
    			lobby.showLeaderboardDialog();
    		}
    		else if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_START.name())) {
    			Bundle gBundle = new Bundle();
    			gBundle.putString(KatanaService.EXTRAS_GAMEBG, intent.getStringExtra(KatanaService.EXTRAS_GAMEBG));
    			gBundle.putStringArrayList(KatanaService.EXTRAS_GAMESTART, intent.getStringArrayListExtra(KatanaService.EXTRAS_GAMESTART));
    			System.out.println("gBundle: " + gBundle);
    			startMyActivity(context, GameActivity.class, gBundle);
    		}
		} else if(mode == 3) {
			// GameActivity
			GameActivity game = (GameActivity) context;
			if(intent.getStringExtra(KatanaService.OPCODE).equals(Opcode.S_GAME_UPDATE_MOVE.name())) {
				int id = intent.getIntExtra(KatanaService.EXTRAS_UNITMOVE, 0);
				float x = intent.getFloatExtra(KatanaService.EXTRAS_UNITMOVE_X, 0.0f);
				float y = intent.getFloatExtra(KatanaService.EXTRAS_UNITMOVE_Y, 0.0f);
				
				game.moveUnit(id, x, y);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void startMyActivity(Context context, Class myClass, Bundle myBundle) {
		Intent intent = new Intent();
		intent.setClass(context, myClass);
		if(myBundle != null)
			intent.putExtras(myBundle);
		context.startActivity(intent);
	}
}