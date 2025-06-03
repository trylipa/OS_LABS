import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThreadManager {

    public int getTotalThreads() {
        try {
            Process process = new ProcessBuilder("ps", "-eLf").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();
            return lines - 1;
        } catch (Exception e) {
            return -1;
        }
    }
    public void showThreadList(JTextArea outputArea, int count) {
        outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", "PID", "TID", "STAT", "%CPU", "%MEM", "COMMAND"));
        try {
            Process process = new ProcessBuilder("ps", "-eLf", "--no-headers").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int shown = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (count != -1 && shown >= count) break;
                String[] parts = line.trim().split("\\s+", 6);
                if (parts.length < 6) continue;
                outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                shown++;
            }
            reader.close();
        } catch (Exception e) {
            outputArea.append("Ошибка при получении списка потоков: " + e.getMessage() + "\n");
        }
    }
    public void showTopHeavyThreads(JTextArea outputArea, int count) {
        outputArea.append("ТОП " + count + " потоков по CPU:\n");
        outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", "PID", "TID", "%CPU", "%MEM", "STAT", "COMMAND"));
        try {
            Process process = new ProcessBuilder("ps", "-eLf", "--no-headers").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String[]> threadList = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 6);
                if (parts.length == 6) {
                    threadList.add(parts);
                }
            }
            reader.close();

            threadList.sort((a, b) -> Double.compare(Double.parseDouble(b[3]), Double.parseDouble(a[3])));

            for (int i = 0; i < Math.min(count, threadList.size()); i++) {
                String[] thread = threadList.get(i);
                outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", thread[0], thread[1], thread[3], thread[4], thread[2], thread[5]));
            }

        } catch (Exception e) {
            outputArea.append("Ошибка при получении трудоёмких потоков: " + e.getMessage() + "\n");
        }
    }
    public void showThreadStatus(int pid, int tid, JTextArea outputArea) {
        try {
            Process process = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-L", "-o", "pid,tid,stat,pcpu,pmem,comm").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", "PID", "TID", "STAT", "%CPU", "%MEM", "COMMAND"));

            String line;
            boolean found = false;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (line.contains(String.valueOf(tid))) {
                    String[] parts = line.trim().split("\\s+", 6);
                    if (parts.length < 6) continue;
                    outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                    found = true;
                    break;
                }
            }

            if (!found) {
                outputArea.append("Поток с TID " + tid + " не найден.\n");
            }

            reader.close();
        } catch (Exception e) {
            outputArea.append("Ошибка при получении статуса потока: " + e.getMessage() + "\n");
        }
    }
    public void showThreadLoad(int pid, int tid, JTextArea outputArea) {
        try {
            Process process = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-L", "-o", "tid,pcpu,pmem,comm").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            outputArea.append(String.format("%-8s %-6s %-6s %-25s%n", "TID", "%CPU", "%MEM", "COMMAND"));

            String line;
            boolean found = false;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length < 4) continue;

                if (Integer.parseInt(parts[0]) == tid) {
                    outputArea.append(String.format("%-8s %-6s %-6s %-25s%n", parts[0], parts[1], parts[2], parts[3]));
                    found = true;
                    break;
                }
            }

            // Если поток не найден, выводим сообщение
            if (!found) {
                outputArea.append("Поток с TID " + tid + " не найден.\n");
            }

            reader.close();
        } catch (Exception e) {
            outputArea.append("Ошибка при получении нагрузки потока: " + e.getMessage() + "\n");
        }
    }
    public void showThreadListSimple(JTextArea outputArea, int pid) {
        try {
            Process process = new ProcessBuilder("ps", "-p", String.valueOf(pid), "-L", "-o", "pid,tid,stat,pcpu,pmem,comm").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", "PID", "TID", "STAT", "%CPU", "%MEM", "COMMAND"));

            String line;
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 6);
                if (parts.length < 6) continue;

                if (parts[5].contains("chrome")) {
                    outputArea.append(String.format("%-8s %-8s %-6s %-6s %-6s %-25s%n", parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                }
            }

            reader.close();
        } catch (Exception e) {
            outputArea.append("Ошибка при получении списка потоков: " + e.getMessage() + "\n");
        }
    }
}