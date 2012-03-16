package server;


import java.net.ServerSocket;

import shared.Constants;
import shared.KatanaPacket;

public class KatanaServer implements Runnable
{
    ServerSocket listener;
    Thread thread;
    
    public KatanaServer() 
    {
        listener = null;
        thread = new Thread(this, "KatanaServer-Thread");
        thread.start();
    }
    
    public static void handlePacket(KatanaPacket packet)
    {
        System.out.println(packet.getPacketId() + ":" + packet.getOpcode().name());
        /******* TODO ****************/
    }
    
    public void listenLoop()
    {
        try
        {
            listener = new ServerSocket(Constants.SERVER_LISTEN_PORT);
            while(true)
                new Thread(new KatanaClientWorker(listener.accept())).start();
            // Garbage collection thread needed? Probably not
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void run() { listenLoop(); }
    protected void finalize()
    {
        try
        {
            if(listener != null)
                listener.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}