package server;

import java.io.*;
import java.net.Socket;
import java.util.AbstractMap.SimpleEntry;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private SharedBuffer sharedBuffer;
    private ChatServer server;
    private String username;
    private boolean connected;
    
    public ClientHandler(Socket socket, SharedBuffer sharedBuffer, ChatServer server) {
        this.socket = socket;
        this.sharedBuffer = sharedBuffer;
        this.server = server;
        this.connected = true;
        
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            server.log("ClientHandler created for new client");
        } catch (IOException e) {
            server.log("Error initializing client: " + e.getMessage());
        }
    }
    
    public void run() {
        try {
            String inputLine;
            while (connected && (inputLine = reader.readLine()) != null) {
                server.log("Received from client: " + inputLine);
                
                String[] parts = inputLine.split(":", 3);
                String type = parts[0];
                
                if (type.equals("LOGIN")) {
                    username = parts[1];
                    Request request = new Request(Request.Type.LOGIN, username, "");
                    // Put BOTH request and this handler into buffer
                    sharedBuffer.put(new SimpleEntry<>(request, this));
                    server.log("Login request from " + username + " added to buffer");
                    
                } else if (type.equals("MSG")) {
                    String message = parts[2];
                    Request request = new Request(Request.Type.MESSAGE, username, message);
                    sharedBuffer.put(new SimpleEntry<>(request, this));
                    server.log("Message from " + username + " added to buffer");
                    
                } else if (type.equals("LOGOUT")) {
                    Request request = new Request(Request.Type.LOGOUT, username, "");
                    sharedBuffer.put(new SimpleEntry<>(request, this));
                    server.log("Logout from " + username + " added to buffer");
                }
            }
        } catch (Exception e) {
            server.log("Client " + username + " disconnected: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    public void sendMessage(String message) {
        if (writer != null && connected) {
            writer.println(message);
            server.log("Sent to client " + username + ": " + message);
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
    
    public String getUsername() { return username; }
}