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
    
    public void updateLoop(int update_diff)
    {
        HashMap<Integer, Map> maps = KatanaServer.instance().getMaps();
        for(Integer i : maps.keySet())
            maps.get(i).update(update_diff);
    }
    
    public void run()
    {
        try
        {
            long time = System.currentTimeMillis();
            while(true)
            {
                updateLoop((int)(System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
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
