package com.kot.ui.gui;

import com.kot.core.SharedQueue;
import com.kot.db.UserDAO;
import com.kot.model.Chef;
import com.kot.model.MenuItem;
import com.kot.model.User;
import com.kot.model.Waiter;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LoginWindow extends JFrame {

    private final SharedQueue sharedQueue;
    private final Map<Integer, MenuItem> menuData;
    private final UserDAO userDAO;

    public LoginWindow(SharedQueue sharedQueue, Map<Integer, MenuItem> menuData) {
        this.sharedQueue = sharedQueue;
        this.menuData = menuData;
        this.userDAO = new UserDAO();

        setTitle("Kitchen Order Ticket (KOT) - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Central KOT Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(title);

        JPanel rolePanel = new JPanel(new GridLayout(1, 2));
        rolePanel.add(new JLabel("Role:"));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Owner", "Waiter", "Chef"});
        rolePanel.add(roleCombo);
        panel.add(rolePanel);

        JPanel userPanel = new JPanel(new GridLayout(1, 2));
        userPanel.add(new JLabel("Username:"));
        JTextField userField = new JTextField();
        userPanel.add(userField);
        panel.add(userPanel);

        JPanel passPanel = new JPanel(new GridLayout(1, 2));
        passPanel.add(new JLabel("Password:"));
        JPasswordField passField = new JPasswordField();
        passPanel.add(passField);
        panel.add(passPanel);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            String role = (String) roleCombo.getSelectedItem();
            String username = userField.getText();
            String password = new String(passField.getPassword());

            User user = userDAO.authenticate(username, password, role);

            if (user != null) {
                // Check multiple sessions
                if (com.kot.core.SessionManager.getInstance().isUserLoggedIn(user.getUserId())) {
                    JOptionPane.showMessageDialog(this, "This user is already logged in actively! (Close other window first)", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Success
                if (user.isFirstLogin()) {
                    new ChangePasswordWindow(user, () -> launchRoleWindow(user)).setVisible(true);
                } else {
                    launchRoleWindow(user);
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or role mismatch!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(loginBtn);

        add(panel);
    }

    private void launchRoleWindow(User user) {
        SwingUtilities.invokeLater(() -> {
            switch (user.getRole()) {
                case "Owner":
                    // Assuming Owner singleton handles its visibility
                    new OwnerWindow(sharedQueue, user).setVisible(true);
                    break;
                case "Waiter":
                    Waiter waiter = new Waiter(user.getUserId(), user.getName());
                    new WaiterWindow(waiter, sharedQueue, menuData).setVisible(true);
                    break;
                case "Chef":
                    Chef chef = new Chef(user.getUserId(), user.getName());
                    new ChefWindow(chef, sharedQueue).setVisible(true);
                    break;
            }
        });
    }
}
