package server;

import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class ChatServer extends JFrame {
    private JTextField portField;
    private JButton startButton, stopButton, kickButton;
    private JTextArea chatArea, logArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    private ServerSocket serverSocket;
    private boolean isRunning;
    private List<ClientHandler> clients;
    private List<String> activeUsers;
    private SharedBuffer sharedBuffer;
    private Dispatcher dispatcher;
    
    public ChatServer() {
        clients = Collections.synchronizedList(new ArrayList<>());
        activeUsers = Collections.synchronizedList(new ArrayList<>());
        sharedBuffer = new SharedBuffer(100);
        dispatcher = new Dispatcher(sharedBuffer, this);
        dispatcher.start();
        initGUI();
    }
    
    private void initGUI() {
        setTitle("Chat Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Port:"));
        portField = new JTextField("12345", 6);
        controlPanel.add(portField);
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        add(controlPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        centerPanel.add(new JScrollPane(chatArea));
        
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.add(new JLabel("Active Users"), BorderLayout.NORTH);
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        usersPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        kickButton = new JButton("Kick Selected User");
        usersPanel.add(kickButton, BorderLayout.SOUTH);
        centerPanel.add(usersPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
        
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        kickButton.addActionListener(e -> kickUser());
        
        setVisible(true);
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            serverSocket = new ServerSocket(port);
            isRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            log("Server started on port " + port);
            
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, sharedBuffer, this);
                        clients.add(handler);
                        handler.start();
                        log("New client connected");
                    } catch (Exception e) {}
                }
            }).start();
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }
    
    private void stopServer() {
        isRunning = false;
        try {
            Request stopRequest = new Request(Request.Type.STOP_SERVER, "SERVER", "");
            sharedBuffer.put(stopRequest);
        } catch (Exception e) {}
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
    
    private void kickUser() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null) {
            Request kickRequest = new Request(Request.Type.KICK, "SERVER", "");
            kickRequest.setTargetUser(selectedUser);
            try {
                sharedBuffer.put(kickRequest);
            } catch (Exception e) {}
        }
    }
    
    public void broadcastMessage(String message, String sender) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[" + sender + "]: " + message + "\n");
        });
        
        synchronized(clients) {
            for (ClientHandler client : clients) {
                client.sendMessage("MSG:" + sender + ":" + message);
            }
        }
    }
    
    public void broadcastSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[SYSTEM]: " + message + "\n");
        });
        
        synchronized(clients) {
            for (ClientHandler client : clients) {
                client.sendMessage("SYS:" + message);
            }
        }
    }
    
    public void updateUserList() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            synchronized(activeUsers) {
                for (String user : activeUsers) {
                    userListModel.addElement(user);
                }
            }
        });
        
        StringBuilder userListStr = new StringBuilder("USERS:");
        synchronized(activeUsers) {
            for (String user : activeUsers) {
                userListStr.append(user).append(",");
            }
        }
        
        synchronized(clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(userListStr.toString());
            }
        }
    }
    
    
        public void addUser(String username, ClientHandler handler) {
        synchronized(activeUsers) {
        if (!activeUsers.contains(username)) {
            activeUsers.add(username);
            }
        }
        updateUserList();
        broadcastSystemMessage("User " + username + " has joined the chat");
    }
    
    public void removeUser(String username) {
        synchronized(activeUsers) {
            activeUsers.remove(username);
        }
        synchronized(clients) {
            clients.removeIf(c -> username.equals(c.getUsername()));
        }
        updateUserList();
        broadcastSystemMessage("User " + username + " has left the chat");
    }
    
    public void disconnectUser(String username) {
        synchronized(clients) {
            for (ClientHandler client : clients) {
                if (username.equals(client.getUsername())) {
                    client.disconnect();
                    break;
                }
            }
        }
        removeUser(username);
        broadcastSystemMessage("User " + username + " was removed by the server");
    }
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new Date() + "] " + message + "\n");
        });
    }
    
    public List<ClientHandler> getClients() { return clients; }
    public List<String> getActiveUsers() { return activeUsers; }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatServer());
    }
}