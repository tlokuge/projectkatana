package server.utils;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;
import server.communication.KatanaServer;
import server.shared.Constants;

public abstract class Config
{
    private static HashMap<String, String> config_map;
    
    public static void loadConfig()
    {
        System.out.println("Loading Config....");
        config_map = new HashMap<String, String>();
        try
        {
            File config = new File(Constants.CONFIG_FILE);
            Scanner scan = new Scanner(new FileReader(config));
            while(scan.hasNextLine())
            {
                String line = scan.nextLine();
                String[] split = line.split(Constants.CONFIG_LINE_SEPERATOR);
                if(split.length == 1 || line.startsWith("*") || line.startsWith("/")) // Ignore any lines without a seperator or starting with * or /
                    continue;
                System.out.println("Loaded: [" + split[0].toUpperCase().trim() + " - '" + split[1].trim() + "']");
                config_map.put(split[0].toUpperCase().trim(), split[1].trim());
            }
            scan.close();
            System.out.println("Config Loaded");
        }
        catch(Exception ex)
        {
            System.err.println("Config: Error loading '" + Constants.CONFIG_FILE + "'");
            ex.printStackTrace();
            KatanaServer.exit(KatanaError.ERR_CONFIG_LOAD_FAIL);
        }
    }
    
    public static String getConfig(String option) { return config_map.get(option); }
}
