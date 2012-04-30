package server.communication;

import java.net.ServerSocket;
import java.util.ArrayList;
import server.utils.KatanaError;

/**
 * The main server singleton, it accepts client connections and creates a new worker to handle them. It also stores all unauthenticated clients.
 */
public class KatanaServer implements Runnable
{
    private ServerSocket listener;
    private int port;
    private Thread thread;
    
    private ArrayList<KatanaClient> waitingClients;
    private static KatanaServer instance;
    
    /**
     * Constructs the server singleton, which listens for client sockets in its own thread
     * @param port The port with which to listen for client socket connections
     */
    private KatanaServer(int port)
    {
        listener  = null;
        this.port = port;
        
        // This list will store all clients before they are authenticated
        waitingClients = new ArrayList<KatanaClient>();
        
        thread = new Thread(this, "KatanaServer");
        thread.start();
    }
    
    public void listenLoop()
    {
        try
        {
            listener = new ServerSocket(port);
            while(true) // Accept a new client, and pass it off to a worker
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
    
    /**
     * Since this is a singleton, it cannot be cloned
     * @return
     * @throws CloneNotSupportedException 
     */
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("DON'T CLONE SINGLETONS D<");
    }
    
    /**
     * Instantiates the singleton object with the specified port
     * @param port The port with which the server will listen for clients
     */
    public static void initSingleton(int port)
    {
        if(instance == null)
            instance = new KatanaServer(port);
    }
    
    /**
     * Since this is a singleton, this is the only way to access the actual server object
     * @return The instance of this server
     */
    public synchronized static KatanaServer instance()
    {
        if(instance == null)
        {
            System.err.println("ERROR: LOST KATANASERVER SINGLETON!");
            exit(KatanaError.ERR_SERVER_SINGLETON_LOST);
        }
        
        return instance;
    }
    
    /**
     * Terminates the server due to a fatal error
     * @param error The error that occurred
     */
    public static void exit(KatanaError error)
    {
        System.err.println("FATAL: " + error.name());
        System.exit(error.ordinal());
    }
    
    // Simple mutator and accessors for waiting clients
    public void addWaitingClient(KatanaClient client)       { waitingClients.add(client); }
    public void removeWaitingClient(KatanaClient client)    { waitingClients.remove(client); }
    public ArrayList<KatanaClient> getWaitingClients()      { return waitingClients; }
}