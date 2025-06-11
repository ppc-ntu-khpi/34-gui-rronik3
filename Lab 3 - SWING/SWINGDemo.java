import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Alexander 'Taurus' Babich
 */
public class SWINGDemo {

    private final JEditorPane log;
    private final JButton show;
    private final JButton report; // Додана кнопка Report
    private final JComboBox<String> clients;

    public SWINGDemo() {
        log = new JEditorPane("text/html", "");
        log.setPreferredSize(new Dimension(250, 150));
        show = new JButton("Show");
        report = new JButton("Report"); // Ініціалізація кнопки Report
        clients = new JComboBox<>();
        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            clients.addItem(Bank.getCustomer(i).getLastName() + ", " + Bank.getCustomer(i).getFirstName());
        }
    }

    private void launchFrame() {
        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());
        JPanel cpane = new JPanel();
        cpane.setLayout(new GridLayout(1, 3)); // Змінено на 3 для додавання кнопки Report

        cpane.add(clients);
        cpane.add(show);
        cpane.add(report); // Додано кнопку Report
        frame.add(cpane, BorderLayout.NORTH);
        frame.add(log, BorderLayout.CENTER);

        show.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Customer current = Bank.getCustomer(clients.getSelectedIndex());
                StringBuilder custInfo = new StringBuilder("<br>&nbsp;<b><span style=\"font-size:2em;\">")
                        .append(current.getLastName()).append(", ").append(current.getFirstName())
                        .append("</span><br><hr>");
                for (int i = 0; i < current.getNumberOfAccounts(); i++) {
                    String accType = current.getAccount(i) instanceof CheckingAccount ? "Checking" : "Savings";
                    custInfo.append("&nbsp;<b>Acc Type: </b>").append(accType)
                            .append("<br>&nbsp;<b>Balance: <span style=\"color:red;\">$")
                            .append(current.getAccount(i).getBalance()).append("</span></b><br><hr>");
                }
                log.setText(custInfo.toString());
            }
        });

        report.addActionListener(new ActionListener() { // Додано обробник для кнопки Report
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder reportInfo = new StringBuilder("<html><body>");
                reportInfo.append("<h2>Customer Report</h2><hr>");
                for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                    Customer customer = Bank.getCustomer(i);
                    reportInfo.append("<b>").append(customer.getLastName()).append(", ")
                            .append(customer.getFirstName()).append("</b><br>");
                    for (int j = 0; j < customer.getNumberOfAccounts(); j++) {
                        String accType = customer.getAccount(j) instanceof CheckingAccount ? "Checking" : "Savings";
                        reportInfo.append("&nbsp;&nbsp;Acc Type: ").append(accType)
                                .append(", Balance: $").append(customer.getAccount(j).getBalance()).append("<br>");
                    }
                    reportInfo.append("<hr>");
                }
                reportInfo.append("</body></html>");
                log.setText(reportInfo.toString());
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static void loadDataFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Видалити зайві пробіли
                if (line.isEmpty()) {
                    continue; // Пропустити порожній рядок
                }

                // Якщо рядок містить лише число, це кількість клієнтів (ігноруємо)
                if (line.matches("\\d+")) {
                    continue;
                }

                // Обробка даних клієнта
                String[] parts = line.split("\\t");
                if (parts.length < 3) {
                    System.err.println("Invalid customer data format: " + line);
                    continue; // Пропустити рядок, якщо він має недостатньо даних
                }

                String firstName = parts[0].trim();
                String lastName = parts[1].trim();
                int numAccounts;
                try {
                    numAccounts = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number of accounts: " + parts[2]);
                    continue; // Пропустити клієнта, якщо кількість рахунків некоректна
                }

                Bank.addCustomer(firstName, lastName);
                Customer customer = Bank.getCustomer(Bank.getNumberOfCustomers() - 1);

                // Читання рахунків клієнта
                for (int i = 0; i < numAccounts; i++) {
                    line = reader.readLine();
                    if (line == null || line.trim().isEmpty()) {
                        System.err.println("Missing account data for customer: " + firstName + " " + lastName);
                        break; // Пропустити рахунок, якщо дані відсутні
                    }

                    String[] accountParts = line.split("\\t");
                    if (accountParts.length < 2) {
                        System.err.println("Invalid account data format: " + line);
                        continue; // Пропустити рахунок, якщо дані некоректні
                    }

                    String accType = accountParts[0].trim();
                    double balance;
                    try {
                        balance = Double.parseDouble(accountParts[1].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid balance format: " + accountParts[1]);
                        continue; // Пропустити рахунок, якщо баланс некоректний
                    }

                    if (accType.equalsIgnoreCase("C")) {
                        customer.addAccount(new CheckingAccount(balance));
                    } else if (accType.equalsIgnoreCase("S")) {
                        customer.addAccount(new SavingsAccount(balance, 3));
                    } else {
                        System.err.println("Unknown account type: " + accType);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        loadDataFromFile("f:\\34-gui-rronik3\\data/test.dat");
        SWINGDemo demo = new SWINGDemo();
        demo.launchFrame();
    }
}
