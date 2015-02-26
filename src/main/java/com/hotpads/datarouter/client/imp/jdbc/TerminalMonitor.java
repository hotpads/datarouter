package com.hotpads.datarouter.client.imp.jdbc;

/*

Database Programming with JDBC and Java, Second Edition
By George Reese
ISBN: 1-56592-616-1

Publisher: O'Reilly

*/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.hotpads.datarouter.util.core.ExceptionTool;

/**
 * Examples 4.4 through 4.6.
 */
public class TerminalMonitor {
  static Connection connection = null;

  static BufferedReader input;

  static public void main(String args[]) {
    DriverPropertyInfo[] required;
    StringBuffer buffer = new StringBuffer();
    Properties props = new Properties();
    boolean connected = false;
    Driver driver;
    String url;
    int line = 1; // Mark current input line

    if (args.length < 1) {
      System.out.println("Syntax: <java -Djdbc.drivers=DRIVER_NAME "
          + "TerminalMonitor JDBC_URL>");
      return;
    }
    url = args[0];
    // We have to get a reference to the driver so we can
    // find out what values to prompt the user for in order
    // to make a connection.
    try {
      driver = DriverManager.getDriver(url);
    } catch (SQLException e) {
      System.err.println(ExceptionTool.getStackTraceAsString(e));
      System.err.println("Unable to find a driver for the specified "
          + "URL.");
      System.err.println("Make sure you passed the jdbc.drivers "
          + "property on the command line to specify "
          + "the driver to be used.");
      return;
    }
    try {
      required = driver.getPropertyInfo(url, props);
    } catch (SQLException e) {
      System.err.println(ExceptionTool.getStackTraceAsString(e));
      System.err.println("Unable to get driver property information.");
      return;
    }
    input = new BufferedReader(new InputStreamReader(System.in));
    // some drivers do not implement this properly
    // if that is the case, prompt for user name and password
    try {
      if (required.length < 1) {
        props.put("user", prompt("user: "));
        props.put("password", prompt("password: "));
      } else {
        // for each required attribute in the driver property info
        // prompt the user for the value
        for (int i = 0; i < required.length; i++) {
          if (!required[i].required) {
            continue;
          }
          props
              .put(required[i].name, prompt(required[i].name
                  + ": "));
        }
      }
    } catch (IOException e) {
      System.err.println(ExceptionTool.getStackTraceAsString(e));
      System.err.println("Unable to read property info.");
      return;
    }
    // Make the connection.
    try {
      connection = DriverManager.getConnection(url, props);
    } catch (SQLException e) {
      System.err.println(ExceptionTool.getStackTraceAsString(e));
      System.err.println("Unable to connect to the database.");
    }
    connected = true;
    System.out.println("Connected to " + url);
    // Enter into a user input loop
    while (connected) {
      String tmp, cmd;

      // Print a prompt
      if (line == 1) {
        System.out.print("TM > ");
      } else {
        System.out.print(line + " -> ");
      }
      System.out.flush();
      // Get the next line of input
      try {
        tmp = input.readLine();
      } catch (java.io.IOException e) {
        System.err.println(ExceptionTool.getStackTraceAsString(e));
        return;
      }
      // Get rid of extra space in the command
      cmd = tmp.trim();
      // The user wants to commit pending transactions
      if (cmd.equals("commit")) {
        try {
          connection.commit();
          System.out.println("Commit successful.");
        } catch (SQLException e) {
          System.out.println("Error in commit: " + e.getMessage());
        }
        buffer = new StringBuffer();
        line = 1;
      }
      // The user wants to execute the current buffer
      else if (cmd.equals("go")) {
        if (!buffer.equals("")) {
          try {
            executeStatement(buffer);
          } catch (SQLException e) {
            System.out.println(e.getMessage());
          }
        }
        buffer = new StringBuffer();
        line = 1;
        continue;
      }
      // The user wants to quit
      else if (cmd.equals("quit")) {
        connected = false;
        continue;
      }
      // The user wants to clear the current buffer
      else if (cmd.equals("reset")) {
        buffer = new StringBuffer();
        line = 1;
        continue;
      }
      // The user wants to abort a pending transaction
      else if (cmd.equals("rollback")) {
        try {
          connection.rollback();
          System.out.println("Rollback successful.");
        } catch (SQLException e) {
          System.out.println("An error occurred during rollback: "
              + e.getMessage());
        }
        buffer = new StringBuffer();
        line = 1;
      }
      // The user wants version info
      else if (cmd.startsWith("show")) {
        DatabaseMetaData meta;

        try {
          meta = connection.getMetaData();
          cmd = cmd.substring(5, cmd.length()).trim();
          if (cmd.equals("version")) {
            showVersion(meta);
          } else {
            System.out.println("show version"); // Bad arg
          }
        } catch (SQLException e) {
          System.out.println("Failed to load meta data: "
              + e.getMessage());
        }
        buffer = new StringBuffer();
        line = 1;
      }
      // The input that is not a keyword should appended be to the buffer
      else {
        buffer.append(" " + tmp);
        line++;
        continue;
      }
    }
    try {
      connection.close();
    } catch (SQLException e) {
      System.out.println("Error closing connection: " + e.getMessage());
    }
    System.out.println("Connection closed.");
  }

  static public void executeStatement(StringBuffer buff) throws SQLException {
    String sql = buff.toString();
    Statement statement = null;

    try {
      statement = connection.createStatement();
      if (statement.execute(sql)) { // true means the SQL was a SELECT
        processResults(statement.getResultSet());
      } else { // no result sets, see how many rows were affected
        int num;

        switch (num = statement.getUpdateCount()) {
        case 0:
          System.out.println("No rows affected.");
          break;

        case 1:
          System.out.println(num + " row affected.");
          break;

        default:
          System.out.println(num + " rows affected.");
        }
      }
    } catch (SQLException e) {
      throw e;
    } finally { // close out the statement
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
        }
      }
    }
  }

  static public String prompt(String prop) throws IOException {
    String tmp = "";

    while (tmp.length() < 1) {
      System.out.print(prop);
      tmp = input.readLine().trim();
    }
    return tmp;
  }

  static public void processResults(ResultSet results) throws SQLException {
    try {
      ResultSetMetaData meta = results.getMetaData();
      StringBuffer bar = new StringBuffer();
      StringBuffer buffer = new StringBuffer();
      int cols = meta.getColumnCount();
      int row_count = 0;
      int i, width = 0;

      // Prepare headers for each of the columns
      // The display should look like:
      //  --------------------------------------
      //  | Column One | Column Two |
      //  --------------------------------------
      //  | Row 1 Value | Row 1 Value |
      //  --------------------------------------

      // create the bar that is as long as the total of all columns
      for (i = 1; i <= cols; i++) {
        width += meta.getColumnDisplaySize(i);
      }
      width += 1 + cols;
      for (i = 0; i < width; i++) {
        bar.append('-');
      }
      bar.append('\n');
      buffer.append(bar.toString() + "|");
      // After the first bar goes the column labels
      for (i = 1; i <= cols; i++) {
        StringBuffer filler = new StringBuffer();
        String label = meta.getColumnLabel(i);
        int size = meta.getColumnDisplaySize(i);
        int x;

        // If the label is longer than the column is wide,
        // then we truncate the column label
        if (label.length() > size) {
          label = label.substring(0, size);
        }
        // If the label is shorter than the column, pad it with spaces
        if (label.length() < size) {
          int j;

          x = (size - label.length()) / 2;
          for (j = 0; j < x; j++) {
            filler.append(' ');
          }
          label = filler + label + filler;
          if (label.length() > size) {
            label = label.substring(0, size);
          } else {
            while (label.length() < size) {
              label += " ";
            }
          }
        }
        // Add the column header to the buffer
        buffer.append(label + "|");
      }
      // Add the lower bar
      buffer.append("\n" + bar.toString());
      // Format each row in the result set and add it on
      while (results.next()) {
        row_count++;

        buffer.append('|');
        // Format each column of the row
        for (i = 1; i <= cols; i++) {
          StringBuffer filler = new StringBuffer();
          Object value = results.getObject(i);
          int size = meta.getColumnDisplaySize(i);
          String str;

          if (results.wasNull()) {
            str = "NULL";
          } else {
            str = value.toString();
          }
          if (str.length() > size) {
            str = str.substring(0, size);
          }
          if (str.length() < size) {
            int j, x;

            x = (size - str.length()) / 2;
            for (j = 0; j < x; j++) {
              filler.append(' ');
            }
            str = filler + str + filler;
            if (str.length() > size) {
              str = str.substring(0, size);
            } else {
              while (str.length() < size) {
                str += " ";
              }
            }
          }
          buffer.append(str + "|");
        }
        buffer.append("\n");
      }
      // Stick a row count up at the top
      if (row_count == 0) {
        buffer = new StringBuffer("No rows selected.\n");
      } else if (row_count == 1) {
        buffer = new StringBuffer("1 row selected.\n"
            + buffer.toString() + bar.toString());
      } else {
        buffer = new StringBuffer(row_count + " rows selected.\n"
            + buffer.toString() + bar.toString());
      }
      System.out.print(buffer.toString());
      System.out.flush();
    } catch (SQLException e) {
      throw e;
    } finally {
      try {
        results.close();
      } catch (SQLException e) {
      }
    }
  }

  static public void showVersion(DatabaseMetaData meta) {
    try {
      System.out.println("TerminalMonitor v2.0");
      System.out.println("DBMS: " + meta.getDatabaseProductName() + " "
          + meta.getDatabaseProductVersion());
      System.out.println("JDBC Driver: " + meta.getDriverName() + " "
          + meta.getDriverVersion());
    } catch (SQLException e) {
      System.out.println("Failed to get version info: " + e.getMessage());
    }
  }
}
