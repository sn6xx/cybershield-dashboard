package cybershielddashboard;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CyberDashboard {

    private static String lastReportPath = "";
    private static javax.swing.Timer timer;

    private static final Map<String, Integer> failedLoginCount = new HashMap<>();
    private static final java.util.List<String> logLines = new ArrayList<>();
    private static final java.util.List<String> reportLines = new ArrayList<>();

    private static int currentIndex = 0;
    private static int totalLogs = 0;
    private static int mediumAlerts = 0;
    private static int highAlerts = 0;
    private static int criticalAlerts = 0;

    private static JLabel totalLabel;
    private static JLabel mediumLabel;
    private static JLabel highLabel;
    private static JLabel criticalLabel;

    public static void main(String[] args) {

        loadLogsFromFile();

        JFrame frame = new JFrame("CyberShield Dashboard");
        frame.setSize(850, 520);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel title = new JLabel("CyberShield Smart Threat Monitor", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setOpaque(true);
        title.setBackground(Color.BLACK);
        title.setForeground(Color.GREEN);

        JTextPane logs = new JTextPane();
        logs.setEditable(false);
        logs.setFont(new Font("Consolas", Font.PLAIN, 14));
        logs.setBackground(Color.BLACK);
        logs.setForeground(Color.GREEN);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4));

        totalLabel = new JLabel("Total Logs: 0", JLabel.CENTER);
        mediumLabel = new JLabel("Medium: 0", JLabel.CENTER);
        highLabel = new JLabel("High: 0", JLabel.CENTER);
        criticalLabel = new JLabel("Critical: 0", JLabel.CENTER);

        statsPanel.add(totalLabel);
        statsPanel.add(mediumLabel);
        statsPanel.add(highLabel);
        statsPanel.add(criticalLabel);

        JButton startBtn = new JButton("Start Analysis");
        JButton stopBtn = new JButton("Stop");
        JButton clearBtn = new JButton("Clear Logs");
        JButton exportBtn = new JButton("Export Report");
        JButton openBtn = new JButton("Open Last Report");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startBtn);
        buttonPanel.add(stopBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(openBtn);

        timer = new javax.swing.Timer(2000, e -> {
            if (currentIndex < logLines.size()) {
                String line = logLines.get(currentIndex);
                currentIndex++;

                String result = analyzeLog(line);
                appendColoredText(logs, result + "\n");
                reportLines.add(result);

                updateStats();

            } else {
                appendColoredText(logs, "=== No more logs to analyze ===\n");
                timer.stop();
            }
        });

        startBtn.addActionListener(e -> {
            if (!timer.isRunning()) {
                appendColoredText(logs, "=== Smart analysis started ===\n");
                timer.start();
            }
        });

        stopBtn.addActionListener(e -> {
            timer.stop();
            appendColoredText(logs, "=== Analysis stopped ===\n");
        });

        clearBtn.addActionListener(e -> {
            logs.setText("");
            reportLines.clear();
            resetStats();
        });

        exportBtn.addActionListener(e -> exportReport());

        openBtn.addActionListener(e -> {
            try {
                if (lastReportPath.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No report generated yet.");
                    return;
                }

                Desktop.getDesktop().open(new File(lastReportPath));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Unable to open file.");
            }
        });

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statsPanel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(title, BorderLayout.NORTH);
        frame.add(new JScrollPane(logs), BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void loadLogsFromFile() {
        try {
            InputStream inputStream =
                    CyberDashboard.class.getResourceAsStream("/resources/logs/security_logs.txt");

            if (inputStream == null) {
                JOptionPane.showMessageDialog(
                        null,
                        "Log file not found in resources/logs/security_logs.txt",
                        "File Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Scanner fileReader = new Scanner(inputStream);

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine().trim();

                if (!line.isEmpty()) {
                    logLines.add(line);
                }
            }

            fileReader.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error reading log file: " + e.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static String analyzeLog(String line) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        String[] parts = line.split(",");

        if (parts.length != 2) {
            totalLogs++;
            return "[" + time + "] [LOW] Invalid log format: " + line;
        }

        String ip = parts[0];
        String event = parts[1];

        totalLogs++;

        switch (event) {

            case "FAILED_LOGIN":
                int count = failedLoginCount.getOrDefault(ip, 0) + 1;
                failedLoginCount.put(ip, count);

                if (count >= 3) {
                    highAlerts++;
                    return "[" + time + "] [HIGH] Brute Force Attack detected from IP: " + ip;
                } else {
                    mediumAlerts++;
                    return "[" + time + "] [MEDIUM] Failed login attempt from IP: " + ip;
                }

            case "SQL_INJECTION":
                criticalAlerts++;
                return "[" + time + "] [CRITICAL] SQL Injection attempt from IP: " + ip;

            case "PORT_SCAN":
                mediumAlerts++;
                return "[" + time + "] [MEDIUM] Port scanning activity from IP: " + ip;

            case "LOGIN_SUCCESS":
                return "[" + time + "] [LOW] Normal successful login from IP: " + ip;

            default:
                return "[" + time + "] [LOW] Unknown event from IP: " + ip;
        }
    }

    private static void appendColoredText(JTextPane pane, String text) {
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("LogStyle", null);

        if (text.contains("[CRITICAL]")) {
            StyleConstants.setForeground(style, Color.RED);
            Toolkit.getDefaultToolkit().beep();
        } else if (text.contains("[HIGH]")) {
            StyleConstants.setForeground(style, Color.ORANGE);
        } else if (text.contains("[MEDIUM]")) {
            StyleConstants.setForeground(style, Color.YELLOW);
        } else {
            StyleConstants.setForeground(style, Color.GREEN);
        }

        try {
            doc.insertString(doc.getLength(), text, style);
            pane.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateStats() {
        totalLabel.setText("Total Logs: " + totalLogs);
        mediumLabel.setText("Medium: " + mediumAlerts);
        highLabel.setText("High: " + highAlerts);
        criticalLabel.setText("Critical: " + criticalAlerts);
    }

    private static void resetStats() {
        totalLogs = 0;
        mediumAlerts = 0;
        highAlerts = 0;
        criticalAlerts = 0;
        currentIndex = 0;
        failedLoginCount.clear();
        updateStats();
    }

    private static void exportReport() {
        try {
            String timeStamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

            String filePath = "reports/security_analysis_report_" + timeStamp + ".txt";

            Formatter output = new Formatter(filePath);

            output.format("CyberShield Security Analysis Report%n");
            output.format("------------------------------------%n");
            output.format("Total Logs: %d%n", totalLogs);
            output.format("Medium Alerts: %d%n", mediumAlerts);
            output.format("High Alerts: %d%n", highAlerts);
            output.format("Critical Alerts: %d%n%n", criticalAlerts);

            output.format("Analysis Details:%n");

            for (String line : reportLines) {
                output.format("%s%n", line);
            }

            output.close();

            lastReportPath = filePath;

            JOptionPane.showMessageDialog(
                    null,
                    "Report saved as:\n" + filePath,
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error exporting report.",
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}