package server;

public class Worker extends Thread {
    private Request request;
    private ChatServer server;
    private ClientHandler handler;
    
    public Worker(Request request, ChatServer server, ClientHandler handler) {
        this.request = request;
        this.server = server;
        this.handler = handler;
    }
    
    public void run() {
        server.log("Worker processing: " + request.getType());
        
        if (request.getType() == Request.Type.LOGIN) {
            String username = request.getUsername();
            
            // Add user to active users list
            synchronized(server.getActiveUsers()) {
                if (!server.getActiveUsers().contains(username)) {
                    server.getActiveUsers().add(username);
                }
            }
            
           
            server.updateUserList();
            
            
            server.broadcastSystemMessage("User " + username + " has joined the chat");
            
            
            if (handler != null) {
                handler.sendMessage("LOGIN_SUCCESS");
                server.log("Sent LOGIN_SUCCESS to " + username);
            }
            
        } else if (request.getType() == Request.Type.MESSAGE) {
            server.broadcastMessage(request.getMessage(), request.getUsername());
            
        } else if (request.getType() == Request.Type.LOGOUT) {
            server.removeUser(request.getUsername());
            
        } else if (request.getType() == Request.Type.KICK) {
            server.disconnectUser(request.getTargetUser());
            
        } else if (request.getType() == Request.Type.STOP_SERVER) {
            server.log("Stopping server...");
            System.exit(0);
        }
    }
}