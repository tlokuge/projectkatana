package server;

public enum KatanaError 
{
    /** Error Codes **/
    // SQL
    ERR_SQL_CONN_LOST,
    ERR_SQL_CONN_FAIL,
    ERR_SQL_SINGLETON_LOST,

    // Config
    ERR_CONFIG_LOAD_FAIL,

    // Server
    ERR_SERVER_LISTEN_FAIL,
    ERR_SERVER_SINGLETON_LOST,

    // SQLCache
    ERR_SQLCACHE_NO_LOCATIONS,
    ERR_SQLCACHE_NO_SPELLS,
    ERR_SQLCACHE_NO_CLASSES,
    ERR_SQLCACHE_NO_MODELS,
    ERR_SQLCACHE_NO_MAPS,
};
