package com.katana.splash;

import java.util.ArrayList;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
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
    private ViewFlipper vf;
	
	private int classChoice = 0;
	private int selectClass = 0;
	
	private String roomName;
	private String difficulty;
	private int maxPlayers;
	
	private Dialog classDialog;
	private Dialog createRoomDialog; 
	private Dialog leaderboardDialog;
	
	private boolean newRoom = false;
	private boolean inRoom = false;
		
	//Service Vars
	KatanaService katanaService;
	boolean mBound = false;
	
	// Location Manager
	private String lat;
	private String lng;
	
	@Override 
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called"); 
		if (inRoom == true) {
			inRoom = false;
			vf.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_right);
    		vf.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_right);
    		vf.showPrevious();
		} else {
			this.finish();
		}
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewflipper);
        
        vf = (ViewFlipper)findViewById(R.id.flipper);
        
        ArrayList<String> rooms = new ArrayList<String>();
        rooms.add("abc");
        rooms.add("def");
        rooms.add("ghi");
        rooms.add("jkl");
        rooms.add("mno");
        rooms.add("pqr");
        rooms.add("stu"); 
        rooms.add("vwx");
        rooms.add("yz!");
        
        ListView listView = (ListView)findViewById(R.id.list_roomslist);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rooms));
        
        Button join_b = (Button)findViewById(R.id.b_join);
        join_b.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				classChoice = 0;
				newRoom = false;
				showSelectClassDialog();
			}
        });
        
        Button create_b = (Button)findViewById(R.id.b_create);
        create_b.setOnClickListener( new OnClickListener() {
        	public void onClick(View v){
        		classChoice = 0;
        		newRoom = true;
        		showCreateRoomDialog();
        	}
        });
        
        Button leader_b = (Button)findViewById(R.id.b_leaderboard);
        leader_b.setOnClickListener( new OnClickListener() {
        	public void onClick(View v){
        		showLeaderboardDialog();
        	}
        });
        
        Intent intent = new Intent(this,KatanaService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
        
        // Location Manager -- FIND A WAY TO LOOK UP LOCATION ONCLICK!!@#!@#!#@!@#
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
        
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 20, locListener);
    }
    
	protected void onStop(){
		super.onStop();
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_LOGOUT);
		katanaService.sendPacket(packet);
	}
	
    void doUnbindService() {
        if (mBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            unregisterReceiver(broadcastReceiver);
            stopService(new Intent(LobbyActivity.this,KatanaService.class));
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
    
    public void joinRoom(){
    	String message = "";
    	if(newRoom) {
    		message = "Create room! \n" +
    				roomName + "\n" +
    				difficulty + "\n" +
    				maxPlayers;
    		inRoom = true;
    		TextView l_wroomname = (TextView)findViewById(R.id.l_wroomname);
    		l_wroomname.setText(roomName);
    		TextView l_wroomdiff = (TextView)findViewById(R.id.l_wroomdiff);
    		l_wroomdiff.setText(difficulty);
    		
    		vf.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_left);
    		vf.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_left);
    		vf.showNext();
    	} else if (!newRoom) {
    		message = "Join Room!";
    		inRoom = true;
    		TextView l_wroomname = (TextView)findViewById(R.id.l_wroomname);
    		l_wroomname.setText(roomName);
    		
    		vf.setInAnimation(LobbyActivity.this, R.anim.transition_infrom_left);
    		vf.setOutAnimation(LobbyActivity.this, R.anim.transition_outfrom_left);
    		vf.showNext();
    	}
    	
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    public void showCreateRoomDialog(){
    	createRoomDialog = new Dialog(this, R.style.DialogTheme);
    	createRoomDialog.setContentView(R.layout.create_room);
    	createRoomDialog.setTitle("Create Room");
    	createRoomDialog.setCancelable(true);
    	
    	createRoomDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				createRoomDialog.dismiss();
			}   		
    	});
    	
    	ImageButton close = (ImageButton)createRoomDialog.findViewById(R.id.b_close);
    	close.setImageResource(R.drawable.close);
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
    
    public void showSelectClassDialog(){
    	classDialog = new Dialog(this, R.style.DialogTheme);
    	classDialog.setContentView(R.layout.select_class);
    	classDialog.setTitle("Select Class");
    	classDialog.setCancelable(true);
    	
    	classDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				selectClass = 0;
				classDialog.dismiss();
			}   		
    	}); 
    	  	
    	ImageButton class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
    	ImageButton class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
    	ImageButton close = (ImageButton)classDialog.findViewById(R.id.b_close);
    	
    	close.setImageResource(R.drawable.close);
    	close.setOnClickListener( new OnClickListener(){
    		public void onClick(View v) { 
				classDialog.cancel();
    		}
    	});
    	
    	classDialog.show();
    	
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
    	
    	class1.setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				if(selectClass != 1) {
					selectClass = 1;
					TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
					desc.setText("This is the mage. He can heal :D");
					
					/** Set class images to indicate change!**/
					ImageButton class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
			    	ImageButton class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
			    	class1.setImageResource(R.drawable.class1_selected);
		    		class2.setImageResource(R.drawable.class2);
					
					
				} else if(selectClass == 1){
					classChoice = 1;
					selectClass = 0;
					
					if(!inRoom)
						joinRoom();
					classDialog.dismiss();
				}
			}
    	});
    	
    	class2.setOnClickListener( new OnClickListener(){
			public void onClick(View v) {
				if(selectClass != 2) {
					selectClass = 2;
					TextView desc = (TextView)classDialog.findViewById(R.id.l_classdesc);
					desc.setText("This is the warrior. He is strong :D");
					
					/** Set class images to indicate change!**/
					ImageButton class1 = (ImageButton)classDialog.findViewById(R.id.b_class1);
			    	ImageButton class2 = (ImageButton)classDialog.findViewById(R.id.b_class2);
			    	class1.setImageResource(R.drawable.class1);
		    		class2.setImageResource(R.drawable.class2_selected);
		    		
				} else if(selectClass == 2){
					classChoice = 2;
					selectClass = 0;
					
					if(!inRoom)
						joinRoom();
					classDialog.dismiss();
				}
			}
    	});
    }
    
    public void showLeaderboardDialog(){
   	
    	leaderboardDialog = new Dialog(this, R.style.DialogTheme);
    	leaderboardDialog.setContentView(R.layout.leaderboard);
    	leaderboardDialog.setTitle("Leaderboards");
    	leaderboardDialog.setCancelable(true);
    	
    	leaderboardDialog.setOnCancelListener( new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				selectClass = 0;
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
    
    public void makeRoom(){
    	RadioGroup diffSel = (RadioGroup)LobbyActivity.this.createRoomDialog.findViewById(R.id.rg_roomdiff);
    	RadioGroup plyrSel = (RadioGroup)LobbyActivity.this.createRoomDialog.findViewById(R.id.rg_roommaxplr);
    	EditText rmName = (EditText)LobbyActivity.this.createRoomDialog.findViewById(R.id.l_roomname);
    	
    	roomName = rmName.getText().toString().trim();
    	if(roomName.length() < 1) {
    		roomName = "Let's Fun!";
    	}
    	
    	int diff = diffSel.getCheckedRadioButtonId();
    	switch(diff){
    		case R.id.diff1: difficulty = "Easy"; break;
    		case R.id.diff2: difficulty = "Standard"; break;
    		case R.id.diff3: difficulty = "Hard"; break;
    		default: difficulty = "Standard"; break;
    	}
    	
    	int plyr = plyrSel.getCheckedRadioButtonId();
    	switch(plyr){
			case R.id.plyr1: maxPlayers = 1; break;
			case R.id.plyr2: maxPlayers = 2; break;
			case R.id.plyr3: maxPlayers = 3; break;
			case R.id.plyr4: maxPlayers = 4; break;
			default: maxPlayers = 4; break;
    	}
    }

    public void refreshList(View view){
    	// Send server refresh room list packet
    	KatanaPacket packet = new KatanaPacket(0, Opcode.C_ROOM_LIST);
    	packet.addData(lat);
    	packet.addData(lng);
    	katanaService.sendPacket(packet);
    	Toast.makeText(getApplicationContext(), lat+" "+lng, Toast.LENGTH_SHORT).show();
    }
    
    private void showRoomList(Intent intent){
    	String locName = intent.getStringExtra("locName");
    	ArrayList<String> roomsList = intent.getStringArrayListExtra("rooms");
    	
    	TextView textView = (TextView)findViewById(R.id.l_realmname);
    	textView.setText(locName);
    	
    	ListView listView = (ListView)findViewById(R.id.list_roomslist);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roomsList));
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	System.out.println("Service is bound!");
            LocalBinder binder = (LocalBinder) service;
            katanaService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(intent.getStringExtra("opCode").equals(Opcode.S_ROOM_LIST.name())){
    			System.out.println("Got room list :D");
    			showRoomList(intent);
    		}
    	}
    };
}

