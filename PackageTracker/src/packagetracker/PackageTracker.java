/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packagetracker;

import java.util.Scanner;

/**
 *
 * @author markulai
 */
public class PackageTracker {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        Scanner lukija = new Scanner(System.in);
        ui.start(lukija);
    }
    
}
