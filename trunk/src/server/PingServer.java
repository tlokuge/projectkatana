package server;


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