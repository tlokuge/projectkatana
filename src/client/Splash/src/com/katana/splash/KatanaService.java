package com.katana.splash;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import shared.Constants;
import shared.KatanaPacket;
import shared.Opcode;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class KatanaService extends Service {
	protected static final String BROADCAST_ACTION = "com.katana.splash";
	
	private final IBinder mBinder = new LocalBinder();
	private static final String SERVER = "projectkatana.no-ip.org";
	private static final int PORT = 7777;
	
	private Socket socket;
	private KatanaSocketListener socketListener;

	public class LocalBinder extends Binder {
		KatanaService getService() {
			return KatanaService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		try
		{
			socket = new Socket(SERVER, PORT);
			socketListener = new KatanaSocketListener(socket);
		}
		catch(Exception ex)
		{
			socket = null;
			socketListener = null;
			ex.printStackTrace();
			// Print error, quit?
		}
	}

	/* methods for clients */
	public void sendPacket(KatanaPacket packet) {
		if(socket == null)
			return;
		
		try
		{
			OutputStream out = socket.getOutputStream();
			out.write(packet.convertToBytes());
			out.flush();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	class KatanaSocketListener implements Runnable 
	{
		private Socket listener;
		private Thread thread;

		public KatanaSocketListener(Socket socket) 
		{
			listener = socket;
			thread = new Thread(this);

			thread.start();
		}

		public void listen() 
		{
			if (listener == null || listener.isClosed())
				return;

			try 
			{
				byte[] buffer = new byte[Constants.MAX_PACKET_BUF];
				InputStream in = listener.getInputStream();
				in.read(buffer);

				KatanaPacket packet = KatanaPacket.createPacketFromBuffer(new String(buffer));
				
				parsePacket(packet);
			}
			catch (ConnectException ex) 
			{
				System.err.println("KatanaSocketListener: Received ConnectException: "
								+ ex.getLocalizedMessage());
				closeListener();
				thread.interrupt();
			}
			catch (SocketException ex) 
			{
				System.err.println("KatanaSocketListener: Received SocketException: "
								+ ex.getLocalizedMessage());
				closeListener();
				thread.interrupt();
			}
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}
		}

		private void closeListener() 
		{
			try 
			{
				if (listener != null && !listener.isClosed())
					listener.close();
			}
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}
		}

		@Override
		public void run() 
		{
			while (true)
				listen();
		}

		@Override
		public void finalize() throws Throwable {
			super.finalize();
			closeListener();
		}
	}
	
	private void parsePacket(KatanaPacket packet){
    	Intent intent = new Intent(BROADCAST_ACTION);
    	System.out.println("Received packet: " + packet.getOpcode().name());
    	
    	switch(packet.getOpcode()){
    		case S_PING:
    			sendPacket(new KatanaPacket(0, Opcode.C_PONG)); break;
    		case S_REG_OK:
    		case S_REG_NO: 
    		case S_AUTH_OK:
    		case S_AUTH_NO: 
    			handleRegisterLoginReply(packet, intent); break;
    		case S_ROOM_LIST:
    			handleRoomListReply(packet, intent); break;
    		default: break;
    	}
    }
	
	private void handleRegisterLoginReply(KatanaPacket packet, Intent intent){
		intent.putExtra("opCode", packet.getOpcode().name());
		sendBroadcast(intent);
	}
	
	private void handleRoomListReply(KatanaPacket packet, Intent intent){
		ArrayList<String> roomsList = new ArrayList<String>();
		String[] lines = packet.getData().split(Constants.PACKET_DATA_SEPERATOR);
		String locName = lines[0];
		
		for(int i = 1; i < lines.length; i++){
			roomsList.add(lines[i]);
		}
		
		intent.putExtra("opCode", packet.getOpcode().name());
		intent.putExtra("locName" , locName);
		intent.putStringArrayListExtra("rooms", roomsList);
		sendBroadcast(intent);
	}
}