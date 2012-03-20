package server;

import java.net.InetAddress;

import shared.Constants;
import shared.KatanaPacket;
import shared.KatanaSocket;
import shared.Opcode;

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
        PingServer ping = new PingServer(
                Integer.parseInt(Config.getConfig(Constants.CONFIG_PING_INTERVAL)));
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
            Thread.currentThread().sleep(5000);
            KatanaPacket packet = new KatanaPacket(0, Opcode.C_LOGIN);
            packet.addData("katana"); // username
            packet.addData("password"); // password
            packet.addData("321.19, 291.32"); // location
            
            KatanaSocket.sendPacket(InetAddress.getLocalHost().getHostAddress(), KatanaServer.instance().getPort(), packet);
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
        
        testStuff();
        
    }
}