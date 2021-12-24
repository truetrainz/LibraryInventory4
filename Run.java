import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import static org.junit.Assert.assertEquals;

public class Run {

    private static final String url = "jdbc:postgresql://localhost:5434/nickcliffel";
    private static final String password = "";
    private static final String username = "";
    private int total = 0;
    private Logger log;

    public Run() {
        try {
            Connection connection = DriverManager.getConnection(url);
            databaseSetup(connection);
            clearDatabase(connection);
            run(connection);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getBarcode() {
        BufferedImage image = captureImage();
        while (image == null) {
            image = captureImage();
        }
        System.out.println("IMAGE WAS CAPTURED");
        String code = getCode(image);
        while (code == null) {
            code = getCode(image);
        }
        return code;
    }

    public void run(Connection connection) {
        databaseSetup(connection);
        LocalTime time = LocalTime.now();
        log = Logger.getLogger("Log");
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler("/Users/nickcliffel/Documents/LogFile.log");
            log.addHandler(fileHandler);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        while (time.getHour() >= 8 && time.getHour() < 21) {
           String barcode = getBarcode();
            if (incomingOutgoing()) { // incoming
                if (isNew()) {
                    String name = getName();
                    if (checkName(name, connection)) {
                        String description = getDescription();
                        String vendorId = getVendorId();
                        String manufactorId = getManufactorId();
                        int amount = getAmount();
                        total += amount;
                        addNewInventory(amount, name, description, barcode, vendorId, manufactorId, connection);
                        log.info("new inventory added -> name: " + name + " amount: " + amount +
                                " description: " + description + " barcode: " + barcode);
                    } else {
                        System.out.println("That name is already in the system");
                    }
                } else {
                    String name = getName();
                    if (!checkName(name, connection)) {
                        int amount = getAmount();
                        total += amount;
                        addIncomingInventory(barcode, amount, name, connection);
                        log.info("added incoming inventory -> name: " + name + " amount: " + amount +
                                " barcode: " + barcode);
                    } else {
                        System.out.println("That name does not exist in the system");
                    }
                }
            } else { // outgoing
                removeInventoryIn(barcode, connection);
            }
            int checkingAmount = getAmountFromName("test", connection);
            log.info("new total -> " + checkingAmount);
            assertEquals(total, checkingAmount);
        }
    }

    private BufferedImage captureImage() {
        Webcam webcam = Webcam.getDefault();
        webcam.open();
        BufferedImage image = webcam.getImage();
        try {
            ImageIO.write(image, ImageUtils.FORMAT_JPG, new File("/Users/nickcliffel/Documents/selfie.jpg"));
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCode(BufferedImage image) {
        try{
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean incomingOutgoing() {
        System.out.println("Incoming or Outgoing? Input an I for incoming and an O for outgoing.");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        boolean result = false;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.trim().equalsIgnoreCase("I")) {
                    result = true;
                    done = true;
                } else if (line.trim().equalsIgnoreCase("O")) {
                    result = false;
                    done = true;
                } else {
                    System.out.println("That input was not an I or an O. Please enter an I or O.");
                }
            }
        }
        return result;
    }

    private boolean isNew() {
        System.out.println("Is this a new product? Input Y for yes and N for no.");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        boolean result = false;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.trim().equalsIgnoreCase("Y")) {
                    result = true;
                    done = true;
                } else if (line.trim().equalsIgnoreCase("N")) {
                    result = false;
                    done = true;
                } else {
                    System.out.println("That input was not a Y or N. Please enter a Y or N.");
                }
            }
        }
        return result;
    }

    private String getName() {
        System.out.println("What is the name of this product");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        String result = null;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (!line.trim().equals("")) {
                    result = line;
                    if (isCorrect(result)) {
                        done = true;
                    }
                } else {
                    System.out.println("Please input a string.");
                }
            }
        }
        return result;
    }

    private String getVendorId() {
        System.out.println("What is the vendor id?");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        String result = null;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (!line.trim().equals("")) {
                    result = line;
                    if (isCorrect(result)) {
                        done = true;
                    }
                } else {
                    System.out.println("Please input a String");
                }
            }
        }
        return result;
    }

    private String getManufactorId() {
        System.out.println("What is the manufactor id?");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        String result = null;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (!line.trim().equals("")) {
                    result = line;
                    if (isCorrect(result)) {
                        done = true;
                    }
                } else {
                    System.out.println("Please input a String");
                }
            }
        }
        return result;
    }

    private String getDescription() {
        System.out.println("Please input a description of the product.");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        String result = null;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (!line.trim().equals("")) {
                    result = line;
                    if (isCorrect(result)) {
                        done = true;
                    }
                } else {
                    System.out.println("Please input a string.");
                }
            }
        }
        return result;
    }

    private boolean isCorrect(String incoming) {
        System.out.println("Is this the correct input: " + incoming + "?");
        return getBoolValue();
    }

    private boolean isCorrect(int input) {
        System.out.println("Is this the correct input: " + input + "?");
        return getBoolValue();
    }

    private boolean getBoolValue() {
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        boolean result = false;
        while (!done) {
            String line = null;
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.trim().equalsIgnoreCase("Y")) {
                    result = true;
                    done = true;
                } else if (line.trim().equalsIgnoreCase("N")) {
                    result = false;
                    done = true;
                } else {
                    System.out.println("That input was not a Y or N. Please enter a Y or N.");
                }
            }
        }
        return result;
    }

    public void databaseSetup(Connection connection) {
        try {
            if (connection != null) {
                Statement statement = connection.createStatement();
                statement.addBatch("CREATE TABLE IF NOT EXISTS inventory_in (id bigint PRIMARY KEY, barcode varchar(180), amount int, name varchar(180));");
                statement.addBatch("CREATE TABLE IF NOT EXISTS inventory(id bigint PRIMARY KEY, amount int, description TEXT, name varchar(180), barcode varchar(180), vendor_id varchar(180), manufactor_id varchar(180));");
                statement.addBatch("CREATE SEQUENCE IF NOT EXISTS inventory_in_sequence INCREMENT BY 1 MINVALUE 0 MAXVALUE 9223372036854775806 START 1;");
                statement.addBatch("CREATE SEQUENCE IF NOT EXISTS inventory_sequence INCREMENT BY 1 MINVALUE 0 MAXVALUE 9223372036854775806 START 1;");
                statement.executeBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearDatabase(Connection connection) {
        String sql = "DELETE FROM inventory WHERE name = 'test';";
        String sql2 = "DELETE FROM inventory_in WHERE name = 'test'";
        try {
            Statement statement = connection.createStatement();
            statement.addBatch(sql);
            statement.addBatch(sql2);
            statement.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getAmount() {
        System.out.println("How many of the items are incoming?");
        Scanner scanner = new Scanner(System.in);
        boolean done = false;
        int result = -1;
        while (!done) {
            if (scanner.hasNextInt()) {
                int working = scanner.nextInt();
                if (working > 0) {
                    if (isCorrect(working)) {
                        result = working;
                        done = true;
                    } else {
                        System.out.println("How many of the items are incoming?");
                    }
                }
            }
        }
        return result;
    }

    private void addNewInventory(int amount, String name, String description, String barcode, String vendorId, String manufactorId, Connection connection) {
        String sql = "INSERT INTO inventory (id, amount, description, name, barcode, vendor_id, manufactor_id) VALUES ((SELECT nextval('inventory_sequence')), "
                + amount + ", '" + description + "', '" + name + "', '" + barcode + "', '" + vendorId + "', '" + manufactorId + "');";
        String sql2 = "INSERT INTO inventory_in (id, barcode, amount, name) VALUES ((SELECT nextval('inventory_in_sequence')), '" +
                barcode + "', " + amount + ", '" + name + "');";
        try {
            Statement statement = connection.createStatement();
            statement.addBatch(sql);
            statement.addBatch(sql2);
            statement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getAmountFromName(String name, Connection connection) {
        String sql = "SELECT amount FROM inventory WHERE name = '" + name + "';";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("amount");
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean checkName(String name, Connection connection) {
        String sql = "SELECT name FROM inventory WHERE name = '" + name + "';";
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            int value = 0;
            while (resultSet.next()) {
                value++;
            }
            if (value == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addIncomingInventory(String barcode, int amount, String name, Connection connection) {
        String sql = "INSERT INTO inventory_in (id, barcode, amount, name) VALUES ((SELECT nextval('inventory_in_sequence')), '"
                + barcode + "', " + amount + ", '" + name + "');";
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            int newAmount = getAmountFromName(name, connection) + amount;
            String updateAmount = "UPDATE inventory SET amount = " + newAmount + " WHERE name = '" + name + "';";
            Statement statement1 = connection.createStatement();
            statement1.executeUpdate(updateAmount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeInventoryIn(String barcode, Connection connection) {
        String sql = "SELECT id, amount, name FROM inventory_in WHERE barcode = '" + barcode + "';";
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            boolean found = false;
            int amount = -1;
            String name = null;
            int id = -1;
            while (resultSet.next() && !found) {
                id = resultSet.getInt("id");
                amount = resultSet.getInt("amount");
                name = resultSet.getString("name");
                found = true;
            }
            if (amount != -1 && name != null && id != -1) {
                int newAmount = getAmountFromName(name, connection) - amount;
                String sql2 = "UPDATE inventory SET amount = " + newAmount + " WHERE name = '" + name + "';";
                total -= amount;
                connection.createStatement().executeUpdate(sql2);
                String sql3 = "DELETE FROM inventory_in WHERE id = " + id + ";";
                connection.createStatement().executeUpdate(sql3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
