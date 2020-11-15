/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemmanagementclient;

import ejb.session.stateless.FlightReservationSessionBeanRemote;
import ejb.session.stateless.FlightSessionBeanRemote;
import ejb.session.stateless.SeatInventorySessionBeanRemote;
import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import entity.SeatEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.ejb.EJB;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.FlightNotFoundException;
import util.exception.FlightScheduleNotFoundException;

/**
 *
 * @author kiyon
 */
public class SalesManagementModule {

    @EJB
    private FlightSessionBeanRemote flightSessionBeanRemote;
    @EJB
    private SeatInventorySessionBeanRemote seatInventorySessionBeanRemote;
    @EJB
    private FlightReservationSessionBeanRemote flightReservationSessionBeanRemote;

    public SalesManagementModule() {
    }

    public SalesManagementModule(FlightSessionBeanRemote flightSessionBeanRemote, SeatInventorySessionBeanRemote seatInventorySessionBeanRemote, FlightReservationSessionBeanRemote flightReservationSessionBeanRemote) {
        this.flightSessionBeanRemote = flightSessionBeanRemote;
        this.seatInventorySessionBeanRemote = seatInventorySessionBeanRemote;
        this.flightReservationSessionBeanRemote = flightReservationSessionBeanRemote;
    }

    public void salesManagementMenu() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Flight Management System: Sales Management Module ***\n");
            System.out.println("1: View Seats Inventory");
            System.out.println("2: View Flight Reservations");
            System.out.println("3: Logout\n");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");

                response = Integer.parseInt(scanner.nextLine().trim());

                if (response == 1) {
                    viewSeatsInventory();
                } else if (response == 2) {
                    viewFlightReservations();
                } else if (response == 3) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 3) {
                break;
            }
        }
    }

    private void viewFlightSchedules(List<FlightSchedulePlanEntity> flightSchedulePlans) {
        for (FlightSchedulePlanEntity flightSchedulePlan : flightSchedulePlans) {
            System.out.println("Flight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());

            List<FlightScheduleEntity> flightSchedules = flightSchedulePlan.getFlightSchedules();
            for (FlightScheduleEntity flightSchedule : flightSchedules) {
                System.out.println("\tFlight Schedule Id " + flightSchedule.getFlightScheduleId() + ", Departure date: " + flightSchedule.getDepartureDate());
            }

            if (flightSchedulePlan.getReturnFlightSchedulePlan() != null) {
                FlightSchedulePlanEntity returFlightSchedulePlan = flightSchedulePlan.getReturnFlightSchedulePlan();
                System.out.println("Return Flight Schedule Plan Id: " + returFlightSchedulePlan.getFlightSchedulePlanId());

                List<FlightScheduleEntity> returnFlightSchedules = returFlightSchedulePlan.getFlightSchedules();
                for (FlightScheduleEntity flightSchedule : returnFlightSchedules) {
                    System.out.println("\tFlight Schedule Id " + flightSchedule.getFlightScheduleId() + ", Departure date: " + flightSchedule.getDepartureDate());
                }
            }
        }
    }

    private void viewSeatsInventory() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Sales Managemnet Module: View Seats Inventory ***\n");
        System.out.print("Enter flight number> ");
        String flightNumber = scanner.nextLine().trim();
        System.out.println("flight number " + flightNumber);

        //flight number -> FlightEntity -> FlightSchedulePlan -> flight schedule
        try {
            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
            List<FlightSchedulePlanEntity> flightSchedulePlans = flight.getFlightSchedulePlans();

            if (flightSchedulePlans.isEmpty()) { //no flight schedules
                System.out.println("Flight has no flight schedules!");
            } else {
                viewFlightSchedules(flightSchedulePlans);

                System.out.println("Enter flight schedule ID to view its seat inventory> ");
                Long selectedOption = Long.parseLong(scanner.nextLine());

                SeatInventory seatInventory = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(selectedOption);

                System.out.println("Total number of available seats acroass all cabins: " + seatInventory.getTotalAvailSeats());
                System.out.println("Total number of reserved seats acroass all cabins: " + seatInventory.getTotalReservedSeats());
                System.out.println("Total number of balance seats acroass all cabins: " + seatInventory.getTotalBalancedSeats() + "\n");

                HashMap<CabinClassEnum, Integer[]> map = seatInventory.getCabinSeatsInventory();

                for (Map.Entry<CabinClassEnum, Integer[]> entry : map.entrySet()) {
                    if (entry.getValue()[0] == null) {
                        continue;
                    }
                    System.out.println("Number of available seats for cabin " + entry.getKey().toString() + ": " + entry.getValue()[0]);
                    System.out.println("Number of reserved seats for cabin " + entry.getKey().toString() + ": " + entry.getValue()[1]);
                    System.out.println("Number of balance seats for cabin " + entry.getKey().toString() + ": " + entry.getValue()[2] + "\n");
                }
            }

        } catch (FlightNotFoundException | FlightScheduleNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void viewFlightReservations() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Sales Managemnet Module: Flight Reservations ***\n");
        System.out.print("Enter flight number>");
        String flightNumber = scanner.nextLine().trim();

        //flight number -> FlightEntity -> FlightSchedulePlan -> flight schedule
        try {
            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
            List<FlightSchedulePlanEntity> flightSchedulePlans = flight.getFlightSchedulePlans();

            if (flightSchedulePlans.isEmpty()) { //no flight schedules
                System.out.println("Flight has no flight schedules!");
            } else {
                viewFlightSchedules(flightSchedulePlans);

                System.out.println("Enter flight schedule ID to view its reservations> ");
                Long selectedOption = Long.parseLong(scanner.nextLine());

                List<SeatEntity> reservedSeats = seatInventorySessionBeanRemote.retrieveReservedSeatsByFlightScheduleId(selectedOption);
                printReservedSeats(reservedSeats);
            }

        } catch (FlightNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void printReservedSeats(List<SeatEntity> reservedSeats) {
        for (SeatEntity seat : reservedSeats) {
            System.out.println("cabin class: " + seat.getCabinClassEnum()
                    + ", seat number: " + seat.getSeatNumber()
                    + ", passenger name: " + seat.getPassenger().getFirstName() + " " + seat.getPassenger().getLastName()
                    + " , fare basis code: " + seat.getFareBasisCode());
        }
    }
}
