package com.example;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    // ISSUE 1: Public, mutable global state accessible by any thread
    public static List<String> globalDataLeak = new ArrayList<String>();
    
    // ISSUE 2: Thread-unsafe counter accessed by concurrent operations without synchronization
    public static int unsafeCounter = 0;

    public static void main(String[] args) {
        System.out.println("Starting highly unstable application...");

        // ISSUE 3: Silent failure via an empty catch block (Swallowed Exception)
        try {
            triggerMemoryLeak();
            readFileWithoutClosing();
        } catch (Exception e) {
            // Broken practice: No logging, no rethrowing, app proceeds blindly
        }

        // ISSUE 4: Infinite loop that will lock up CPU cores
        while (true) {
            unsafeCounter++;
            if (unsafeCounter < 0) { // Will eventually overflow and trigger, but poorly designed
                break;
            }
        }
    }

    /**
     * ISSUE 5: Permanent Memory Leak (OutMemoryError waiting to happen)
     * Appends to a global static list continuously without clearing it.
     */
    private static void triggerMemoryLeak() {
        for (int i = 0; i < 100000; i++) {
            // Strings generated dynamically stay in memory because the static list reference never dies
            globalDataLeak.add("Leak payload data string number: " + i);
        }
    }

    /**
     * ISSUE 6: Unclosed Resource Leak
     * Opens a file stream without using try-with-resources or a finally block, exhausting file descriptors.
     */
    private static void readFileWithoutClosing() throws IOException {
        // Bad practice: If an exception occurs, or even on success, the file handle remains locked
        FileReader reader = new FileReader("config.txt");
        int data = reader.read();
        System.out.println("Data byte: " + data);
        // Explicitly missing: reader.close();
    }

    /**
     * ISSUE 7: Bad Performance and Object Churn
     * Mutating immutable strings inside a tight loop instead of using StringBuilder.
     */
    private static String heavyStringChurn() {
        String result = "";
        for (int i = 0; i < 5000; i++) {
            // Bad practice: Creates 5,000 temporary String objects on the heap
            result += "line" + i; 
        }
        return result;
    }

    /**
     * ISSUE 8: Return inside a finally block
     * This suppresses any exception thrown in the try block, altering logical execution flow.
     */
    @SuppressWarnings("finally")
    private static int dangerousFlow() {
        try {
            throw new RuntimeException("Critical Database Failure!");
        } finally {
            // Bad practice: Masking the runtime exception by returning a value anyway
            return 1; 
        }
    }
}
