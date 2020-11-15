/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holidayreservationsystemclient;

import ejb.session.ws.CreateNewFlightReservationException_Exception;
import ejb.session.ws.FlightScheduleNotFoundException_Exception;
import ejb.session.ws.InvalidLoginCredentialsException_Exception;
import ejb.session.ws.NoMatchingFlightsException_Exception;
import ejb.session.ws.PartnerNotFoundException_Exception;
import ejb.session.ws.RemoteFlightSchedule;
import ejb.session.ws.RemotePassenger;
import ejb.session.ws.RemoteSeat;
import ejb.session.ws.SearchFlightFailedException_Exception;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Li Xin
 */
public class MainApp {

    private String username;
    private String password;

    public MainApp() {
        this.username = null;
        this.password = null;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Welcome to Flight Reservation System Reservation ***\n");
            System.out.println("1: Login");
            System.out.println("2: Search Flight");
            System.out.println("3: Exit\n");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");

                response = Integer.parseInt(scanner.nextLine());

                if (response == 1) {
                    doLogin();
                    if (this.username != null && this.password != null) {
                        System.out.println("Welcome ! " + username);
                        menuMain();
                    }
                } else if (response == 2) {
                    searchFlight();
                } else if (response == 3) {
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

    private void doLogin() {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        String password = "";

        System.out.println("*** Flight Reservation System :: Login ***\n");
        System.out.print("Enter username> ");
        username = scanner.nextLine().trim();
        System.out.print("Enter password> ");
        password = scanner.nextLine().trim();

        if (username.length() > 0 && password.length() > 0) {
            if (doLogin(username, password)) {
                this.username = username;
                this.password = password;
            } else {
                System.out.println("Invalid login!");
            }
        } else {
            this.username = null;
            this.password = null;
            System.out.println("Invalid login!");
        }
    }

    private void menuMain() {
        if (this.username == null || this.password == null) {
            return;
        }
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
                    retrieveAllReservations();
                } else if (response == 3) {
                    retrieveReservation();
                } else if (response == 4) {
                    // logout
                    this.username = null;
                    this.password = null;
                    return;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 4) {
                break;
            }
        }
    }

    private void searchFlight() {
        System.out.println("*** Flight Reservation System Reservation :: Search Flights ***\n");
        retrieveAllAirports();

        Scanner scanner = new Scanner(System.in);

        System.out.println("1: Search One Way Flights");
        System.out.println("2: Search Two Way Flights");

        Integer option = 0;
        do {
            System.out.print("> ");

            option = Integer.parseInt(scanner.nextLine());

            if (option <= 0 || option > 2) {
                System.out.println("Invalid response! Choose either 1 or 2\n");
            }
        } while (option <= 0 || option > 2);

        System.out.print("Enter Departure Airport Id> ");
        Long departureAirportId = Long.parseLong(scanner.nextLine());

        System.out.print("Enter Arrival Airport Id> ");
        Long arrivalAirportId = Long.parseLong(scanner.nextLine());

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

            numberOfPassengers = Integer.parseInt(scanner.nextLine());

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
            directFlightResponse = Integer.parseInt(scanner.nextLine());
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

        String preferredCabinClass = null;
        do {
            System.out.print("Enter preferred cabin class (F/J/W/Y/\"-\" if no preference> ");
            preferredCabinClass = scanner.nextLine().trim();
            if (!preferredCabinClass.equals("F") && !preferredCabinClass.equals("J") && !preferredCabinClass.equals("W") && !preferredCabinClass.equals("Y") && !preferredCabinClass.equals("-")) {
                System.out.println("Invalid response! Choose F/J/W/Y");
            }
        } while (!preferredCabinClass.equals("F") && !preferredCabinClass.equals("J") && !preferredCabinClass.equals("W") && !preferredCabinClass.equals("Y") && !preferredCabinClass.equals("-"));

        if (preferredCabinClass.equals("-")) {
            preferredCabinClass = null;
        }

        if (option == 1) { //one way flight
            searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabinClass);
        } else {
            searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass);
        }
    }

    private boolean doLogin(String username, String password) {
        boolean result = false;

        ejb.session.ws.HolidayReservationWebService_Service service1 = new ejb.session.ws.HolidayReservationWebService_Service();
        ejb.session.ws.HolidayReservationWebService port1 = service1.getHolidayReservationWebServicePort();
        // TODO process result here
        result = port1.login(username, password);

        return result;
    }

    private void searchOneWayFlights(
            long departureAirportId,
            long arrivalAirportId,
            Date departureDate,
            Integer numberOfPassengers,
            boolean preferDirectFlight,
            String preferredCabinClass
    ) {
        Scanner sc = new Scanner(System.in);

        try {
            XMLGregorianCalendar date
                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(departureDate.toString());

            ejb.session.ws.HolidayReservationWebService_Service service3 = new ejb.session.ws.HolidayReservationWebService_Service();
            ejb.session.ws.HolidayReservationWebService port3 = service3.getHolidayReservationWebServicePort();
            // WSDL not updating
            java.util.List<ejb.session.ws.RemoteFlightSchedule> result = (java.util.List<ejb.session.ws.RemoteFlightSchedule>) port3.searchOneWayFlights(departureAirportId, arrivalAirportId, date, numberOfPassengers, preferDirectFlight, preferredCabinClass);
            for (ejb.session.ws.RemoteFlightSchedule flight : result) {
                System.out.println("Itinery number: " + flight.getItineryNumber());
                System.out.println("Flight schedule id: " + flight.getFlightScheduleID() + ", flight number: " + flight.getFlightNumber() + ", departure date: " + flight.getDepartureDate() + ", arrival date: " + flight.getArrivalDate());
            }

            System.out.println("Book flight? Y/N");
            if (sc.nextLine().equals("Y")) {
                List<Long> toFlightSchedules = new ArrayList<>();
                List<String> passengers = new ArrayList<>();
                List<String> seats = new ArrayList<>();

                System.out.println("Enter itinery number> ");
                int itineryNumber = Integer.parseInt(sc.nextLine());
                boolean addMorePassenger = false;
                do {
                    String details = "";

                    System.out.print("Enter first name of Passenger >");
                    details += sc.nextLine().trim();

                    System.out.print("Enter last name of Passenger >");
                    details += " ";
                    details += sc.nextLine().trim();

                    System.out.print("Enter passport number of Passenger >");
                    details += " ";
                    String passportNumber = sc.nextLine().trim();
                    details += passportNumber;

                    // add details 
                    passengers.add(details);

                    // loop through each flight
                    for (ejb.session.ws.RemoteFlightSchedule flight : result) {
                        if (flight.getItineryNumber() == itineryNumber) {
                            toFlightSchedules.add(flight.getFlightScheduleID());

                            retrieveFaresForFlightSchedule(flight.getFlightScheduleID());

                            System.out.println("Select preferred cabin class F/J/W/Y> ");
                            String preferredCabin = sc.nextLine();// select cabin

                            String selectedSeat = passportNumber + " ";
                            List< RemoteSeat> availSeats = retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flight.getFlightScheduleID(), preferredCabin);
                            // loop through seats
                            System.out.println("Avail seats: ");
                            for (RemoteSeat seat : availSeats) {
                                System.out.println("Id: " + seat.getSeatID() + ", cabin class: " + seat.getCabinClass() + ", seat number: "
                                        + seat.getSeatNumber());
                            }

                            // enter seat id
                            System.out.println("Enter seat ID> ");
                            selectedSeat += sc.nextLine();
                            seats.add(selectedSeat);
                        }
                    }

                    System.out.println("Add more passenger? Y/N");
                    if (sc.nextLine().equals("Y")) {
                        addMorePassenger = true;
                    } else {
                        addMorePassenger = false;
                    }
                } while (addMorePassenger);

                List<String> creditCard = new ArrayList<>();

                String ccFirstName = "";
                String ccLastName = "";
                String ccNumber = "";
                String expiryDate = "";
                String cvc = "";

                do {
                    System.out.print("Enter first name on credit card> ");
                    ccFirstName = sc.nextLine().trim();
                    if (ccFirstName.length() <= 0 || ccFirstName.length() > 32) {
                        System.out.println("First name must be between lengths 1 and 32");
                    }
                } while (ccFirstName.length() <= 0 || ccFirstName.length() > 32);

                do {
                    System.out.print("Enter last name on credit card> ");
                    ccLastName = sc.nextLine().trim();
                    if (ccLastName.length() <= 0 || ccLastName.length() > 32) {
                        System.out.println("Last name must be between lengths 1 and 32");
                    }
                } while (ccLastName.length() <= 0 || ccLastName.length() > 32);

                do {
                    System.out.print("Enter credit card number> ");
                    ccNumber = sc.nextLine().trim();
                    if (ccNumber.length() <= 0 || ccNumber.length() > 16) {
                        System.out.println("Last name must be between lengths 1 and 16");
                    }
                } while (ccNumber.length() <= 0 || ccNumber.length() > 16);

                System.out.print("Enter credit card expiry date (dd/mm)> ");
                expiryDate = sc.nextLine().trim();

                do {
                    System.out.print("Enter CVC> ");
                    cvc = sc.nextLine().trim();
                    if (cvc.length() != 3) {
                        System.out.println("CVC must be length 3");
                    }
                } while (cvc.length() != 3);

                reserveFlight(toFlightSchedules, new ArrayList<>(), passengers, seats, creditCard);
            }

        } catch (DatatypeConfigurationException | FlightScheduleNotFoundException_Exception | NoMatchingFlightsException_Exception | NumberFormatException | SearchFlightFailedException_Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void searchTwoWaysFlights(
            long departureAirportId,
            long arrivalAirportId,
            Date departureDate,
            Date returnDate,
            Integer numberOfPassengers,
            boolean preferDirectFlight,
            String preferredCabinClass
    ) {
        Scanner sc = new Scanner(System.in);

        try {
            XMLGregorianCalendar date1
                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(departureDate.toString());
            XMLGregorianCalendar date2
                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(returnDate.toString());
            ejb.session.ws.HolidayReservationWebService_Service service2 = new ejb.session.ws.HolidayReservationWebService_Service();
            ejb.session.ws.HolidayReservationWebService port2 = service2.getHolidayReservationWebServicePort();
            // TODO process result here
            java.util.List<ejb.session.ws.RemoteFlightSchedule> result = (java.util.List<ejb.session.ws.RemoteFlightSchedule>) port2.searchTwoWaysFlights(departureAirportId, arrivalAirportId, date1, date2, numberOfPassengers, preferDirectFlight, preferredCabinClass);

            java.util.List<ejb.session.ws.RemoteFlightSchedule> toFlights = new ArrayList<>();
            java.util.List<ejb.session.ws.RemoteFlightSchedule> returnFlights = new ArrayList<>();

            HashSet<Integer> set = new HashSet<>();
            for (ejb.session.ws.RemoteFlightSchedule flight : result) {
                if (!set.contains(flight.getItineryNumber())) {
                    set.add(flight.getItineryNumber());
                    toFlights.add(flight);
                    System.out.println("Itinery number: " + flight.getItineryNumber());
                    System.out.println("Flight schedule id: " + flight.getFlightScheduleID() + ", flight number: " + flight.getFlightNumber() + ", departure date: " + flight.getDepartureDate() + ", arrival date: " + flight.getArrivalDate());
                } else {
                    System.out.println("Return itinery number: " + flight.getItineryNumber());
                    System.out.println("Return flight schedule id: " + flight.getFlightScheduleID() + ", flight number: " + flight.getFlightNumber() + ", departure date: " + flight.getDepartureDate() + ", arrival date: " + flight.getArrivalDate());
                    returnFlights.add(flight);
                }
            }

            System.out.println("Book flight? Y/N");
            if (sc.nextLine().equals("Y")) {

                List<Long> toFlightSchedules = new ArrayList<>();
                List<Long> returnFlightSchedules = new ArrayList<>();
                List<String> passengers = new ArrayList<>();
                List<String> seats = new ArrayList<>();

                System.out.println("Enter to flight itinery number> ");
                int toItineryNumber = Integer.parseInt(sc.nextLine());

                System.out.println("Enter return flight itinery number> ");
                int returnItineryNumber = Integer.parseInt(sc.nextLine());

                boolean addMorePassenger = false;
                do {
                    String details = "";

                    System.out.print("Enter first name of Passenger >");
                    details += sc.nextLine().trim();

                    System.out.print("Enter last name of Passenger >");
                    details += " ";
                    details += sc.nextLine().trim();

                    System.out.print("Enter passport number of Passenger >");
                    details += " ";
                    String passportNumber = sc.nextLine().trim();
                    details += passportNumber;

                    // add details 
                    passengers.add(details);

                    // loop through each flight
                    for (ejb.session.ws.RemoteFlightSchedule flight : toFlights) {
                        if (flight.getItineryNumber() == toItineryNumber) {
                            System.out.println("Choosing seat for flight " + flight.getFlightNumber());
                            toFlightSchedules.add(flight.getFlightScheduleID());

                            retrieveFaresForFlightSchedule(flight.getFlightScheduleID());

                            System.out.println("Select preferred cabin class for to flight F/J/W/Y> ");
                            String preferredCabin = sc.nextLine();// select cabin

                            String selectedSeat = passportNumber + " ";
                            List< RemoteSeat> availSeats = retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flight.getFlightScheduleID(), preferredCabin);
                            // loop through seats
                            System.out.println("Avail seats: ");
                            for (RemoteSeat seat : availSeats) {
                                System.out.println("Id: " + seat.getSeatID() + ", cabin class: " + seat.getCabinClass() + ", seat number: "
                                        + seat.getSeatNumber());
                            }

                            // enter seat id
                            System.out.println("Enter seat ID> ");
                            selectedSeat += sc.nextLine();
                            seats.add(selectedSeat);
                        }
                    }

                    for (ejb.session.ws.RemoteFlightSchedule flight : returnFlights) {
                        if (flight.getItineryNumber() == returnItineryNumber) {
                            System.out.println("Choosing seat for flight " + flight.getFlightNumber());
                            returnFlightSchedules.add(flight.getFlightScheduleID());

                            retrieveFaresForFlightSchedule(flight.getFlightScheduleID());

                            System.out.println("Select preferred cabin class for return flight F/J/W/Y> ");
                            String preferredCabin = sc.nextLine();// select cabin

                            String selectedSeat = passportNumber + " ";
                            List< RemoteSeat> availSeats = retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flight.getFlightScheduleID(), preferredCabin);
                            // loop through seats
                            System.out.println("Avail seats: ");
                            for (RemoteSeat seat : availSeats) {
                                System.out.println("Id: " + seat.getSeatID() + ", cabin class: " + seat.getCabinClass() + ", seat number: "
                                        + seat.getSeatNumber());
                            }

                            // enter seat id
                            System.out.println("Enter seat ID> ");
                            selectedSeat += sc.nextLine();
                            seats.add(selectedSeat);
                        }
                    }

                    System.out.println("Add more passenger? Y/N");
                    if (sc.nextLine().equals("Y")) {
                        addMorePassenger = true;
                    } else {
                        addMorePassenger = false;
                    }
                } while (addMorePassenger);

                List<String> creditCard = new ArrayList<>();

                String ccFirstName = "";
                String ccLastName = "";
                String ccNumber = "";
                String expiryDate = "";
                String cvc = "";

                do {
                    System.out.print("Enter credit card number> ");
                    ccNumber = sc.nextLine().trim();
                    if (ccNumber.length() <= 0 || ccNumber.length() > 16) {
                        System.out.println("Last name must be between lengths 1 and 16");
                    }
                } while (ccNumber.length() <= 0 || ccNumber.length() > 16);

                do {
                    System.out.print("Enter first name on credit card> ");
                    ccFirstName = sc.nextLine().trim();
                    if (ccFirstName.length() <= 0 || ccFirstName.length() > 32) {
                        System.out.println("First name must be between lengths 1 and 32");
                    }
                } while (ccFirstName.length() <= 0 || ccFirstName.length() > 32);

                do {
                    System.out.print("Enter last name on credit card> ");
                    ccLastName = sc.nextLine().trim();
                    if (ccLastName.length() <= 0 || ccLastName.length() > 32) {
                        System.out.println("Last name must be between lengths 1 and 32");
                    }
                } while (ccLastName.length() <= 0 || ccLastName.length() > 32);

                System.out.print("Enter credit card expiry date (dd/mm)> ");
                expiryDate = sc.nextLine().trim();

                do {
                    System.out.print("Enter CVC> ");
                    cvc = sc.nextLine().trim();
                    if (cvc.length() != 3) {
                        System.out.println("CVC must be length 3");
                    }
                } while (cvc.length() != 3);

                reserveFlight(toFlightSchedules, returnFlightSchedules, passengers, seats, creditCard);
            }
        } catch (DatatypeConfigurationException | FlightScheduleNotFoundException_Exception | NoMatchingFlightsException_Exception | NumberFormatException | SearchFlightFailedException_Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void reserveFlight(
            List<Long> toFlightSchedules,
            List<Long> returnFlightSchedules,
            List<String> passengers,
            List<String> seats,
            List<String> creditCard
    ) {
        try {
            ejb.session.ws.HolidayReservationWebService_Service service3 = new ejb.session.ws.HolidayReservationWebService_Service();
            ejb.session.ws.HolidayReservationWebService port3 = service3.getHolidayReservationWebServicePort();
            // TODO process result here
            long id = port3.reserveFlight(toFlightSchedules, returnFlightSchedules, passengers, seats, username, password, creditCard);
            System.out.println(id);
        } catch (CreateNewFlightReservationException_Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void retrieveAllAirports() {

        ejb.session.ws.HolidayReservationWebService_Service service4 = new ejb.session.ws.HolidayReservationWebService_Service();
        ejb.session.ws.HolidayReservationWebService port4 = service4.getHolidayReservationWebServicePort();
        // TODO process result here
        java.util.List<ejb.session.ws.RemoteAirport> result = port4.retrieveAllAirports();
        System.out.println("Airports:");
        for (ejb.session.ws.RemoteAirport airport : result) {
            System.out.println("ID: " + airport.getId() + " , IATA code: " + airport.getIataCode());
        }
    }

    private java.util.List<ejb.session.ws.RemoteSeat> retrieveAllAvailableSeatsFromFlightScheduleAndCabin(long flightScheduleId, String cabinClass) throws FlightScheduleNotFoundException_Exception {

        ejb.session.ws.HolidayReservationWebService_Service service5 = new ejb.session.ws.HolidayReservationWebService_Service();
        ejb.session.ws.HolidayReservationWebService port5 = service5.getHolidayReservationWebServicePort();
        // TODO process result here
        return port5.retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flightScheduleId, cabinClass);
    }

    private void retrieveAllReservations() {
        System.out.println("*****View My Reservations*****");
        try {
            ejb.session.ws.HolidayReservationWebService_Service service6 = new ejb.session.ws.HolidayReservationWebService_Service();
            ejb.session.ws.HolidayReservationWebService port6 = service6.getHolidayReservationWebServicePort();
            // TODO process result here
            java.util.List<ejb.session.ws.RemoteReservation> result = port6.retrieveAllReservations(username, password);
            for (ejb.session.ws.RemoteReservation reservation : result) {
                System.out.println("ID: " + reservation.getId() + ", total amount: $" + reservation.getTotalAmount());
            }
        } catch (InvalidLoginCredentialsException_Exception | PartnerNotFoundException_Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void retrieveReservation() {
        System.out.println("*****View Reservation Detail*****");
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter reservation id> ");
        Long reservationId = Long.parseLong(sc.nextLine());

        ejb.session.ws.HolidayReservationWebService_Service service7 = new ejb.session.ws.HolidayReservationWebService_Service();
        ejb.session.ws.HolidayReservationWebService port7 = service7.getHolidayReservationWebServicePort();
        // TODO process result here
        ejb.session.ws.RemoteReservationDetails result = port7.retrieveReservations(reservationId);
        System.out.println("Total amount: $" + result.getTotalAmount());
        List<RemoteFlightSchedule> itinery = result.getItinery();
        System.out.println("Itinery: ");
        for (RemoteFlightSchedule flight : itinery) {
            System.out.println("ID: " + flight.getFlightScheduleID() + ", flight number: " + flight.getFlightNumber() + ", departure date: " + flight.getDepartureDate() + ", arrival date: " + flight.getArrivalDate());
        }

        List<RemotePassenger> passengers = result.getPassengers();
        List<RemoteSeat> seats = result.getSeats();
        System.out.println("Passengers:");
        for (RemotePassenger passenger : passengers) {
            System.out.println("Name: " + passenger.getFirstName() + " " + passenger.getLastName() + ", passport number: " + passenger.getPassportNumber());
            for (RemoteSeat seat : seats) {
                if (seat.getPassengerId() == passenger.getId()) {
                    System.out.println("Cabin class: " + seat.getCabinClass() + ", seat number: " + seat.getSeatNumber());
                }
            }
        }
    }

    private void retrieveFaresForFlightSchedule(Long flightScheduleId) {
        try {

            ejb.session.ws.HolidayReservationWebService_Service service8 = new ejb.session.ws.HolidayReservationWebService_Service();
            ejb.session.ws.HolidayReservationWebService port8 = service8.getHolidayReservationWebServicePort();
            // TODO process result here
            java.util.List<ejb.session.ws.RemoteFare> result = port8.retrieveAllFaresForFlightSchedule(flightScheduleId);
            System.out.println("Fares: ");
            for (ejb.session.ws.RemoteFare fare : result) {
                System.out.println("ID: " + fare.getFareId() + " , fare basis code: " + fare.getFareBasisCode() + " , amount: $" + fare.getFareAmount());
            }
        } catch (FlightScheduleNotFoundException_Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
