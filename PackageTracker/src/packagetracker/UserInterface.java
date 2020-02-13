/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packagetracker;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author markulai
 */
public class UserInterface {

    public void start(Scanner reader) throws SQLException {
        while (true) {
            System.out.print("Valitse toiminto (1-9) / lopeta: ");
            String command = reader.nextLine();

            if (command.equals("1")) { //creating the db
                createDatabase();
            } else if (command.equals("lopeta")) { // ending application
                System.out.println("Heippa");
                break;
            } else if (command.equals("2")) { //creating a new location
                System.out.print("Anna paikan nimi: ");
                String what = reader.nextLine();
                addNameOfThePlace(what);

            } else if (command.equals("3")) { //Creating a new customer
                System.out.print("Anna asiakkaan nimi? ");
                String what = reader.nextLine();
                addNameOfTheCustomer(what);

            } else if (command.equals("4")) { // adding a package to db with customer name and tracking code. Customer has to exist in db.
                System.out.print("Anna paketin seurantakoodi ");
                String code = reader.nextLine();
                System.out.print("Anna asiakkaan nimi: ");
                String name = reader.nextLine();
                addNewPackageToDb(code, name);

            } else if (command.equals("5")) { // adding a new event on db. Tracking code, location and description has been given. Location has to exit in db.
                System.out.print("Anna paketin seurantakoodi ");
                String code = reader.nextLine();
                System.out.print("Anna tapahtuman paikka: ");
                String location = reader.nextLine();
                System.out.print("Anna tapahtuman kuvaus: ");
                String description = reader.nextLine();
                addNewEventToDb(code, location, description);

            } else if (command.equals("6")) { //Get all events with tracking code
                System.out.print("Anna paketin seurantakoodi: ");
                String code = reader.nextLine();
                getAllEventsWithTrackingCode(code);

            } else if (command.equals("7")) { //Get all packages of a given customer with number of events. (tracking code + no. events
                System.out.print("Anna asiakkaan nimi: ");
                String name = reader.nextLine();
                getAllPackagesOfCustomer(name);

            } else if (command.equals("8")) { //Get no. events from a location at given day
                System.out.print("Anna paikan nimi: ");
                String name = reader.nextLine();
                System.out.print("Anna päivämäärä: ");
                String date = reader.nextLine();
                getNumberOfEventsFromLocationAtGivenDay(name, date);

            } else if (command.equals("9")) { //Do performance test

                doPerformanceTest();

            } else {
                System.out.println("Komento ei tuettu");
            }

        }

    }

    public static void createDatabase() throws SQLException {
        Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
        Statement s = db.createStatement();
        s.execute("CREATE TABLE Locations (id INTEGER PRIMARY KEY, name TEXT)");
        s.execute("CREATE TABLE Customers (id INTEGER PRIMARY KEY, name TEXT)");
        s.execute("CREATE TABLE Packages (id INTEGER PRIMARY KEY, customer_id INTEGER, scan_code TEXT)");
        s.execute("CREATE TABLE Events (id INTEGER PRIMARY KEY, description TEXT, created_at TEXT, location_id INTEGER, package_id INTEGER)");

        System.out.println("Tietokanta luotu!");
    }

    public static void addNameOfThePlace(String name) throws SQLException {

        if (doesValueExistAlreadyFromLocations(name)) {
            System.out.println("Paikka " + name + " löytyy jo!!");
        } else {
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = db.prepareStatement("INSERT INTO Locations (name) VALUES (?)");
            p.setString(1, name);
            p.executeUpdate();
            System.out.println("Paikka " + name + " lisätty");
        }

    }

    public static void addNameOfTheCustomer(String name) throws SQLException {
        if (doesValueExistAlreadyFromCustomers(name)) {
            System.out.println("Asiakas " + name + " löytyy jo!!");
        } else {
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = db.prepareStatement("INSERT INTO Customers (name) VALUES (?)");
            p.setString(1, name);
            p.executeUpdate();
            System.out.println("Asiakas " + name + " lisätty");
        }

    }

    public static void addNewPackageToDb(String code, String name) throws SQLException {
        if (!doesValueExistAlreadyFromCustomers(name)) {
            System.out.println("Asiakasta " + name + " ei löydy!");
        } else if (doesCodeAlreadyExistFromPackages(code)) {
            System.out.println("Pakettikoodilla " + code + " löytyy jo paketti!");
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
        if (!doesValueExistAlreadyFromLocations(location)) {
            System.out.println("Paikkaa " + location + " ei löydy!");
        } else if (!doesCodeAlreadyExistFromPackages(code)) {
            System.out.println("Pakettikoodilla " + code + " ei löydy pakettia!");
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
        if (!doesCodeAlreadyExistFromPackages(code)) {
            System.out.println("Seurantakoodia " + code + " ei loytynyt!");
        } else {
            System.out.println("Koodilla " + code + " on seuraavia tapahtumia");
            Connection db = DriverManager.getConnection("jdbc:sqlite:packagetracker.db");
            PreparedStatement p = db.prepareStatement("SELECT Events.created_at, Locations.name, Events.description FROM Events, Locations, Packages WHERE Events.package_id = Packages.id AND Locations.id = Events.location_id AND Packages.scan_code = ? ");
            p.setString(1,code);
            ResultSet r = p.executeQuery();
            
            while (r.next()) {
                System.out.println(r.getString("created_at") + ", " + r.getString("name") + ", " + r.getString("description"));
            }
             db.close();
        }

    }

    public static void getAllPackagesOfCustomer(String name) {
        System.out.println("Asiakkaan " + name + " paketit:");
    }

    public static void doPerformanceTest() {
        System.out.println("lets to the performance test...");
    }

    public static void getNumberOfEventsFromLocationAtGivenDay(String name, String date) {
        System.out.println("Paikalla " + name + " löytyi x määrä tapahtumia");
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
