package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import shared.Constants;
import shared.KatanaPacket;
import shared.Opcode;


public class PingServer implements Runnable
{
    private Thread thread;
    private int interval;
    private int max_pings;
    
    public PingServer(int interval, int max_pings)
    {
        this.interval = interval;
        if(interval < 10000)
            this.interval = 10000;
        this.max_pings = max_pings;
        if(max_pings < 5)
            this.max_pings = 5;
        thread = new Thread(this, "PingServer-Thread");
        thread.start();
    }
    
    public void pingLoop()
    {
        try
        {
            Thread.sleep(interval);
            SQLHandler.instance().runPingQuery();
            
            for(KatanaClient client : KatanaServer.instance().getWaitingClients())
            {
                KatanaPacket packet = new KatanaPacket(-1, Opcode.S_PING);
                client.sendPacket(packet);
            }
            
            HashMap<Integer, Player> map = KatanaServer.instance().getPlayers();
            Set<Integer> keys = map.keySet();
            for(Integer key : keys)
            {
                Player player = map.get(key);
                if(player.getClient().getPingsSent() > max_pings)
                {
                    System.err.println("Maximum number of pings (" + max_pings + ") sent to player " + player + " - disconnecting them");
                    player.getClient().remove(true);
                }
                else
                    player.getClient().sentPing();
                
                KatanaPacket packet = new KatanaPacket(-1, Opcode.S_PING);
                player.sendPacket(packet);
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