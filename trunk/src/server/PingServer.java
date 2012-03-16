package server;


public class PingServer implements Runnable
{
    private Thread thread;
    
    public PingServer()
    {
        thread = new Thread(this, "PingServer");
        thread.start();
    }
    
    public void pingLoop()
    {
        try
        {
            // TODO
            Thread.sleep(10000);
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