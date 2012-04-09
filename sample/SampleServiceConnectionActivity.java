package com.katana.splash;

import katana.receivers.KatanaReceiver;
import katana.services.KatanaService;
import katana.services.KatanaService.KatanaSBinder;
import katana.shared.KatanaPacket;
import katana.shared.Opcode;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SampleServiceConnectionActivity extends Activity {
	
	/** Android Activity Lifecycle */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Put in onEngineStart()
		katanaReceiver = new KatanaReceiver(3);
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		// Binds to KatanaService
		doBindService();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		// Unbinds from KatanaService
		doUnbindService();
	}
	
	/** Android Hardware Buttons */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.d("CDA", "onBackPressed Called");
		KatanaPacket packet = new KatanaPacket(0, Opcode.C_LOGOUT);
		katanaService.sendPacket(packet);
		// Kills KatanaService
		doKillService();
	}
	
	/** Required for KatanaService */
	private KatanaService katanaService;
	private boolean serviceBound;
	private KatanaReceiver katanaReceiver;
	
    private void doBindService() {
        Intent intent = new Intent(this,KatanaService.class);
        bindService(intent, katanaConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(katanaReceiver, new IntentFilter(KatanaService.BROADCAST_ACTION));
    }
    
    private void doUnbindService() {
        if (serviceBound) {
            // Detach our existing connection and broadcast receiver
            unbindService(katanaConnection);
            unregisterReceiver(katanaReceiver);
            serviceBound = false;
        }
    }
    
    private void doKillService() {
        unbindService(katanaConnection);
        unregisterReceiver(katanaReceiver);
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
}