/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemreservationclient;

import ejb.session.stateless.CreditCardSessionBeanRemote;
import ejb.session.stateless.CustomerSessionBeanRemote;
import ejb.session.stateless.FlightReservationSessionBeanRemote;
import ejb.session.stateless.FlightSearchSessionBeanRemote;
import ejb.session.stateless.SeatInventorySessionBeanRemote;
import entity.CreditCardEntity;
import entity.CustomerEntity;
import entity.FareEntity;
import entity.FlightReservationEntity;
import entity.FlightScheduleEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewCustomerException;
import util.exception.CreateNewFlightReservationException;
import util.exception.CustomerNotFoundException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.InvalidLoginCredentialsException;
import util.exception.NoMatchingFlightsException;
import util.exception.ReserveSeatException;
import util.exception.SearchFlightFailedException;
import util.exception.SeatNotFoundException;

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
    @EJB
    private FlightReservationSessionBeanRemote flightReservationSessionBeanRemote;

    private CustomerEntity customerEntity;

    public MainApp() {
    }

    public MainApp(CustomerSessionBeanRemote customerSessionBeanRemote, FlightSearchSessionBeanRemote flightSearchSessionBeanRemote, SeatInventorySessionBeanRemote seatInventorySessionBeanRemote, FlightReservationSessionBeanRemote flightReservationSessionBeanRemote) {
        this.customerSessionBeanRemote = customerSessionBeanRemote;
        this.flightSearchSessionBeanRemote = flightSearchSessionBeanRemote;
        this.seatInventorySessionBeanRemote = seatInventorySessionBeanRemote;
        this.flightReservationSessionBeanRemote = flightReservationSessionBeanRemote;
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
            System.out.println("*** Flight Reservation System Reservation :: Main Menu ***\n");
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
                    viewAllFlightReservations();
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

    public void searchFlight() {
        System.out.println("*** Flight Reservation System Reservation :: Search Flights ***\n");
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
                System.out.print("Enter return departure date> ");
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
            System.out.print("Enter preferred cabin class (F/J/W/Y/\"-\" if no preference> ");
            inputCabinClass = scanner.nextLine().trim();
            if (!inputCabinClass.equals("F") && !inputCabinClass.equals("J") && !inputCabinClass.equals("W") && !inputCabinClass.equals("Y") && !inputCabinClass.equals("-")) {
                System.out.println("Invalid response! Choose F/J/W/Y");
            }
        } while (!inputCabinClass.equals("F") && !inputCabinClass.equals("J") && !inputCabinClass.equals("W") && !inputCabinClass.equals("Y") && !inputCabinClass.equals("-"));
        if (!inputCabinClass.equals("-")) {
            preferredCabinClass = CabinClassEnum.valueOf(inputCabinClass);
        } else {
            preferredCabinClass = null;
        }

        Integer reserveOption = 0;

        try {
            if (option == 1) { //one way flight
                HashMap<Integer, List<FlightScheduleEntity>> oneWayFlights = flightSearchSessionBeanRemote.searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabinClass);
                this.printAvailableFlights(oneWayFlights, preferredCabinClass, numberOfPassengers);

                Integer response = 0;
                System.out.println("1: Reserve Flight");
                System.out.println("2: Back");
                do {
                    System.out.print("> ");
                    response = scanner.nextInt();
                    if (response <= 0 || response > 2) {
                        System.out.println("Invalid response! Enter 1/2");
                    }
                } while (response <= 0 || response > 2);

                if (response == 1) {
                    List<FlightScheduleEntity> itinerary = new ArrayList<>();

                    Integer chooseOption = 0;
                    do {
                        System.out.print("Select a Flight Schedule option> ");
                        chooseOption = scanner.nextInt();
                        if (chooseOption <= 0 || chooseOption > oneWayFlights.size()) {
                            System.out.println("Invalid option! Choose again.");
                        }
                    } while (chooseOption <= 0 || chooseOption > oneWayFlights.size());
                    scanner.nextLine();

                    String cabinClassResponse = "";
                    if (preferredCabinClass == null) {
                        do {
                            System.out.println("Select a cabin class> ");
                            cabinClassResponse = scanner.nextLine().trim();
                            if (!cabinClassResponse.equals("F") && !cabinClassResponse.equals("J") && !cabinClassResponse.equals("W") && !cabinClassResponse.equals("Y")) {
                                System.out.println("Invalid response! Enter F/J/W/Y");
                            }
                        } while (!cabinClassResponse.equals("F") && !cabinClassResponse.equals("J") && !cabinClassResponse.equals("W") && !cabinClassResponse.equals("Y"));
                        preferredCabinClass = CabinClassEnum.valueOf(cabinClassResponse);
                    }

                    reserveFlight(oneWayFlights.get(chooseOption), preferredCabinClass, numberOfPassengers);
                    //print message
                }

            } else { //if two way flight
                HashMap<Integer, List<FlightScheduleEntity>> toFlight = flightSearchSessionBeanRemote.searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass).get(0);
                this.printAvailableFlights(toFlight, preferredCabinClass, numberOfPassengers);

                HashMap<Integer, List<FlightScheduleEntity>> returnFlight = flightSearchSessionBeanRemote.searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass).get(1);
                this.printAvailableFlights(returnFlight, preferredCabinClass, numberOfPassengers);

                Integer response = 0;
                System.out.println("1: Reserve Flight");
                System.out.println("2: Back");
                do {
                    System.out.print("> ");
                    response = scanner.nextInt();
                    if (response <= 0 || response > 2) {
                        System.out.println("Invalid response! Enter 1/2");
                    }
                } while (response <= 0 || response > 2);

                if (response == 1) {
                    List<FlightScheduleEntity> itinerary = new ArrayList<>();

                    Integer chooseToFlightOption = 0;
                    do {
                        System.out.println("Select a Flight Schedule option> ");
                        if (chooseToFlightOption <= 0 || chooseToFlightOption > toFlight.size()) {
                            System.out.println("Invalid option! Choose again.");
                        }
                    } while (chooseToFlightOption <= 0 || chooseToFlightOption > toFlight.size());

                    Integer chooseReturnFlightOption = 0;
                    do {
                        System.out.println("Select a Return Flight Schedule option> ");
                        if (chooseReturnFlightOption <= 0 || chooseReturnFlightOption > toFlight.size()) {
                            System.out.println("Invalid option! Choose again.");
                        }
                    } while (chooseReturnFlightOption <= 0 || chooseReturnFlightOption > toFlight.size());

                    List<FlightScheduleEntity> combinedItinerary = new ArrayList<>();
                    for (FlightScheduleEntity flightSchedule : toFlight.get(chooseToFlightOption - 1)) {
                        combinedItinerary.add(flightSchedule);
                    }
                    for (FlightScheduleEntity flightSchedule : returnFlight.get(chooseToFlightOption - 1)) {
                        combinedItinerary.add(flightSchedule);
                    }
                    reserveFlight(combinedItinerary, preferredCabinClass, numberOfPassengers);
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

    //called from searchFlight() method
    private void printAvailableFlights(HashMap<Integer, List<FlightScheduleEntity>> oneWayFlights, CabinClassEnum preferredCabinClass, Integer numberOfPassengers) throws FlightScheduleNotFoundException {
        try {
            for (Map.Entry<Integer, List<FlightScheduleEntity>> map : oneWayFlights.entrySet()) {
                System.out.println("Option #" + map.getKey());
                Double pricePerPassenger = 0.0;
                Double totalAmount = 0.0;

                if (map.getValue().size() == 1) { //direct flight
                    FlightScheduleEntity flightSchedule = map.getValue().get(0);
                    System.out.println("Flight Schedule Id: " + flightSchedule.getFlightScheduleId());
                    System.out.println("\tDeparture date: " + flightSchedule.getDepartureDate().toString().substring(0, 10)); //need to change this
                    System.out.println("\tDeparture time: " + flightSchedule.getDepartureDate().toString().substring(10));
                    //print avail seats
                    SeatInventory seats = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(flightSchedule.getFlightScheduleId());
                    //if user has chosen a cabin class
                    if (preferredCabinClass != null) {
                        pricePerPassenger = getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), preferredCabinClass);
                        if (pricePerPassenger != Double.MAX_VALUE) {
                            System.out.println("available seats: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[0]);
                            System.out.println("reserved seats: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[1]);
                            System.out.println("\tNumber of available seats for selected cabin class: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[2]);

                            System.out.println("\tPrice per passenger: " + pricePerPassenger);
                            totalAmount = pricePerPassenger * numberOfPassengers;
                            System.out.println("\tTotal amount for " + numberOfPassengers + " passengers: " + totalAmount);
                        }
                    } else { //if user did not choose a cabin class
                        for (Map.Entry<CabinClassEnum, Integer[]> seatsMap : seats.getCabinSeatsInventory().entrySet()) {
                            pricePerPassenger = getLowestAmountForCabin(flightSchedule.getFlightSchedulePlan().getFares(), seatsMap.getKey());
                            if (pricePerPassenger != Double.MAX_VALUE) {
                                System.out.println("\tNumber of available seats for cabin class " + seatsMap.getKey().toString() + ": " + seatsMap.getValue()[2]);

                                System.out.println("\tPrice per passenger: " + pricePerPassenger);
                                totalAmount = pricePerPassenger * numberOfPassengers;
                                System.out.println("\tTotal amount for " + numberOfPassengers + " passengers: " + totalAmount);
                            }
                        }
                    }

                } else { //has at least one connecting flight 
                    Double fareForSingleCabin = 0.0;
                    HashMap<CabinClassEnum, Double> fareMap = new HashMap<>();
                    for (FlightScheduleEntity connectingFlight : map.getValue()) {
                        System.out.println("Flight Schedule Id: " + connectingFlight.getFlightScheduleId());
                        System.out.println("\tDeparture date: " + connectingFlight.getDepartureDate().toString().substring(0, 10)); //need to change this
                        System.out.println("\tDeparture time: " + connectingFlight.getDepartureDate().toString().substring(10));

                        SeatInventory seats = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(connectingFlight.getFlightScheduleId());

                        if (preferredCabinClass != null) { //if user has chosen a cabin class
                            System.out.println("\tNumber of available seats for selected cabin class: " + seats.getCabinSeatsInventory().get(preferredCabinClass)[2]);

                            fareForSingleCabin += getLowestAmountForCabin(connectingFlight.getFlightSchedulePlan().getFares(), preferredCabinClass);
                        } else { //if user did not choose a cabin class
                            for (Map.Entry<CabinClassEnum, Integer[]> seatsMap : seats.getCabinSeatsInventory().entrySet()) {
                                System.out.println("\tNumber of available seats for cabin class " + seatsMap.getKey().toString() + ": " + seatsMap.getValue()[2]);

                                if (fareMap.containsKey(seatsMap.getKey())) {
                                    fareMap.put(seatsMap.getKey(), fareMap.get(seatsMap.getKey()) + getLowestAmountForCabin(connectingFlight.getFlightSchedulePlan().getFares(), seatsMap.getKey()));
                                } else {
                                    fareMap.put(seatsMap.getKey(), getLowestAmountForCabin(connectingFlight.getFlightSchedulePlan().getFares(), seatsMap.getKey()));
                                }
                            }
                        }
                    }
                    if (preferredCabinClass == null) { //print out the lowest fares for all the available cabin class
                        for (Map.Entry<CabinClassEnum, Double> fares : fareMap.entrySet()) {
                            if (fares.getValue() != 0.0) {
                                System.out.println("Price per passenger for cabin class " + fares.getKey().toString() + ": " + fares.getValue());
                                totalAmount = fares.getValue() * numberOfPassengers;
                                System.out.println("Total amount for cabin class " + fares.getKey().toString() + " for " + numberOfPassengers + " passengers: " + totalAmount);
                            }
                        }
                    } else {
                        System.out.println("\tPrice per passenger: " + fareForSingleCabin);
                        totalAmount = fareForSingleCabin * numberOfPassengers;
                        System.out.println("\tTotal amount for " + numberOfPassengers + " passengers: " + totalAmount);
                    }
                } //END ELSE
            } //END FOR
        } catch (FlightScheduleNotFoundException ex) {
            throw new FlightScheduleNotFoundException(ex.getMessage());
        }
    }

    //called from searchFlight() method
    private void reserveFlight(List<FlightScheduleEntity> flightSchedules, CabinClassEnum preferredCabinClass, Integer numberOfPassengers) {
        System.out.println("*** Flight Reservation System Reservation :: Reserve Flight ***\n");
        Scanner scanner = new Scanner(System.in);

        //create list of passengers
        List<PassengerEntity> passengers = new ArrayList<>();
        for (int i = 1; i <= numberOfPassengers; i++) {
            PassengerEntity passenger = new PassengerEntity();
            //firstname, lastname, passport number
            String firstName;
            String lastName;
            String passportNumber;

            do {
                System.out.print("Enter first name of Passenger " + i + "> ");
                firstName = scanner.nextLine().trim();
                if (firstName.length() <= 0 || firstName.length() > 32) {
                    System.out.println("First name must be between lengths 1 and 32");
                }
            } while (firstName.length() <= 0 || firstName.length() > 32);

            do {
                System.out.print("Enter last name of Passenger " + i + "> ");
                lastName = scanner.nextLine().trim();
                if (lastName.length() <= 0 || lastName.length() > 32) {
                    System.out.println("Last name must be between lengths 1 and 32");
                }
            } while (lastName.length() <= 0 || lastName.length() > 32);

            do {
                System.out.print("Enter passport number of Passenger " + i + "> ");
                passportNumber = scanner.nextLine().trim();
                if (passportNumber.length() <= 0 || passportNumber.length() > 10) {
                    System.out.println("Passport number must be between lengths 1 and 10");
                }
            } while (passportNumber.length() <= 0 || passportNumber.length() > 10);

            passenger.setFirstName(firstName);
            passenger.setLastName(lastName);
            passenger.setPassportNumber(passportNumber);
            passengers.add(passenger);
        }

        try {
            for (FlightScheduleEntity flightSchedule : flightSchedules) {

                List<SeatEntity> seatsForCabin = seatInventorySessionBeanRemote.retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flightSchedule.getFlightScheduleId(), preferredCabinClass);
                List<String> seatNumbers = seatsForCabin.stream().map(x -> x.getSeatNumber()).collect(Collectors.toList());
                seatNumbers.sort((a, b) -> {
                    String firstPrefix = a.substring(0, a.length() - 1);
                    String firstSuffix = a.substring(a.length() - 1);
                    String secondPrefix = b.substring(0, b.length() - 1);
                    String secondSuffix = b.substring(b.length() - 1);

                    if (firstPrefix.hashCode() == secondPrefix.hashCode()) {
                        return firstSuffix.hashCode() - secondSuffix.hashCode();
                    } else {
                        return firstPrefix.hashCode() - secondPrefix.hashCode();
                    }
                });

                for (int i = 0; i < seatNumbers.size(); i++) {
                    System.out.print(seatNumbers.get(i) + " ");
                    if (i != seatNumbers.size() - 1) {
                        String currSeatSuffix = seatNumbers.get(i).substring(seatNumbers.get(i).length() - 1);
                        String nextSeatSuffix = seatNumbers.get(i + 1).substring(seatNumbers.get(i + 1).length() - 1);
                        if (currSeatSuffix.hashCode() > nextSeatSuffix.hashCode()) {
                            System.out.println("");
                        }
                    }
                }
                System.out.print("\n");

                for (PassengerEntity passenger : passengers) {
                    String seatNumber = "";
                    do {
                        System.out.print("Select a seat for " + passenger.getFirstName() + " " + passenger.getLastName() + "> ");
                        seatNumber = scanner.nextLine().trim();
                        if (!seatNumbers.contains(seatNumber)) {
                            System.out.println("Seat number not available! Choose another seat");
                        }
                    } while (!seatNumbers.contains(seatNumber));
                    SeatEntity chosenSeat = seatInventorySessionBeanRemote.retrieveAvailableSeatFromFlightScheduleAndCabin(flightSchedule.getFlightScheduleId(), preferredCabinClass, seatNumber);
                    seatNumbers.remove(seatNumber);
                    seatsForCabin.remove(chosenSeat);
                    passenger.getSeats().add(chosenSeat);
                }
            }

            CreditCardEntity creditCard = new CreditCardEntity();
            String ccFirstName = "";
            String ccLastName = "";
            String ccNumber = "";
            String dateInput = "";
            Date expiryDate = new Date();
            String cvc = "";

            do {
                System.out.print("Enter first name on credit card> ");
                ccFirstName = scanner.nextLine().trim();
                if (ccFirstName.length() <= 0 || ccFirstName.length() > 32) {
                    System.out.println("First name must be between lengths 1 and 32");
                }
            } while (ccFirstName.length() <= 0 || ccFirstName.length() > 32);

            do {
                System.out.print("Enter last name on credit card> ");
                ccLastName = scanner.nextLine().trim();
                if (ccLastName.length() <= 0 || ccLastName.length() > 32) {
                    System.out.println("Last name must be between lengths 1 and 32");
                }
            } while (ccLastName.length() <= 0 || ccLastName.length() > 32);

            do {
                System.out.print("Enter credit card number> ");
                ccNumber = scanner.nextLine().trim();
                if (ccNumber.length() <= 0 || ccNumber.length() > 16) {
                    System.out.println("Last name must be between lengths 1 and 16");
                }
            } while (ccNumber.length() <= 0 || ccNumber.length() > 16);

            Boolean dateCheck = false;
            SimpleDateFormat format = new SimpleDateFormat("dd/MM");
            do {
                System.out.print("Enter credit card expiry date (dd/mm)> ");
                dateInput = scanner.nextLine().trim();
                try {
                    expiryDate = format.parse(dateInput);
                    dateCheck = true;
                } catch (ParseException ex) {
                    System.out.println("Wrong format for date!");
                }
            } while (!dateCheck);

            do {
                System.out.print("Enter CVC> ");
                cvc = scanner.nextLine().trim();
                if (cvc.length() != 3) {
                    System.out.println("CVC must be length 3");
                }
            } while (cvc.length() != 3);

            creditCard.setFirstName(ccFirstName);
            creditCard.setLastName(ccLastName);
            creditCard.setCardNumber(ccNumber);
            creditCard.setDateOfExpiry(expiryDate);
            creditCard.setCvc(cvc);

            Long reservationId = flightReservationSessionBeanRemote.createNewFlightReservation(flightSchedules, passengers, creditCard, customerEntity);
            System.out.println("Reservation successful! Reservation id: " + reservationId);

        } catch (SeatNotFoundException | ReserveSeatException | CreateNewFlightReservationException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void viewAllFlightReservations() {
        List<FlightReservationEntity> flightReservations = flightReservationSessionBeanRemote.viewFlightReservationByCustomer(customerEntity.getUserId());
        for (FlightReservationEntity flightReservation : flightReservations) {
            System.out.println("Flight reservation Id: " + flightReservation.getFlightReservationId());
            System.out.println("Total amount paid: " + flightReservation.getTotalAmount() + "\n");
        }
    }

    private void viewFlightReservationDetails(Long flightReservationId) {
        FlightReservationEntity flightReservation = flightReservationSessionBeanRemote.viewFlightReservationsByFlightScheduleId(flightReservationId);

        System.out.println("Itinerary:");
        for (FlightScheduleEntity flightSchedule : flightReservation.getFlightSchedules()) {
            System.out.print(flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getOriginAirport().getAirportName() + " ---> ");
            System.out.println(flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportName());
            System.out.println("Departing on " + flightSchedule.getDepartureDate());
            System.out.println("Estimated arrival time is " + flightSchedule.getArrivalDateTime() + "\n");

            for (PassengerEntity passenger : flightReservation.getPassengers()) {
                System.out.println("\tPassenger: " + passenger.getFirstName() + " " + passenger.getLastName());
                for (SeatEntity seat : passenger.getSeats()) {
                    if (seat.getFlightSchedule().getFlightScheduleId().equals(flightSchedule.getFlightScheduleId())) {
                        System.out.println("\tCabin class: " + seat.getCabinClassEnum().toString());
                        System.out.println("\tSeat number: " + seat.getSeatNumber());
                        break;
                    }
                }

            }
        }
        System.out.println("TOTAL AMOUNT PAID: " + flightReservation.getTotalAmount());
    }

}
