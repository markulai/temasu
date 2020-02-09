/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packagetracker;

import java.sql.*;
import java.util.Scanner;

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
                continue;
            } else if (command.equals("3")) { //Creating a new customer
                System.out.print("Anna asiakkaan nimi? ");
                String what = reader.nextLine();
                addNameOfTheCustomer(what);
                continue;
            } else if (command.equals("4")) { // adding a package to db with customer name and tracking code. Customer has to exist in db.
                System.out.print("Anna paketin seurantakoodi ");
                String code = reader.nextLine();
                System.out.print("Anna asiakkaan nimi: ");
                String name = reader.nextLine();
                addNewPackageToDb(code, name);
                continue;
            } else if (command.equals("5")) { // adding a new event on db. Tracking code, location and description has been given. Location has to exit in db.
                System.out.print("Anna paketin seurantakoodi ");
                String code = reader.nextLine();
                System.out.print("Anna tapahtuman paikka: ");
                String location = reader.nextLine();
                System.out.print("Anna tapahtuman kuvaus: ");
                String description = reader.nextLine();
                addNewEventToDb(code, location, description);
                continue;
            } else if (command.equals("6")) { //Get all events with tracking code
                System.out.print("Anna paketin seurantakoodi: ");
                String code = reader.nextLine();
                getAllEventsWithTrackingCode(code);
                continue;
            } else if (command.equals("7")) { //Get all packages of a given customer with number of events. (tracking code + no. events
                System.out.print("Anna asiakkaan nimi: ");
                String name = reader.nextLine();
                getAllPackagesOfCustomer(name);
                continue;
            } else if (command.equals("8")) { //Get no. events from a location at given day
                System.out.print("Anna paikan nimi: ");
                String name = reader.nextLine();
                System.out.print("Anna päivämäärä: ");
                String date = reader.nextLine();
                getNumberOfEventsFromLocationAtGivenDay(name, date);
                continue;
            } else if (command.equals("9")) { //Do performance test

                doPerformanceTest();
                continue;
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
        s.execute("CREATE TABLE Packages (id INTEGER PRIMARY KEY, customer_id INTEGER, scan_code INTEGER)");
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

    public static void addNameOfTheCustomer(String name) {
        System.out.println("Asiakas " + name + " lisätty");
    }

    public static void addNewPackageToDb(String code, String name) {
        System.out.println("Paketti " + name + " lisätty oheisella seurantakoodilla: " + code);
    }

    public static void addNewEventToDb(String code, String location, String description) {
        System.out.println("Tapahtuma, " + description + " paikalla " + location + " ja seurantakoodilla: " + code + " lisätty!");
    }

    public static void getAllEventsWithTrackingCode(String code) {
        System.out.println("Koodilla " + code + " on seuraavia tapahtumia");
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
        if (r.next()) {

            return true;
        }

        return false;
    }

}
