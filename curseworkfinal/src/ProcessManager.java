import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;

public class ProcessManager {

    public void showProcessList(JTextArea outputArea, int count) {
        outputArea.append(String.format("%-8s %-25s %-6s %-6s%n", "PID", "COMMAND", "%CPU", "%MEM"));
        try {
            Process process = new ProcessBuilder("ps", "-eo", "pid,comm,%cpu,%mem").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.readLine();
            int shown = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (count != -1 && shown >= count) break;
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length < 4) continue;
                outputArea.append(String.format("%-8s %-25s %-6s %-6s%n", parts[0], parts[1], parts[2], parts[3]));
                shown++;
            }
            reader.close();
        } catch (Exception e) {
            outputArea.append("Ошибка при получении списка процессов: " + e.getMessage() + "\n");
        }
    }

    public void showTopHeavyProcesses(JTextArea outputArea, int count) {
        outputArea.append("ТОП " + count + " процессов по CPU:\n");
        outputArea.append(String.format("%-8s %-25s %-6s %-6s%n", "PID", "COMMAND", "%CPU", "%MEM"));
        try {
            Process process = new ProcessBuilder("ps", "-eo", "pid,comm,%cpu,%mem", "--no-headers").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String[]> processList = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length == 4) processList.add(parts);
            }
            reader.close();

            processList.sort((a, b) -> Double.compare(Double.parseDouble(b[2]), Double.parseDouble(a[2])));
            for (int i = 0; i < Math.min(count, processList.size()); i++) {
                String[] p = processList.get(i);
                outputArea.append(String.format("%-8s %-25s %-6s %-6s%n", p[0], p[1], p[2], p[3]));
            }

        } catch (Exception e) {
            outputArea.append("Ошибка при получении трудоёмких процессов: " + e.getMessage() + "\n");
        }
    }

    public void showProcessInfoByPid(JTextArea outputArea, int pid) {
        try {
            Process process = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-o", "pid,comm,pcpu,pmem").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            outputArea.append(String.format("%-8s %-25s %-6s %-6s%n", "PID", "COMMAND", "%CPU", "%MEM"));

            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length < 4) continue;
                outputArea.append(String.format("%-8s %-25s %-6s %-6s%n", parts[0], parts[1], parts[2], parts[3]));
            }
        } catch (Exception e) {
            outputArea.append("Ошибка при получении информации о процессе: " + e.getMessage() + "\n");
        }
    }

    public void showProcessManagementDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Управление процессом");
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel pidLabel = new JLabel("Введите PID:");
        JTextField pidField = new JTextField(10);
        JButton findButton = new JButton("Найти");

        inputPanel.add(pidLabel);
        inputPanel.add(pidField);
        inputPanel.add(findButton);

        String[] columnNames = {"PID", "COMMAND", "%CPU", "%MEM"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable processTable = new JTable(tableModel);
        processTable.setFillsViewportHeight(true);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        processTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        processTable.setGridColor(Color.LIGHT_GRAY);
        processTable.setShowGrid(true);

        JScrollPane scrollPane = new JScrollPane(processTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton killButton = new JButton("Удалить процесс");
        JButton resumeButton = new JButton("Возобновить процесс");
        JButton stopButton = new JButton("Приостановить");

        buttonPanel.add(killButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(stopButton);

        refreshProcessTable(tableModel);

        dialog.add(inputPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        findButton.addActionListener(e -> {
            try {
                int pid = Integer.parseInt(pidField.getText());
                selectProcessInTable(processTable, pid);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите корректный PID", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        processTable.getSelectionModel().addListSelectionListener(e -> {
            int row = processTable.getSelectedRow();
            if (row >= 0) {
                pidField.setText(processTable.getValueAt(row, 0).toString());
            }
        });

        ActionListener processAction = e -> {
            try {
                int pid = Integer.parseInt(pidField.getText());
                String signal = "";
                String message = "";

                if (e.getSource() == killButton) {
                    signal = "-9";
                    message = "Процесс успешно удален";
                } else if (e.getSource() == resumeButton) {
                    signal = "-18";
                    message = "Процесс успешно возобновлен";
                } else if (e.getSource() == stopButton) {
                    signal = "-19";
                    message = "Процесс успешно приостановлен";
                }

                Process process = new ProcessBuilder("kill", signal, String.valueOf(pid)).start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    JOptionPane.showMessageDialog(dialog, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
                    refreshProcessTable(tableModel);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Не удалось выполнить действие", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        };

        killButton.addActionListener(processAction);
        resumeButton.addActionListener(processAction);
        stopButton.addActionListener(processAction);

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void refreshProcessTable(DefaultTableModel model) {
        model.setRowCount(0);

        try {
            Process process = new ProcessBuilder("ps", "-eo", "pid,comm,%cpu,%mem", "--no-headers").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length == 4) {
                    model.addRow(parts);
                }
            }
            reader.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ошибка получения списка процессов: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectProcessInTable(JTable table, int pid) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 0).toString().equals(String.valueOf(pid))) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }
        JOptionPane.showMessageDialog(table, "Процесс с PID " + pid + " не найден",
                "Ошибка", JOptionPane.WARNING_MESSAGE);
    }
    }