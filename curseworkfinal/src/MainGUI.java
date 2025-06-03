import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    private JTextArea outputArea;
    private JButton showProcessListButton, showProcessInfoButton, manageProcessButton, showThreadInfoButton, exitButton;
    private ProcessManager processManager;
    private ThreadManager threadManager;

    private JPanel pidPanel;
    private JTextField pidField;
    private JButton pidConfirmButton;

    private JPanel tidPanel;
    private JTextField tidField;
    private JButton tidConfirmButton;

    private int currentProcessListMode = -1;
    private int currentPid = -1;

    public MainGUI() {
        processManager = new ProcessManager();
        threadManager = new ThreadManager();

        setTitle("Process Manager");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 5));

        showProcessListButton = new JButton("Список процессов");
        showProcessInfoButton = new JButton("Информация о процессе");
        manageProcessButton = new JButton("Управление процессом");
        showThreadInfoButton = new JButton("Информация о потоке");
        exitButton = new JButton("Выйти");

        buttonPanel.add(showProcessListButton);
        buttonPanel.add(showProcessInfoButton);
        buttonPanel.add(manageProcessButton);
        buttonPanel.add(showThreadInfoButton);
        buttonPanel.add(exitButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        pidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pidPanel.add(new JLabel("Введите PID:"));
        pidField = new JTextField(10);
        pidPanel.add(pidField);
        pidConfirmButton = new JButton("Найти");
        pidPanel.add(pidConfirmButton);
        pidPanel.setVisible(false);

        tidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tidPanel.add(new JLabel("Введите TID:"));
        tidField = new JTextField(10);
        tidPanel.add(tidField);
        tidConfirmButton = new JButton("Показать поток");
        tidPanel.add(tidConfirmButton);
        tidPanel.setVisible(false);

        JPanel inputPanels = new JPanel(new GridLayout(2, 1));
        inputPanels.add(pidPanel);
        inputPanels.add(tidPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanels, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(buttonPanel, BorderLayout.WEST);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        showProcessListButton.addActionListener(e -> {
            String[] options = {"Показать все процессы", "Показать самые трудоёмкие"};
            int choice = JOptionPane.showOptionDialog(this, "Выберите режим отображения процессов:", "Список процессов",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == JOptionPane.CLOSED_OPTION) return;

            pidPanel.setVisible(false);
            tidPanel.setVisible(false);
            outputArea.setText("");

            if (choice == 0) {
                processManager.showProcessList(outputArea, -1);
                currentProcessListMode = 0;
            } else {
                processManager.showTopHeavyProcesses(outputArea, 10);
                currentProcessListMode = 1;
            }
        });

        showProcessInfoButton.addActionListener(e -> {
            String[] options = {"Показать все процессы", "Показать самые трудоёмкие"};
            int choice = JOptionPane.showOptionDialog(this, "Выберите список процессов:", "Информация о процессе",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == JOptionPane.CLOSED_OPTION) {
                pidPanel.setVisible(false);
                tidPanel.setVisible(false);
                outputArea.setText("");
                currentProcessListMode = -1;
                return;
            }

            outputArea.setText("");
            if (choice == 0) {
                processManager.showProcessList(outputArea, -1);
            } else {
                processManager.showTopHeavyProcesses(outputArea, 10);
            }

            currentProcessListMode = choice;
            pidPanel.setVisible(true);
            tidPanel.setVisible(false);
            pidField.setText("");
            pidField.requestFocus();
        });

        pidConfirmButton.addActionListener(e -> {
            String pidText = pidField.getText().trim();
            if (pidText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите PID.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int pid;
            try {
                pid = Integer.parseInt(pidText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "PID должен быть числом.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (currentProcessListMode == 0 || currentProcessListMode == 1) {
                String[] infoOptions = {"CPU и память", "Список потоков"};
                String infoChoice = (String) JOptionPane.showInputDialog(this, "Что показать для процесса?",
                        "Выбор информации", JOptionPane.PLAIN_MESSAGE, null, infoOptions, infoOptions[0]);

                if (infoChoice == null) return;

                outputArea.setText("");
                switch (infoChoice) {
                    case "CPU и память":
                        processManager.showProcessInfoByPid(outputArea, pid);
                        tidPanel.setVisible(false);
                        break;
                    case "Список потоков":
                        threadManager.showThreadListSimple(outputArea, pid);
                        tidPanel.setVisible(false);
                        break;
                }
            } else if (currentProcessListMode == 2) {
                currentPid = pid;
                outputArea.setText("");
                threadManager.showThreadListSimple(outputArea, pid);

                pidPanel.setVisible(false);
                tidPanel.setVisible(true);
                tidField.setText("");
                tidField.requestFocus();

                currentProcessListMode = 3;
            }
        });

        pidField.addActionListener(e -> pidConfirmButton.doClick());

        tidConfirmButton.addActionListener(e -> {
            if (currentProcessListMode != 3) return;

            String tidText = tidField.getText().trim();
            if (tidText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите TID.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int tid;
            try {
                tid = Integer.parseInt(tidText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "TID должен быть числом.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] options = {"Статус потока", "Нагрузка на поток"};
            int choice = JOptionPane.showOptionDialog(this, "Выберите действие:", "Информация о потоке",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                threadManager.showThreadStatus(currentPid, tid, outputArea);
            } else if (choice == 1) {
                threadManager.showThreadLoad(currentPid, tid, outputArea);
            }
        });

        tidField.addActionListener(e -> tidConfirmButton.doClick());

        manageProcessButton.addActionListener(e -> {
            new ProcessManager().showProcessManagementDialog();
        });

        showThreadInfoButton.addActionListener(e -> {
            String[] options = {"Показать все процессы", "Показать самые трудоёмкие"};
            int choice = JOptionPane.showOptionDialog(this, "Выберите список процессов:", "Информация о потоке",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == JOptionPane.CLOSED_OPTION) {
                pidPanel.setVisible(false);
                tidPanel.setVisible(false);
                outputArea.setText("");
                return;
            }

            outputArea.setText("");
            if (choice == 0) {
                processManager.showProcessList(outputArea, -1);
            } else {
                processManager.showTopHeavyProcesses(outputArea, 10);
            }

            pidPanel.setVisible(true);
            tidPanel.setVisible(false);
            pidField.setText("");
            pidField.requestFocus();

            currentProcessListMode = 2;
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите выйти?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }
}