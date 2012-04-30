package server.main;

import server.communication.KatanaServer;
import server.handlers.SQLHandler;
import server.handlers.UpdateThread;
import server.shared.Constants;
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
        
        UpdateThread updater = new UpdateThread(
                Integer.parseInt(Config.getConfig(Constants.CONFIG_UPDATE_DIFF)),
                Integer.parseInt(Config.getConfig(Constants.CONFIG_PING_INTERVAL)),
                Integer.parseInt(Config.getConfig(Constants.CONFIG_MAX_PINGS)));
        updater.start();
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
    }
}