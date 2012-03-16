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
            System.gc();
            System.runFinalization();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
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