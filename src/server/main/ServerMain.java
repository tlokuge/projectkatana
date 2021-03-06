package server.main;

import server.communication.KatanaServer;
import java.util.Random;
import server.handlers.AIHandler;
import server.handlers.PingServer;
import server.handlers.SQLHandler;
import server.handlers.UpdateThread;
import server.shared.Constants;
import server.shared.KatanaPacket;
import server.shared.KatanaSocket;
import server.shared.Opcode;
import server.utils.Config;
import server.utils.SQLCache;

public class ServerMain
{
    public static void setupServer()
    {
        Config.loadConfig();
        SQLHandler.initSingleton(
                Config.getConfig(Constants.CONFIG_SQL_HOSTNAME), 
                Config.getConfig(Constants.CONFIG_SQL_DATABASE),
                Config.getConfig(Constants.CONFIG_SQL_USERNAME),
                Config.getConfig(Constants.CONFIG_SQL_PASSWORD));
        
        KatanaServer.initSingleton(
                Integer.parseInt(Config.getConfig(Constants.CONFIG_SERVER_PORT)));
        SQLCache.createCache();
        
        // Load AI
        AIHandler.instance();
        
        PingServer ping = new PingServer(
                Integer.parseInt(Config.getConfig(Constants.CONFIG_PING_INTERVAL)),
                Integer.parseInt(Config.getConfig(Constants.CONFIG_MAX_PINGS)));
        UpdateThread update = new UpdateThread(
                Integer.parseInt(Config.getConfig(Constants.CONFIG_UPDATE_DIFF)));
        
    }
    
    // Remove this method, just for testing
    public static void testStuff()
    {
        try
        {
            // Very hacky, but required because otherwise packets are sent immediately to the server
            // Since there are multiple threads running here, the server thread might not be ready 
            // (since the singleton may not be set up yet), and given the way it's been coded - it crashes.
            // This is intentional because if we somehow are unable to set up the Server's Singleton thread...
            //and attempt to access it, we should kill everything instead of letting things like SQL or Ping run..
            Thread.currentThread().sleep(1000);
            
            // Streessss testtt
            for(int i = 0; i < 100; ++i)
            {
                KatanaSocket client = new KatanaSocket("projectkatana.no-ip.org", 7777);
                Thread.currentThread().sleep(1000);

                KatanaPacket packet = new KatanaPacket(Opcode.C_REGISTER);
                packet.addData(i + ""); // username
                packet.addData(i + ""); // password
                client.sendPacket(packet);
                Thread.currentThread().sleep(500);

                packet = new KatanaPacket(Opcode.C_ROOM_LIST);
                packet.addData("43");
                packet.addData("-78");
                client.sendPacket(packet);
                Thread.currentThread().sleep(500);

                packet = new KatanaPacket(Opcode.C_ROOM_CREATE);
                packet.addData("Room " + i);
                packet.addData((new Random(System.currentTimeMillis()).nextInt(3) + 1) + "");
                packet.addData("4");
                packet.addData("2");
                client.sendPacket(packet);
                Thread.currentThread().sleep(500);
                /*
                packet = new KatanaPacket(Opcode.C_ROOM_JOIN);
                packet.addData("1");
                packet.addData("1");
                client.sendPacket(packet);
                Thread.currentThread().sleep(10000);

                packet = new KatanaPacket(Opcode.C_CLASS_CHANGE);
                packet.addData("2");
                client.sendPacket(packet);
                Thread.currentThread().sleep(10000);

                packet = new KatanaPacket(Opcode.C_ROOM_LEAVE);
                packet.addData("2");
                client.sendPacket(packet);
                Thread.currentThread().sleep(10000);

                Thread.currentThread().sleep(360000);
                packet = new KatanaPacket(Opcode.C_LOGOUT);
                client.sendPacket(packet);*/
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        System.out.println("  _  __      _______       _   _        ");
        System.out.println(" | |/ /   /\\|__   __|/\\   | \\ | |   /\\    ");
        System.out.println(" | ' /   /  \\  | |  /  \\  |  \\| |  /  \\   ");
        System.out.println(" |  <   / /\\ \\ | | / /\\ \\ | . ` | / /\\ \\  ");
        System.out.println(" | . \\ / ____ \\| |/ ____ \\| |\\  |/ ____ \\ ");
        System.out.println(" |_|\\_|_/    \\_\\_/_/    \\_\\_| \\_/_/    \\_\\");
        
        setupServer();
        
        //testStuff();
    }
}