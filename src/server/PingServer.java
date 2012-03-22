package server;

import shared.KatanaPacket;
import shared.Opcode;


public class PingServer implements Runnable
{
    private Thread thread;
    private int interval;
    
    public PingServer(int interval)
    {
        this.interval = interval;
        if(interval < 10000)
            interval = 10000;
        
        thread = new Thread(this, "PingServer-Thread");
        thread.start();
    }
    
    public void pingLoop()
    {
        try
        {
            // TODO
            Thread.sleep(interval);
            SQLHandler.instance().executeQuery("SELECT 1 FROM `users` LIMIT 1");
            for(KatanaClientWorker client : KatanaServer.instance().getClients())
            {
                KatanaPacket packet = new KatanaPacket(-1, Opcode.S_PING);
                client.sendPacket(packet);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        // Loop forever~~
        pingLoop();
    }
    
    public void run()
    {
        pingLoop();
    }
    
    public void finalize()
    {
        //TODO;
    }
}