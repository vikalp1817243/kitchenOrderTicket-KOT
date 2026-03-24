package com.kot.ui.gui;

import com.kot.db.UserDAO;
import com.kot.model.User;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordWindow extends JFrame {

    private final User user;
    private final Runnable onSuccess;
    private final UserDAO userDAO;

    public ChangePasswordWindow(User user, Runnable onSuccess) {
        this.user = user;
        this.onSuccess = onSuccess;
        this.userDAO = new UserDAO();

        setTitle("First Time Login - Set Password");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("New Password:"));
        JPasswordField passField1 = new JPasswordField();
        panel.add(passField1);

        panel.add(new JLabel("Confirm Password:"));
        JPasswordField passField2 = new JPasswordField();
        panel.add(passField2);

        JButton saveBtn = new JButton("Save & Continue");
        panel.add(new JLabel()); // Spacer
        panel.add(saveBtn);

        saveBtn.addActionListener(e -> {
            String p1 = new String(passField1.getPassword());
            String p2 = new String(passField2.getPassword());

            if (p1.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                return;
            }

            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }

            boolean success = userDAO.updatePassword(user.getUserId(), p1);
            if (success) {
                user.setFirstLogin(false);
                JOptionPane.showMessageDialog(this, "Password changed successfully!");
                dispose();
                onSuccess.run();
            } else {
                JOptionPane.showMessageDialog(this, "Database error setting password.");
            }
        });

        add(panel);
    }
}
