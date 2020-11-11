/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemreservationclient;

import ejb.session.stateless.CustomerSessionBeanRemote;
import ejb.session.stateless.FlightSearchSessionBeanRemote;
import ejb.session.stateless.SeatInventorySessionBeanRemote;
import entity.CustomerEntity;
import entity.FareEntity;
import entity.FlightScheduleEntity;
import entity.UserEntity;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.ejb.EJB;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewCustomerException;
import util.exception.CustomerNotFoundException;
import util.exception.EmployeeNotFoundException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.InvalidLoginCredentialsException;
import util.exception.NoMatchingFlightsException;
import util.exception.SearchFlightFailedException;

/**
 *
 * @author kiyon
 */
public class MainApp {

    @EJB
    private CustomerSessionBeanRemote customerSessionBeanRemote;
    @EJB
    private FlightSearchSessionBeanRemote flightSearchSessionBeanRemote;
    @EJB
    private SeatInventorySessionBeanRemote seatInventorySessionBeanRemote;

    private CustomerEntity customerEntity;

    public MainApp() {
    }

    public MainApp(CustomerSessionBeanRemote customerSessionBeanRemote, FlightSearchSessionBeanRemote flightSearchSessionBeanRemote, SeatInventorySessionBeanRemote seatInventorySessionBeanRemote) {
        this.customerSessionBeanRemote = customerSessionBeanRemote;
        this.flightSearchSessionBeanRemote = flightSearchSessionBeanRemote;
        this.seatInventorySessionBeanRemote = seatInventorySessionBeanRemote;
    }

    public void runApp() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Welcome to Flight Reservation System Reservation ***\n");
            System.out.println("1: Register as Customer");
            System.out.println("2: Login");
            System.out.println("3: Search Flight");
            System.out.println("4: Exit\n");
            response = 0;

            while (response < 1 || response > 4) {
                System.out.print("> ");

                response = scanner.nextInt();
                scanner.nextLine();

                if (response == 1) {
                    registerAsCustomer();
                } else if (response == 2) {
                    try {
                        doLogin();
                        System.out.println("Login successful!\n");

                        menuMain();
                    } catch (InvalidLoginCredentialsException | CustomerNotFoundException ex) {
                        System.out.println(ex.getMessage() + "\n");
                    }
                } else if (response == 3) {
                    searchFlight();
                } else if (response == 4) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 4) {
                break;
            }
        }
    }

    private void registerAsCustomer() {
        System.out.println("*** Flight Reservation System :: Register as Customer ***\n");

        Scanner scanner = new Scanner(System.in);
        CustomerEntity newCustomer = new CustomerEntity();
        String firstName = "";
        String lastName = "";
        String email = "";
        String phoneNumber = "";
        String address = "";
        String username = "";
        String password = "";

        do {
            System.out.print("Enter first name> ");
            firstName = scanner.nextLine().trim();
            if (firstName.length() <= 0 || firstName.length() > 32) {
                System.out.println("Length of first name must be between 1 and 32");
            } else {
                newCustomer.setFirstName(firstName);
            }
        } while (firstName.length() <= 0 || firstName.length() > 32);

        do {
            System.out.print("Enter last name> ");
            lastName = scanner.nextLine().trim();
            if (lastName.length() <= 0 || lastName.length() > 32) {
                System.out.println("Length of last name must be between 1 and 32");
            } else {
                newCustomer.setLastName(lastName);
            }
        } while (lastName.length() <= 0 || lastName.length() > 32);

        do {
            System.out.print("Enter email> ");
            email = scanner.nextLine().trim();
            if (email.length() <= 0 || email.length() > 32) {
                System.out.println("Length of email must be between 1 and 100");
            } else {
                newCustomer.setEmail(email);
            }
        } while (email.length() <= 0 || email.length() > 100);

        do {
            System.out.print("Enter phone number> ");
            phoneNumber = scanner.nextLine().trim();
            if (phoneNumber.length() <= 0 || phoneNumber.length() > 32) {
                System.out.println("Length of phone number must be between 1 and 100");
            } else {
                newCustomer.setMobilePhoneNumber(phoneNumber);
            }
        } while (phoneNumber.length() <= 0 || phoneNumber.length() > 100);

        do {
            System.out.print("Enter address> ");
            address = scanner.nextLine().trim();
            if (address.length() <= 0 || address.length() > 32) {
                System.out.println("Length of address must be between 1 and 100");
            } else {
                newCustomer.setAddress(address);
            }
        } while (address.length() <= 0 || address.length() > 100);

        do {
            System.out.print("Enter username> ");
            username = scanner.nextLine().trim();
            if (username.length() <= 0 || username.length() > 32) {
                System.out.println("Length of username must be between 1 and 32");
            } else {
                newCustomer.setUserName(username);
            }
        } while (username.length() <= 0 || username.length() > 32);

        do {
            System.out.print("Enter password> ");
            password = scanner.nextLine().trim();
            if (password.length() <= 0 || password.length() > 10) {
                System.out.println("Length of password must be between 1 and 10");
            } else {
                newCustomer.setPassword(password);
            }
        } while (password.length() <= 0 || password.length() > 10);

        try {
            Long customerId = customerSessionBeanRemote.registerNewCustomer(newCustomer);
            System.out.println("Customer successfully registered! Customer Id: " + customerId + ".\n");
        } catch (CreateNewCustomerException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void doLogin() throws InvalidLoginCredentialsException, CustomerNotFoundException {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        String password = "";

        System.out.println("*** Flight Reservation System :: Login ***\n");
        System.out.print("Enter username> ");
        username = scanner.nextLine().trim();
        System.out.print("Enter password> ");
        password = scanner.nextLine().trim();

        if (username.length() > 0 && password.length() > 0) {
            try {
                customerEntity = customerSessionBeanRemote.retrieveCustomerByUsernameAndPassword(username, password);
                System.out.println("You are login as " + customerEntity.getFirstName() + " " + customerEntity.getLastName() + ".\n");
            } catch (CustomerNotFoundException ex) {
                throw new CustomerNotFoundException(ex.getMessage());
            } catch (InvalidLoginCredentialsException ex) {
                throw new InvalidLoginCredentialsException(ex.getMessage());
            }
        } else {
            throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Enter username and/or password!");
        }
    }

    private void menuMain() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Welcome to Flight Reservation System Reservation ***\n");
            System.out.println("1: Search Flight");
            System.out.println("2: View my Flight Reservations");
            System.out.println("3: View my Flight Reservation Details");
            System.out.println("4: Logout\n");
            response = 0;

            while (response < 1 || response > 4) {
                System.out.print("> ");

                response = scanner.nextInt();
                scanner.nextLine();

                if (response == 1) {
                    searchFlight();
                } else if (response == 2) {

                } else if (response == 3) {
                    break;
                } else if (response == 4) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 4) {
                break;
            }
        }
    }

//    List<HashMap<Integer, List<FlightScheduleEntity>>> searchTwoWaysFlights(Long departureAirportId, Long arrivalAirportId, 
//        Date departureDate, Date returnDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) 
//        throws NoMatchingFlightsException, SearchFlightFailedException 
//    HashMap<Integer, List<FlightScheduleEntity>> searchOneWayFlights(Long departureAirportId, Long arrivalAirportId, 
//        Date departureDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) 
//        throws NoMatchingFlightsException, SearchFlightFailedException {
    public void searchFlight() {
        System.out.println("*** Welcome to Flight Reservation System Reservation ***\n");
        Scanner scanner = new Scanner(System.in);

        System.out.println("1: Search One Way Flights");
        System.out.println("2: Search Two Way Flights");

        Integer option = 0;
        do {
            System.out.print("> ");
            option = scanner.nextInt();
            if (option <= 0 || option > 2) {
                System.out.println("Invalid response! Choose either 1 or 2\n");
            }
        } while (option <= 0 || option > 2);

        Long departureAirportId = 0L;
        System.out.print("Enter Departure Airport Id> ");
        departureAirportId = scanner.nextLong();

        System.out.print("Enter Arrival Airport Id> ");
        Long arrivalAirportId = scanner.nextLong();

        scanner.nextLine();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String date = "";
        Boolean dateCheck = false;
        Date departureDate = null;
        Date returnDate = null;

        do {
            System.out.print("Enter departure date> ");
            try {
                date = scanner.nextLine().trim();
                departureDate = format.parse(date);
                dateCheck = true;
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!\n");
            }
        } while (!dateCheck);

        if (option == 2) {
            date = "";
            dateCheck = false;

            do {
                try {
                    date = scanner.nextLine().trim();
                    returnDate = format.parse(date);
                    dateCheck = true;
                } catch (ParseException ex) {
                    System.out.println("Wrong format for date!\n");
                }
            } while (!dateCheck);
        }

        Integer numberOfPassengers = 0;
        do {
            System.out.print("Enter number of passengers> ");
            numberOfPassengers = scanner.nextInt();
            if (numberOfPassengers <= 0) {
                System.out.println("Invalid response! Number of passengers must be greater than 0");
            }
        } while (numberOfPassengers <= 0);

        Integer directFlightResponse = 0;
        Boolean preferDirectFlight = false;
        System.out.println("Select Direct or Connecting Flight");
        System.out.println("1: Direct Flight");
        System.out.println("2: Connecting Flight");
        System.out.println("3: No preference");
        do {
            System.out.print("> ");
            directFlightResponse = scanner.nextInt();
            if (directFlightResponse <= 0 || directFlightResponse > 3) {
                System.out.println("Invalid response! Choose option 1 to 3\n");
            }
        } while (directFlightResponse <= 0 || directFlightResponse > 3);
        if (directFlightResponse == 1) {
            preferDirectFlight = true;
        } else if (directFlightResponse == 2) {
            preferDirectFlight = false;
        } else {
            preferDirectFlight = null;
        }

        scanner.nextLine();
        String inputCabinClass = "";
        CabinClassEnum preferredCabinClass = null;
        do {
            System.out.print("Enter preferred cabin class (F/J/W/Y)(\"-\" if no preference)> ");
            inputCabinClass = scanner.nextLine().trim();
            if (!inputCabinClass.equals("F") && !inputCabinClass.equals("J") && !inputCabinClass.equals("W") && !inputCabinClass.equals("Y") && !inputCabinClass.equals("-")) {
                System.out.println("Invalid response! Choose F/J/W/Y");
            }
        } while (!inputCabinClass.equals("F") && !inputCabinClass.equals("J") && !inputCabinClass.equals("W") && !inputCabinClass.equals("Y") && !inputCabinClass.equals("-"));
        if (!inputCabinClass.equals("-")) {
            preferredCabinClass = CabinClassEnum.valueOf(inputCabinClass);
        }

        try {
            if (option == 1) { //one way flight
                HashMap<Integer, List<FlightScheduleEntity>> oneWayFlights = flightSearchSessionBeanRemote.searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabinClass);
                for (Map.Entry<Integer, List<FlightScheduleEntity>> map : oneWayFlights.entrySet()) {
                    System.out.println("Option #" + map.getKey());
                    Double pricePerPassenger = 0.0;
                    for (FlightScheduleEntity flightSchedule : map.getValue()) {
                        //displays the departure date and time
                        //displays the flight schedule availability??
                        //displays the cabin class availability
                        //displays the price per passenger and total price (lowest price)
                        System.out.println("Flight Schedule Id: " + flightSchedule.getFlightScheduleId());
                        System.out.println("\tDeparture date: " + flightSchedule.getDepartureDate().toString().substring(0, 10));
                        System.out.println("\tDeparture time: " + flightSchedule.getDepartureDate().toString().substring(10));

                        SeatInventory seats = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(flightSchedule.getFlightScheduleId());

                        if (inputCabinClass.equals("F") || inputCabinClass.equals("J") || inputCabinClass.equals("W") || inputCabinClass.equals("Y")) {
                            System.out.println("\tNumber of available seats for selected cabin class: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[2]);

                            pricePerPassenger += getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), preferredCabinClass);

                            if (flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId().equals(arrivalAirportId)) {
                                System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                Double totalAmount = pricePerPassenger * numberOfPassengers;
                                System.out.println("\tTotal amount for all passengers: " + totalAmount);
                                pricePerPassenger = 0.0;
                            }

                        } else {
                            for (Map.Entry<CabinClassEnum, Integer[]> seatsMap : seats.getCabinSeatsInventory().entrySet()) {
                                System.out.println("\tNumber of available seats for cabin class " + seatsMap.getKey().toString() + ": " + seatsMap.getValue()[2]);

                                pricePerPassenger += getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), null);

                                if (flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId().equals(arrivalAirportId)) {
                                    System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                    Double totalAmount = pricePerPassenger * numberOfPassengers;
                                    System.out.println("\tTotal amount for all passengers: " + totalAmount);
                                    pricePerPassenger = 0.0;
                                }
                            }
                        }
                    }
                }
            } else { //if two way flight
                HashMap<Integer, List<FlightScheduleEntity>> toFlight = flightSearchSessionBeanRemote.searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass).get(0);
                for (Map.Entry<Integer, List<FlightScheduleEntity>> map : toFlight.entrySet()) {
                    System.out.println("Option #" + map.getKey());
                    Double pricePerPassenger = 0.0;
                    for (FlightScheduleEntity flightSchedule : map.getValue()) {
                        //displays the departure date and time
                        //displays the flight schedule availability??
                        //displays the cabin class availability
                        //displays the price per passenger and total price (lowest price)
                        System.out.println("Flight Schedule Id: " + flightSchedule.getFlightScheduleId());
                        System.out.println("\tDeparture date: " + flightSchedule.getDepartureDate().toString().substring(0, 10));
                        System.out.println("\tDeparture time: " + flightSchedule.getDepartureDate().toString().substring(10));

                        SeatInventory seats = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(flightSchedule.getFlightScheduleId());

                        if (inputCabinClass.equals("F") || inputCabinClass.equals("J") || inputCabinClass.equals("W") || inputCabinClass.equals("Y")) {
                            System.out.println("\tNumber of available seats for selected cabin class: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[2]);

                            pricePerPassenger += getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), preferredCabinClass);

                            if (flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId().equals(arrivalAirportId)) {
                                System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                Double totalAmount = pricePerPassenger * numberOfPassengers;
                                System.out.println("\tTotal amount for all passengers: " + totalAmount);
                                pricePerPassenger = 0.0;
                            }

                        } else {
                            for (Map.Entry<CabinClassEnum, Integer[]> seatsMap : seats.getCabinSeatsInventory().entrySet()) {
                                System.out.println("\tNumber of available seats for cabin class " + seatsMap.getKey().toString() + ": " + seatsMap.getValue()[2]);

                                pricePerPassenger += getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), null);

                                if (flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId().equals(arrivalAirportId)) {
                                    System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                    Double totalAmount = pricePerPassenger * numberOfPassengers;
                                    System.out.println("\tTotal amount for all passengers: " + totalAmount);
                                    pricePerPassenger = 0.0;
                                }
                            }
                        }
                    }
                }

                HashMap<Integer, List<FlightScheduleEntity>> returnFlight = flightSearchSessionBeanRemote.searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass).get(1);
                for (Map.Entry<Integer, List<FlightScheduleEntity>> map : returnFlight.entrySet()) {
                    System.out.println("Option #" + map.getKey());
                    Double pricePerPassenger = 0.0;
                    for (FlightScheduleEntity flightSchedule : map.getValue()) {
                        System.out.println("Flight Schedule Id: " + flightSchedule.getFlightScheduleId());
                        System.out.println("\tDeparture date: " + flightSchedule.getDepartureDate().toString().substring(0, 10));
                        System.out.println("\tDeparture time: " + flightSchedule.getDepartureDate().toString().substring(10));

                        SeatInventory seats = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(flightSchedule.getFlightScheduleId());

                        if (inputCabinClass.equals("F") || inputCabinClass.equals("J") || inputCabinClass.equals("W") || inputCabinClass.equals("Y")) {
                            System.out.println("\tNumber of available seats for selected cabin class: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[2]);

                            pricePerPassenger += getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), preferredCabinClass);

                            if (flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId().equals(arrivalAirportId)) {
                                System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                Double totalAmount = pricePerPassenger * numberOfPassengers;
                                System.out.println("\tTotal amount for all passengers: " + totalAmount);
                                pricePerPassenger = 0.0;
                            }
                        } else {
                            for (Map.Entry<CabinClassEnum, Integer[]> seatsMap : seats.getCabinSeatsInventory().entrySet()) {
                                System.out.println("\tNumber of available seats for cabin class " + seatsMap.getKey().toString() + ": " + seatsMap.getValue()[2]);

                                pricePerPassenger += getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), null);

                                if (flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId().equals(arrivalAirportId)) {
                                    System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                    Double totalAmount = pricePerPassenger * numberOfPassengers;
                                    System.out.println("\tTotal amount for all passengers: " + totalAmount);
                                    pricePerPassenger = 0.0;
                                }
                            }
                        }
                    }
                }
            }
        } catch (NoMatchingFlightsException | SearchFlightFailedException | FlightScheduleNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //called from searchFlight() method
    private Double getLowestAmountForCabin(List<FareEntity> fareList, CabinClassEnum cabinClass) {
        Double amount = Double.MAX_VALUE;
        for (FareEntity fare : fareList) {
            if (cabinClass == null || fare.getCabinClass().equals(cabinClass)) {
                amount = Math.min(amount, fare.getFareAmount().doubleValue());
            }
        }
        return amount;
    }
}
