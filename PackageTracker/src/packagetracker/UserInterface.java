/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packagetracker;

import java.io.File;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author markulai
 */
public class UserInterface {

    public void start(Scanner reader) throws SQLException {
        while (true) {
            System.out.print("Valitse toiminto (1-9) / lopeta / komennot: ");
            String command = reader.nextLine();

            if (command.equals("1")) { //creating the db
                createDatabase();
            } else if (command.equals("lopeta")) { // ending application
                System.out.println("Heippa");
                break;
            } else if (command.equals("komennot")) { // ending application
                System.out.println("1 - Luo kanta (ei onnistu jos kanta on jo luotu");
                System.out.println("2 - Lisää uusi paikka tietokantaan");
                System.out.println("3 - Lisää uusi asiakas tietokantaan");
                System.out.println("4 - Lisää uusi paketti tietokantaan");
                System.out.println("5 - Lisää uusi tapahtuma tietokantaan");
                System.out.println("6 - Hae kaikki tapahtumat pakettiin liittyen");
                System.out.println("7 - Hae kaikki asiakkaan paketit ja niihin liittyvien tapahtumien määrä");
                System.out.println("8 - Hae annetusta paikasta tapahtumien määrä tiettynä päivänä");
                System.out.println("9 - Suorita tietokannan tehokkuustesti");

            } else if (command.equals("2")) { //creating a new location
                System.out.print("Anna paikan nimi: ");
                String what = reader.nextLine();
                if (what.isEmpty()) {
                    System.out.println("VIRHE syötteessä");
                } else {
                    addNameOfThePlace(what);
                }

            } else if (command.equals("3")) { //Creating a new customer
                System.out.print("Anna asiakkaan nimi? ");
                String what = reader.nextLine();
                if (what.isEmpty()) {
                    System.out.println("VIRHE syötteessä");
                } else {
                    addNameOfTheCustomer(what);
                }

            } else if (command.equals("4")) { // adding a package to db with customer name and tracking code. Customer has to exist in db.
                System.out.print("Anna paketin seurantakoodi ");
                String code = reader.nextLine();
                System.out.print("Anna asiakkaan nimi: ");
                String name = reader.nextLine();
                if (code.isEmpty() || name.isEmpty()) {
                    System.out.println("VIRHE syötteessä!");
                } else {
                    addNewPackageToDb(code, name);
                }

            } else if (command.equals("5")) { // adding a new event on db. Tracking code, location and description has been given. Location has to exit in db.
                System.out.print("Anna paketin seurantakoodi ");
                String code = reader.nextLine();
                System.out.print("Anna tapahtuman paikka: ");
                String location = reader.nextLine();
                System.out.print("Anna tapahtuman kuvaus: ");
                String description = reader.nextLine();
                if (code.isEmpty() || location.isEmpty()) {
                    System.out.println("VIRHE syötteessä");
                } else {
                    addNewEventToDb(code, location, description);
                }

            } else if (command.equals("6")) { //Get all events with tracking code
                System.out.print("Anna paketin seurantakoodi: ");
                String code = reader.nextLine();
                if (code.isEmpty()) {
                    System.out.println("VIRHE syötteessä");
                } else {
                    getAllEventsWithTrackingCode(code);
                }

            } else if (command.equals("7")) { //Get all packages of a given customer with number of events. (tracking code + no. events
                System.out.print("Anna asiakkaan nimi: ");
                String name = reader.nextLine();
                if (name.isEmpty()) {
                    System.out.println("VIRHE syötteessä!!");
                } else {
                    getAllPackagesOfCustomer(name);
                }

            } else if (command.equals("8")) { //Get no. events from a location at given day
                System.out.print("Anna paikan nimi: ");
                String name = reader.nextLine();
                System.out.print("Anna päivämäärä: ");
                String date = reader.nextLine();
                if (name.isEmpty() || date.isEmpty()) {
                    System.out.println("VIRHE: puuttuvaa tietoa!");
                } else {
                    getNumberOfEventsFromLocationAtGivenDay(name, date);
                }

            } else if (command.equals("9")) { //Do performance test

                doPerformanceTest();

            } else {
                System.out.println("Komento ei tuettu");
            }

        }

    }

    public static void createDatabase() throws SQLException {
        File file = new File("packagetracker.db");

        if (file.exists()) {
            System.out.println("Kanta löytyy jo!");

        } else {
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            Statement s = db.createStatement();
            s.execute("CREATE TABLE Locations (id INTEGER PRIMARY KEY, name TEXT UNIQUE)");
            s.execute("CREATE TABLE Customers (id INTEGER PRIMARY KEY, name TEXT UNIQUE)");
            s.execute("CREATE TABLE Packages (id INTEGER PRIMARY KEY, customer_id INTEGER REFERENCES Customers, scan_code TEXT UNIQUE)");
            s.execute("CREATE TABLE Events (id INTEGER PRIMARY KEY, description TEXT, created_at TEXT, location_id INTEGER REFERENCES Locations, package_id INTEGER REFERENCES Packages)");
            s.execute("CREATE INDEX idx_package_id ON Events (package_id)");
            System.out.println("Tietokanta luotu!");
        }

    }

    public static void addNameOfThePlace(String name) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else if (doesValueExistAlreadyFromLocations(name)) {
            System.out.println("VIRHE: Paikka " + name + " löytyy jo!!");
        } else {
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");

            PreparedStatement p = db.prepareStatement("INSERT INTO Locations (name) VALUES (?)");
            p.setString(1, name);
            p.executeUpdate();
            System.out.println("Paikka " + name + " lisätty");
        }

    }

    public static void addNameOfTheCustomer(String name) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else if (doesValueExistAlreadyFromCustomers(name)) {
            System.out.println("VIRHE: Asiakas " + name + " löytyy jo!!");
        } else {
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = db.prepareStatement("INSERT INTO Customers (name) VALUES (?)");
            p.setString(1, name);
            p.executeUpdate();
            System.out.println("Asiakas " + name + " lisätty");
        }

    }

    public static void addNewPackageToDb(String code, String name) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else if (!doesValueExistAlreadyFromCustomers(name)) {
            System.out.println("VIRHE: Asiakasta " + name + " ei löydy!");
        } else if (doesCodeAlreadyExistFromPackages(code)) {
            System.out.println("VIRHE: Pakettikoodilla " + code + " löytyy jo paketti!");
        } else {

            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");

            //Get customer ID of customer name
            int customer_id = 0;
            PreparedStatement a = db.prepareStatement("SELECT id FROM Customers WHERE name=?");
            a.setString(1, name);
            ResultSet ar = a.executeQuery();
            if (ar.next()) {
                customer_id = ar.getInt("id");
            }

            //insert tracking code with user 
            PreparedStatement p = db.prepareStatement("INSERT INTO Packages(scan_code,customer_id) VALUES (?,?)");

            p.setString(1, code);
            p.setInt(2, customer_id);

            p.executeUpdate();
            db.close();
            System.out.println("Paketti " + code + ", asiakkaalle: " + name + ", asiakas ID:lle: " + customer_id + " lisatty!");

        }

    }

    public static void addNewEventToDb(String code, String location, String description) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else if (!doesValueExistAlreadyFromLocations(location)) {
            System.out.println("VIRHE: Paikkaa " + location + " ei löydy!");
        } else if (!doesCodeAlreadyExistFromPackages(code)) {
            System.out.println("VIRHE: Pakettikoodilla " + code + " ei löydy pakettia!");
        } else {

            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");

            //Get location_id ID location
            int location_id = 0;
            PreparedStatement a = db.prepareStatement("SELECT id FROM locations WHERE name=?");
            a.setString(1, location);
            ResultSet ar = a.executeQuery();

            if (ar.next()) {
                location_id = ar.getInt("id");
            }
            db.close();

            //Get Package_id from code
            Connection dba = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            int package_id = 0;
            PreparedStatement pa = dba.prepareStatement("SELECT id FROM Packages WHERE scan_code=?");
            pa.setString(1, code);
            ResultSet par = pa.executeQuery();
            if (par.next()) {
                package_id = par.getInt("id");
            }
            dba.close();

            //Getting the time stamp
            Calendar cal = Calendar.getInstance();
            cal.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String created_at = sdf.format(cal.getTime());

            //insert event with timestamp, package_id, location_id and description
            Connection dbb = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = dbb.prepareStatement("INSERT INTO Events (description,created_at,location_id,package_id) VALUES (?,?,?,?)");

            p.setString(1, description);
            p.setString(2, created_at);
            p.setInt(3, location_id);
            p.setInt(4, package_id);

            p.executeUpdate();
            System.out.println("Tapahtuma, " + description + " paikalle " + location + " ja seurantakoodilla: " + code + " ajalla: " + created_at + " lisätty!");

        }

    }

    public static void getAllEventsWithTrackingCode(String code) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else if (!doesCodeAlreadyExistFromPackages(code)) {
            System.out.println("VIRHE: Seurantakoodia " + code + " ei loytynyt!");
        } else {
            System.out.println("Koodilla " + code + " on seuraavia tapahtumia");
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = db.prepareStatement("SELECT Events.created_at, Locations.name, Events.description FROM Events, Locations, Packages WHERE Events.package_id = Packages.id AND Locations.id = Events.location_id AND Packages.scan_code = ? ");
            p.setString(1, code);
            ResultSet r = p.executeQuery();

            while (r.next()) {
                System.out.println(r.getString("created_at") + ", " + r.getString("name") + ", " + r.getString("description"));
            }
            db.close();
        }

    }

    public static void getAllPackagesOfCustomer(String name) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else if (!doesValueExistAlreadyFromCustomers(name)) {
            System.out.println("VIRHE: Asiakasta  " + name + " ei loytynyt!");
        } else {
            System.out.println("Asiakkaalla " + name + " on seuraavia paketteja");
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = db.prepareStatement("SELECT Packages.scan_code, COUNT(Events.id) FROM Customers, Packages, Events WHERE Events.package_id = Packages.id AND Packages.customer_id = Customers.id AND Customers.name = ? ");
            p.setString(1, name);
            ResultSet r = p.executeQuery();

            while (r.next()) {
                System.out.println(r.getString("scan_code") + ", " + r.getString("COUNT(Events.id)") + " tapahtumaa");
            }
            db.close();
        }

    }

    public static void doPerformanceTest() throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kanta pitää ensin luoda!");

        } else {
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            Statement s = db.createStatement();
            s.execute("BEGIN TRANSACTION");

            long aika1 = System.nanoTime();
            PreparedStatement p1 = db.prepareStatement("INSERT INTO Locations (name) VALUES (?)");
            for (int i = 1; i < 1001; i++) {
                p1.setString(1, "P" + i);
                p1.executeUpdate();
            }
            long aika2 = System.nanoTime();
            System.out.println("Aikaa kului paikkojen luomiseen: " + (aika2 - aika1) / 1e9 + " sekuntia");

            long aika3 = System.nanoTime();
            PreparedStatement p2 = db.prepareStatement("INSERT INTO Customers (name) VALUES (?)");
            for (int i = 1; i < 1001; i++) {
                p2.setString(1, "A" + i);
                p2.executeUpdate();
            }
            long aika4 = System.nanoTime();
            System.out.println("Aikaa kului asiakkaiden luomiseen: " + (aika4 - aika3) / 1e9 + " sekuntia");

            long aika5 = System.nanoTime();
            PreparedStatement p3 = db.prepareStatement("INSERT INTO Packages (customer_id, scan_code) VALUES (?,?)");

            for (int i = 1; i < 1001; i++) {
                int random = ThreadLocalRandom.current().nextInt(1000);
                p3.setInt(1, random);
                p3.setString(2, "PACKAGE" + i);
                p3.executeUpdate();
            }
            long aika6 = System.nanoTime();
            System.out.println("Aikaa kului pakettien luomiseen: " + (aika6 - aika5) / 1e9 + " sekuntia");

            long aika7 = System.nanoTime();
            PreparedStatement p = db.prepareStatement("INSERT INTO Events (description, created_at, location_id, package_id) VALUES (?,?,?,?)");
            for (int i = 1; i < 1000001; i++) { //Should be 1000001
                int random = ThreadLocalRandom.current().nextInt(1000);
                int random_package = ThreadLocalRandom.current().nextInt(1000);
                Calendar cal = Calendar.getInstance();
                cal.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String created_at = sdf.format(cal.getTime());

                p.setString(1, "Random Tapahtuma " + random);
                p.setString(2, created_at);
                p.setInt(3, random);
                p.setInt(4, random_package);
                p.executeUpdate();
            }
            long aika8 = System.nanoTime();
            System.out.println("Aikaa kului tapahtumien luomiseen: " + (aika8 - aika7) / 1e9 + " sekuntia");

            s.execute("COMMIT");

            //Thousand queries where number of packages of some customer are retrieved
            long aika9 = System.nanoTime();
            PreparedStatement p5 = db.prepareStatement("SELECT Packages.scan_code, COUNT(Events.id) FROM Customers, Packages, Events WHERE Events.package_id = Packages.id AND Packages.customer_id = Customers.id AND Customers.name = ? ");
            for (int i = 1; i < 1001; i++) {
                int random = ThreadLocalRandom.current().nextInt(1000);
                String name = "A" + random;
                p5.setString(1, name);
                p5.executeQuery();
            }
            long aika10 = System.nanoTime();
            System.out.println("Aikaa kului pakettien hakemiseen: " + (aika10 - aika9) / 1e9 + " sekuntia");

            //Thousand queries where events of some packages are retrieved
            long aika11 = System.nanoTime();

            PreparedStatement p6 = db.prepareStatement("SELECT Events.created_at, Locations.name, Events.description FROM Events, Locations, Packages WHERE Events.package_id = Packages.id AND Locations.id = Events.location_id AND Packages.scan_code = ? ");
            for (int i = 1; i < 1001; i++) {
                int random = ThreadLocalRandom.current().nextInt(1000);
                String code = "PACKAGE" + random;
                if (doesCodeAlreadyExistFromPackages(code)) {
                    p6.setString(1, code);
                    p6.executeQuery();
                    /*ResultSet r = p6.executeQuery();

                while (r.next()) {
                    System.out.println(r.getString("created_at") + ", " + r.getString("name") + ", " + r.getString("description"));
                } */
                } else {
                    i--;
                }

            }
            long aika12 = System.nanoTime();
            System.out.println("Aikaa kului tapahtumien hakemiseen: " + (aika12 - aika11) / 1e9 + " sekuntia");
        }

    }

    public static void getNumberOfEventsFromLocationAtGivenDay(String name, String date) throws SQLException {
        File file = new File("packagetracker.db");

        if (!file.exists()) {
            System.out.println("Kantaa ei löydy, luo se ensin!");
        } else {
            int count = 0;
            String[] dots = date.split("");
            for (int i = 0; i < date.length(); i++) {
                if (dots[i].matches(".*[.!?]")) {
                    count++;
                }
            }
            if (count == 2) {
                String[] arrOfStr = date.split("\\.");
                if (arrOfStr[1].length() == 1) {
                    arrOfStr[1] = "0" + arrOfStr[1];
                }

                if (arrOfStr[0].length() == 1) {
                    arrOfStr[0] = "0" + arrOfStr[0];
                }

                String modified_date = arrOfStr[0] + "." + arrOfStr[1] + "." + arrOfStr[2] + "%";

                if (!doesValueExistAlreadyFromLocations(name)) {
                    System.out.println("Paikkaa " + name + " ei loytynyt!");
                } else {
                    Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
                    PreparedStatement p = db.prepareStatement("SELECT COUNT(Events.id) FROM Events, Locations WHERE Events.location_id = Locations.id AND Locations.name = ? AND Events.created_at LIKE ? ");
                    p.setString(1, name);
                    p.setString(2, modified_date);
                    ResultSet r = p.executeQuery();

                    while (r.next()) {
                        System.out.println("Tapahtumien määrä: " + r.getInt("COUNT(Events.id)"));
                    }
                    db.close();
                }
            }
            else {
                System.out.println("Väärä päivämäärämuoto! Esim. 02.02.2020");
            }

        }

    }

    public static boolean doesValueExistAlreadyFromLocations(String value) throws SQLException {

        Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
        PreparedStatement p = db.prepareStatement("SELECT name FROM Locations WHERE name=?");
        p.setString(1, value);

        ResultSet r = p.executeQuery();
        db.close();
        if (r.next()) {

            return true;
        }

        return false;
    }

    public static boolean doesValueExistAlreadyFromCustomers(String name) throws SQLException {
        Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
        PreparedStatement p = db.prepareStatement("SELECT name FROM Customers WHERE name=?");
        p.setString(1, name);

        ResultSet r = p.executeQuery();
        db.close();
        if (r.next()) {

            return true;
        }

        return false;
    }

    public static boolean doesValueExistAlreadyFromPackages(int id) throws SQLException {
        Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
        PreparedStatement p = db.prepareStatement("SELECT id FROM Packages WHERE scan_code=?");
        p.setInt(1, id);

        ResultSet r = p.executeQuery();
        db.close();
        if (r.next()) {

            return true;
        }

        return false;
    }

    public static boolean doesCodeAlreadyExistFromPackages(String code) throws SQLException {
        Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
        PreparedStatement p = db.prepareStatement("SELECT id FROM Packages WHERE scan_code=?");
        p.setString(1, code);

        ResultSet r = p.executeQuery();
        db.close();
        if (r.next()) {

            return true;
        }

        return false;
    }

}
