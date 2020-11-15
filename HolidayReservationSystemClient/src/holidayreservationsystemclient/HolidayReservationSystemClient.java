/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holidayreservationsystemclient;

import ejb.session.ws.FlightScheduleNotFoundException;
import ejb.session.ws.NoMatchingFlightsException;
import ejb.session.ws.SearchFlightFailedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Li Xin
 */
public class HolidayReservationSystemClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.run();
    }
}
