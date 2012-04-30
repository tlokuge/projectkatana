package server.handlers;

import java.util.HashMap;
import java.util.Set;
import server.communication.KatanaClient;
import server.communication.KatanaServer;
import server.game.Map;
import server.game.Player;
import server.shared.KatanaPacket;
import server.shared.Opcode;

public class UpdateThread
{
    private int diff;
    private int ping_interval;
    private int ping_timer;
    private int max_pings;
    
    public UpdateThread(int diff, int ping_interval, int max_pings)
    {
        this.diff = diff;
        
        this.ping_interval = ping_interval;
        this.ping_timer    = ping_interval;
        this.max_pings     = max_pings;
    }
    
    public void updateLoop(int update_diff)
    {
        try
        {
            if(ping_timer < diff)
            {
                // SQL Connection keep alive
                SQLHandler.instance().runPingQuery();

                // Ping waiting clients
                for(KatanaClient client : KatanaServer.instance().getWaitingClients())
                    client.sendPacket(new KatanaPacket(Opcode.S_PING));
                
                // Ping authenticated players
                Set<Integer> keys = GameHandler.instance().getPlayers().keySet();
                for(Integer key : keys)
                {
                    Player pl = GameHandler.instance().getPlayer(key);
                    if(pl.getClient().getPingsSent() > max_pings)
                    {
                        System.err.println("Maximum number of pings (" + max_pings + ") sent to player " + pl + " - disconnecting them");
                        pl.getClient().remove(true);
                    }
                    else
                        pl.getClient().sentPing();
                    
                    pl.sendPacket(new KatanaPacket(Opcode.S_PING));
                }                    
                ping_timer = ping_interval;
            }else ping_timer -= diff;
            
            HashMap<Integer, Map> maps = GameHandler.instance().getMaps();
            for(Integer i : maps.keySet())
            {
                Map map = maps.get(i);
                if(map != null)
                    map.update(update_diff);
            }
            
            GameHandler.instance().safelyRemoveMaps();
            GameHandler.instance().safelyRemovePlayers();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void start()
    {
        try
        {
            long time = System.currentTimeMillis();
            while(true)
            {
                updateLoop((int)(System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
                Thread.currentThread().sleep(diff);
            }
        }
        catch(Exception ex)
        {
            System.err.println("Exception in UpdateThread: " + ex.getLocalizedMessage());
            ex.printStackTrace();
            System.exit(1); // System.exit(Constants.UPDATE_THREAD_FAIL);
        }
    }
    
}
