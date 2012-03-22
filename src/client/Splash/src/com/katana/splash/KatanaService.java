package com.katana.splash;

import shared.KatanaPacket;
import shared.KatanaSocket;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class KatanaService extends Service {
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder{
		KatanaService getService(){
			return KatanaService.this;
		}
	}
	
	KatanaSocket katanaSocket;
	private static final String SERVER = "projectkatana.no-ip.org";
	private static final int PORT = 7777;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public void onCreate(){
		katanaSocket = new KatanaSocket(SERVER,PORT);
	}
	
	/* methods for clients */
	public void sendPacket(KatanaPacket packet){
		katanaSocket.sendPacket(packet);
	}
}