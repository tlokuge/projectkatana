package server.shared;

public enum Opcode
{
    /** CLIENT **/
    C_REGISTER,
    C_LOGIN,
    C_LOGOUT,
    C_PONG,
    C_ROOM_CREATE,
    C_ROOM_DESTROY,
    C_ROOM_JOIN,
    C_ROOM_LEAVE,
    C_ROOM_LIST,
    C_CLASS_CHANGE,
    C_LEADERBOARD,
    C_GAME_START,
    C_MOVE,
    C_SPELL,
    
    /** SERVER **/
    S_REG_OK,
    S_REG_NO,
    S_AUTH_OK,
    S_AUTH_NO,
    S_LOGOUT,
    S_PING,
    S_BAD_LOCATION,
    S_ROOM_CREATE_OK,
    S_ROOM_CREATE_NO,
    S_ROOM_DESTROY,
    S_ROOM_LIST,
    S_ROOM_PLAYER_JOIN,
    S_ROOM_JOIN_OK,
    S_ROOM_JOIN_NO,
    S_ROOM_PLAYER_LEAVE,
    S_PLAYER_UPDATE_CLASS,
    S_CLASS_CHANGE_OK,
    S_LEADERBOARD,
    S_GAME_START,
    S_GAME_UPDATE_SYNC,
    S_GAME_SPELL_CAST,
    S_GAME_UPDATE_MOVE,
    S_GAME_END;
    
    public static Opcode getOpcode(int code)
    {
        return Opcode.values()[code];
    }
};
