package client;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private String serverIp;
    private int port;
    private String username;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private LoginGUI loginGUI;
    private ChatGUI chatGUI;
    private boolean connected;
    
    public ChatClient(String serverIp, int port, String username, LoginGUI loginGUI) {
        this.serverIp = serverIp;
        this.port = port;
        this.username = username;
        this.loginGUI = loginGUI;
        this.connected = false;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(serverIp, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            new Thread(this::listenForMessages).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void sendLogin() {
        writer.println("LOGIN:" + username);
    }
    
    public void sendMessage(String message) {
        writer.println("MSG:" + username + ":" + message);
    }
    
    public void sendLogout() {
        writer.println("LOGOUT:" + username);
    }
    
    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = reader.readLine()) != null) {
                if (line.startsWith("LOGIN_SUCCESS")) {
                    loginGUI.onLoginSuccess();
                } else if (line.startsWith("MSG:")) {
                    String[] parts = line.split(":", 3);
                    if (chatGUI != null) {
                        chatGUI.addMessage(parts[1], parts[2]);
                    }
                } else if (line.startsWith("SYS:")) {
                    if (chatGUI != null) {
                        chatGUI.addSystemMessage(line.substring(4));
                    }
                } else if (line.startsWith("USERS:")) {
                    String usersStr = line.substring(6);
                    String[] users = usersStr.split(",");
                    if (chatGUI != null) {
                        chatGUI.updateUserList(users);
                    }
                }
            }
        } catch (IOException e) {}
    }
    
    public void setChatGUI(ChatGUI chatGUI) {
        this.chatGUI = chatGUI;
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
    
    public String getUsername() { return username; }
}
