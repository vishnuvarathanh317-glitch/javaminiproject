package com.hostelmanagement;

/**
 * Standard JavaFX Non-Application Launcher.
 * Prevents "JavaFX runtime components are missing" error when launched from fat JAR or IDE.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
