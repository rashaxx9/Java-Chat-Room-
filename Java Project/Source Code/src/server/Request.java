package server;


    public class Request {
    public enum Type {
        LOGIN, MESSAGE, LOGOUT, KICK, STOP_SERVER
    }
    
    private Type type;
    private String username;
    private String message;
    private String targetUser;
    
    public Request(Type type, String username, String message) {
        this.type = type;
        this.username = username;
        this.message = message;
    }
    
    public Type getType() { return type; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public String getTargetUser() { return targetUser; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
}

