package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
            this.interval = 10000;
        //this.interval = 500;
        thread = new Thread(this, "PingServer-Thread");
        thread.start();
    }
    
    public void pingLoop()
    {
        try
        {
            // TODO
            Thread.sleep(interval);
            //SQLHandler.instance().executeQuery("SELECT 1 FROM `users` LIMIT 1");
            
            HashMap<Integer, KatanaClient> map = KatanaServer.instance().getClients();
            Set<Integer> keys = map.keySet();
            for(Integer key : keys)
            {
                KatanaClient client = map.get(key);
                
                KatanaPacket packet = new KatanaPacket(-1, Opcode.S_PING);
                client.sendPacket(packet);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void run()
    {
        //Loop foreverrrrr~~~
        while(true)
            pingLoop();
    }
    
    public void finalize()
    {
        //TODO;
    }
}