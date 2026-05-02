package cybershielddashboard;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen {
    public static void main(String[] args) {
        JFrame frame = new JFrame("CyberShield Login");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3,2));

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginBtn = new JButton("Login");

        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            if(user.equals("admin") && pass.equals("1234")) {
                JOptionPane.showMessageDialog(frame, "Access Granted");
                frame.dispose();
               CyberDashboard.main(null);
            } else {
                JOptionPane.showMessageDialog(frame, "Access Denied");
            }
        });

        frame.add(userLabel);
        frame.add(userField);
        frame.add(passLabel);
        frame.add(passField);
        frame.add(new JLabel());
        frame.add(loginBtn);

        frame.setVisible(true);
    }
}