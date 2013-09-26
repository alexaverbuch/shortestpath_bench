package org.neo4j.bench.shortestpath;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Config
{
    static
    {
        try
        {
            Map properties = new Properties();
            ( (Properties) properties ).load( Config.class.getResourceAsStream( "/neo4j_config.properties" ) );
            NEO4J_CONFIG = properties;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        };
    }

    public static Map<String, String> NEO4J_CONFIG;

    public final static String DB_DIR = "db/";
}
