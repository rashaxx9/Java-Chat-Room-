package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatGUI extends JFrame {
    private ChatClient client;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, exitButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    public ChatGUI(ChatClient client) {
        this.client = client;
        initGUI();
        setVisible(true);
    }
    
    private void initGUI() {
        setTitle("Chat Room - " + client.getUsername());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
        
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        mainPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        
        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.add(new JLabel("Online Users"), BorderLayout.NORTH);
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setFixedCellWidth(150);
        usersPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        mainPanel.add(usersPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(sendButton);
        buttonPanel.add(exitButton);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
        
        sendButton.addActionListener(e -> sendMessage());
        exitButton.addActionListener(e -> logout());
        messageField.addActionListener(e -> sendMessage());
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            messageField.setText("");
        }
    }
    
    private void logout() {
        client.sendLogout();
        client.disconnect();
        dispose();
        System.exit(0);
    }
    
    public void addMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[" + sender + "]: " + message + "\n");
        });
    }
    
    public void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[SYSTEM]: " + message + "\n");
        });
    }
    
    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }
}
