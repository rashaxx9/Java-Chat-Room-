package client;

import javax.swing.*;
import java.awt.*;

public class LoginGUI extends JFrame {
    private JTextField serverIpField, portField, usernameField;
    private JButton loginButton;
    private ChatClient chatClient;
    
    public LoginGUI() {
        initGUI();
    }
    
    private void initGUI() {
        setTitle("Chat Client - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Server IP:"), gbc);
        gbc.gridx = 1;
        serverIpField = new JTextField("localhost", 15);
        mainPanel.add(serverIpField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("12345", 15);
        mainPanel.add(portField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginButton = new JButton("Login");
        mainPanel.add(loginButton, gbc);
        
        loginButton.addActionListener(e -> attemptLogin());
        add(mainPanel);
    }
    
    private void attemptLogin() {
        String serverIp = serverIpField.getText();
        int port = Integer.parseInt(portField.getText());
        String username = usernameField.getText();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username");
            return;
        }
        
        chatClient = new ChatClient(serverIp, port, username, this);
        if (chatClient.connect()) {
            chatClient.sendLogin();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to connect to server");
        }
    }
    
    public void onLoginSuccess() {
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI = new ChatGUI(chatClient);
            chatClient.setChatGUI(chatGUI);
            dispose();
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUI().setVisible(true));
    }
}