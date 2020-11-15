/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemreservationclient;

import ejb.session.stateless.AirportEntitySessionBeanRemote;
import ejb.session.stateless.CustomerSessionBeanRemote;
import ejb.session.stateless.FareEntitySessionBeanRemote;
import ejb.session.stateless.FlightReservationSessionBeanRemote;
import ejb.session.stateless.FlightScheduleSessionBeanRemote;
import ejb.session.stateless.FlightSearchSessionBeanRemote;
import ejb.session.stateless.SeatInventorySessionBeanRemote;
import javax.ejb.EJB;

/**
 *
 * @author Li Xin
 */
public class Main {

//    @EJB
//    private static FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote;

    @EJB
    private static AirportEntitySessionBeanRemote airportEntitySessionBeanRemote;
    @EJB
    private static CustomerSessionBeanRemote customerSessionBeanRemote;
    @EJB
    private static FlightSearchSessionBeanRemote flightSearchSessionBeanRemote;
    @EJB
    private static SeatInventorySessionBeanRemote seatInventorySessionBeanRemote;
    @EJB
    private static FlightReservationSessionBeanRemote flightReservationSessionBeanRemote;
    @EJB
    private static FareEntitySessionBeanRemote fareEntitySessionBeanRemote;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        MainApp mainApp = new MainApp(
                customerSessionBeanRemote,
                flightSearchSessionBeanRemote,
                seatInventorySessionBeanRemote,
                flightReservationSessionBeanRemote,
                airportEntitySessionBeanRemote
//                flightScheduleSessionBeanRemote
        );

        mainApp.runApp();
    }

}
