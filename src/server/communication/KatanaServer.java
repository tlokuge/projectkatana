package server.communication;

import java.net.ServerSocket;
import java.util.ArrayList;
import server.utils.KatanaError;

public class KatanaServer implements Runnable
{
    private ServerSocket listener;
    private int port;
    private Thread thread;
    
    private ArrayList<KatanaClient> waitingClients;
    private static KatanaServer instance;
    
    private KatanaServer(int port)
    {
        listener  = null;
        this.port = port;
        
        waitingClients = new ArrayList<KatanaClient>();
        
        thread = new Thread(this, "KatanaServer-Thread");
        thread.start();
    }
    
    public void listenLoop()
    {
        try
        {
            listener = new ServerSocket(port);
            while(true)
                new KatanaClient(listener.accept());
            
            // Garbage collection thread needed? Probably not
        }
        catch(Exception ex)
        {
            System.err.println("ERR: Unable to open listen port");
            ex.printStackTrace();
            exit(KatanaError.ERR_SERVER_LISTEN_FAIL);
        }
    }
    
    public void run()  { listenLoop(); }
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
    
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("DON'T CLONE SINGLETONS D<");
    }
    
    public static void initSingleton(int port)
    {
        if(instance == null)
            instance = new KatanaServer(port);
    }
    
    public synchronized static KatanaServer instance()
    {
        if(instance == null)
        {
            System.err.println("ERROR: LOST KATANASERVER SINGLETON!");
            exit(KatanaError.ERR_SERVER_SINGLETON_LOST);
        }
        
        return instance;
    }
    
    public static void exit(KatanaError error)
    {
        System.err.println("FATAL: " + error.name());
        System.exit(error.ordinal());
    }
    
    public void addWaitingClient(KatanaClient client)       { waitingClients.add(client); }
    public void removeWaitingClient(KatanaClient client)    { waitingClients.remove(client); }
    public ArrayList<KatanaClient> getWaitingClients()      { return waitingClients; }
    
    public int getPort() { return port; }
}