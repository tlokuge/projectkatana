package server;

import java.util.HashMap;

public class UpdateThread implements Runnable
{
    private int diff;
    private Thread thread;
    
    public UpdateThread(int diff)
    {
        this.diff = diff;
        
        thread = new Thread(this, "UpdateThread");
        thread.start();
    }
    
    public void updateLoop()
    {
        System.out.println("updateLoop: " + System.currentTimeMillis());
        HashMap<Integer, Player> players = KatanaServer.instance().getPlayers();
        for(Integer i : players.keySet())
            players.get(i).update(diff);
    }
    
    public void run()
    {
        try
        {
            while(true)
            {
                updateLoop();
                thread.sleep(diff);
            }
        }
        catch(Exception ex)
        {
            System.err.println("Exception in UpdateThread: " + ex.getLocalizedMessage());
            System.exit(1); // System.exit(Constants.UPDATE_THREAD_FAIL);
        }
    }
    
}
