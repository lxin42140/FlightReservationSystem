/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemmanagementclient;

import ejb.session.stateless.AircraftConfigurationSessionBeanRemote;
import ejb.session.stateless.EmployeeEntitySessionBeanRemote;
import ejb.session.stateless.FlightRouteSessionBeanRemote;
import javax.ejb.EJB;

/**
 *
 * @author Li Xin
 */
public class Main {
    
    @EJB
    private static EmployeeEntitySessionBeanRemote employeeEntitySessionBeanRemote;
    @EJB
    private static AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote;
    @EJB
    private static FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        MainApp mainApp = new MainApp(employeeEntitySessionBeanRemote, aircraftConfigurationSessionBeanRemote, flightRouteSessionBeanRemote);
//        mainApp.runApp();
    }
    
}
