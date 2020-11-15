/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemmanagementclient;

import ejb.session.stateless.AircraftConfigurationSessionBeanRemote;
import ejb.session.stateless.AirportEntitySessionBeanRemote;
import ejb.session.stateless.FareEntitySessionBeanRemote;
import ejb.session.stateless.FlightRouteSessionBeanRemote;
import ejb.session.stateless.FlightSchedulePlanSessionBeanRemote;
import ejb.session.stateless.FlightScheduleSessionBeanRemote;
import ejb.session.stateless.FlightSessionBeanRemote;
import entity.AircraftConfigurationEntity;
import entity.AirportEntity;
import entity.CabinConfigurationEntity;
import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightRouteEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import javax.ejb.EJB;
import util.enumeration.CabinClassEnum;
import util.enumeration.FlightSchedulePlanTypeEnum;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightInUseException;
import util.exception.FlightNotFoundException;
import util.exception.FlightRouteNotFoundException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.FlightSchedulePlanInUseException;
import util.exception.FlightSchedulePlanNotFoundException;
import util.exception.UpdateFlightFailedException;
import util.exception.UpdateFlightSchedulePlanFailedException;

/**
 *
 * @author kiyon
 */
public class FlightOperationModule {

    @EJB
    private FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;
    @EJB
    private FlightSessionBeanRemote flightSessionBeanRemote;
    @EJB
    private FlightSchedulePlanSessionBeanRemote flightSchedulePlanSessionBeanRemote;
    @EJB
    private FareEntitySessionBeanRemote fareEntitySessionBeanRemote;
    @EJB
    private FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote;
    @EJB
    private AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote;

    public FlightOperationModule() {
    }

    public FlightOperationModule(FlightRouteSessionBeanRemote flightRouteSessionBeanRemote,
            FlightSessionBeanRemote flightSessionBeanRemote,
            FlightSchedulePlanSessionBeanRemote flightSchedulePlanSessionBeanRemote,
            FareEntitySessionBeanRemote fareEntitySessionBeanRemote,
            FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote,
            AirportEntitySessionBeanRemote airportEntitySessionBeanRemote,
            AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote
    ) {
        this.flightRouteSessionBeanRemote = flightRouteSessionBeanRemote;
        this.flightSessionBeanRemote = flightSessionBeanRemote;
        this.flightSchedulePlanSessionBeanRemote = flightSchedulePlanSessionBeanRemote;
        this.fareEntitySessionBeanRemote = fareEntitySessionBeanRemote;
        this.flightScheduleSessionBeanRemote = flightScheduleSessionBeanRemote;
        this.aircraftConfigurationSessionBeanRemote = aircraftConfigurationSessionBeanRemote;
        this.flightRouteSessionBeanRemote = flightRouteSessionBeanRemote;
    }

    public void flightOperationMenu() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Flight Management System: Flight Operation Module ***\n");
            System.out.println("1: Create new Flight");
            System.out.println("2: View All Flights");
            System.out.println("3: View Flight Details\n");
            System.out.println("4: Create Flight Schedule Plan");
            System.out.println("5: View All Flight Schedule Plans");
            System.out.println("6: View Flight Schedule Plan Details\n");
            System.out.println("7: Logout\n");
            response = 0;

            while (response < 1 || response > 7) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    createFlight();
                } else if (response == 2) {
                    viewAllFlights();
                } else if (response == 3) {
                    viewFlightDetails();
                } else if (response == 4) {
                    createFlightSchedulePlan();
                } else if (response == 5) {
                    viewAllFlightSchedulePlans();
                } else if (response == 6) {
                    viewFlightSchedulePlanDetails();
                } else if (response == 7) {
                    break;
                } else {
                    System.out.println("Invalid option, please try again!\n");
                }
            }

            if (response == 7) {
                break;
            }
        }
    }

    private void createFlight() {
        Scanner scanner = new Scanner(System.in);

        FlightEntity flight = new FlightEntity();

        System.out.println("*** Flight Planning Module: Create new Flight ***\n");

        try {
            printFlightRoutes();
            System.out.print("Enter Flight Route Id> ");
            Long flightRouteId = Long.parseLong(scanner.nextLine());

            FlightRouteEntity flightRoute = flightRouteSessionBeanRemote.retrieveFlightRouteById(flightRouteId);

            InputMismatchException inputMismatchException = null;
            do {
                String flightNumber = "ML"; //flight number needs to begin with ML
                System.out.print("Enter flight number > ML(_________)");
                flightNumber += Long.parseLong(scanner.nextLine());
                flight.setFlightNumber(flightNumber);
            } while (inputMismatchException != null);

            printAircraftConfigurations();
            System.out.print("Enter Aircraft Configuration Id> ");
            Long aircraftConfigurationId = Long.parseLong(scanner.nextLine());

            String response = "N";

            if (flightRoute.getReturnFlightRoute() != null) {
                do {
                    System.out.println("Do you want to create a complementary return flight? (Y/N)> ");
                    response = scanner.nextLine().trim();
                    if (!response.equals("Y") && !response.equals("N")) {
                        System.out.println("Invalid response! Input Y/N.");
                    }
                } while (!response.equals("Y") && !response.equals("N"));
            }

            Boolean createReturnFlight = response.equals("Y");

            String returnFlightNumber = "ML";
            if (createReturnFlight) {
                inputMismatchException = null;
                do {
                    System.out.print("Enter return flight number> ML(_________)");
                    returnFlightNumber += Long.parseLong(scanner.nextLine());
                } while (inputMismatchException != null);
            }

            String flightId = flightSessionBeanRemote.createNewFlight(flight, flightRouteId, aircraftConfigurationId, createReturnFlight, returnFlightNumber);

            System.out.println("Flight successfully created! " + flightId);
        } catch (FlightRouteNotFoundException | CreateNewFlightException | AircraftConfigurationNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void viewAllFlights() {
        System.out.println("*** Flight Planning Module: View all Flights ***\n");

        List<FlightEntity> list = flightSessionBeanRemote.retrieveAllFlights();
        for (FlightEntity flight : list) {

            System.out.println("Flight number: " + flight.getFlightNumber());
            System.out.println("Origin Airport: " + flight.getFlightRoute().getOriginAirport().getAirportName() + " ---> Destination Airport: " + flight.getFlightRoute().getDestinationAirport().getAirportName());

            if (flight.getReturnFlight() != null) {
                FlightEntity returnFlight = flight.getReturnFlight();
                System.out.println("\tReturn Flight number: " + returnFlight.getFlightNumber());
                System.out.println("\tOrigin Airport: " + returnFlight.getFlightRoute().getOriginAirport().getAirportName() + " ---> Destination Airport: " + returnFlight.getFlightRoute().getDestinationAirport().getAirportName());
            }
            System.out.println("-----");
        }
    }

    private void viewFlightDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: View Flight Details ***\n");

        System.out.print("Enter Flight Number> ");
        String flightNumber = scanner.nextLine().trim();

        try {
            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);

            System.out.println("Flight number: " + flightNumber);

            AirportEntity originAirport = flight.getFlightRoute().getOriginAirport();
            System.out.println("Origin country, province and city: " + originAirport.getCity() + ", " + originAirport.getProvince() + ", " + originAirport.getCountry());
            System.out.println("Airport name: " + originAirport.getAirportName());

            System.out.println("Cabin Configurations:");

            List<CabinConfigurationEntity> cabinConfigurations = flight.getAircraftConfiguration().getCabinConfigurations();
            for (CabinConfigurationEntity cabinConfiguration : cabinConfigurations) {
                System.out.println("\tCabin type: " + cabinConfiguration.getCabinClass().toString());
                System.out.println("\tNumber of rows: " + cabinConfiguration.getNumberOfRows());
                System.out.println("\tSeating configuration: " + cabinConfiguration.getSeatingConfiguration());
                System.out.println("\tTotal number of seats: " + cabinConfiguration.getMaximumCabinSeatCapacity());
            }
            System.out.print("\n");

            Integer response = 0;
            do {
                System.out.println("1: Update Flight");
                System.out.println("2: Delete Flight");
                System.out.println("3: Back");
                System.out.print("> ");
                response = Integer.parseInt(scanner.nextLine());
                if (response <= 0 || response > 3) {
                    System.out.println("Invalid response! Enter 1-3");
                }
            } while (response <= 0 || response > 3);

            if (response == 1) {
                updateFlight(flight);
            } else if (response == 2) { // delete flight
                try {
                    flightSessionBeanRemote.deleteFlightByFlightNumber(flightNumber);
                    System.out.println("Flight with Flight Number " + flightNumber + " has been deleted!\n");
                } catch (FlightNotFoundException | FlightInUseException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (FlightNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void printFlightRoutes() {
        List<FlightRouteEntity> flightRoutes = flightRouteSessionBeanRemote.retrieveAllFlightRoutes();
        for (FlightRouteEntity flightRoute : flightRoutes) {
            System.out.println("ID: " + flightRoute.getFlightRouteId() + ", Origin: " + flightRoute.getOriginAirport().getIataAirlineCode() + ", Destination: " + flightRoute.getDestinationAirport().getIataAirlineCode());
        }
    }

    private void printAircraftConfigurations() {
        List<AircraftConfigurationEntity> aircraftConfigurations = aircraftConfigurationSessionBeanRemote.retrieveAllAircraftConfiguration();
        for (AircraftConfigurationEntity aircraftConfiguration : aircraftConfigurations) {
            System.out.println("ID: " + aircraftConfiguration.getAircraftConfigurationId() + ", configuration name: " + aircraftConfiguration.getAircraftConfigurationName());
        }
    }

    //called from viewFlightDetails()
    private void updateFlight(FlightEntity flight) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Update Flight ***\n");

        Integer response = 0;
        do {
            System.out.println("1: Update Flight Number");
            System.out.println("2: Update Flight Route");
            System.out.println("3: Update Aircraft Configuration");
            System.out.println("4: Back");
            System.out.print("> ");
            response = Integer.parseInt(scanner.nextLine().trim());
            if (response <= 0 || response > 3) {
                System.out.println("Invalid response! Enter 1-4");
            }
        } while (response <= 0 || response > 4);

        if (response == 1) { // 1: Update Flight Number
            InputMismatchException inputMismatchException = null;
            String newFlightNumber = "ML"; //flight number needs to begin with ML

            do {
                System.out.print("Enter new flight number > ML(_________)");
                newFlightNumber += Long.parseLong(scanner.nextLine());
            } while (inputMismatchException != null);

            String newReturnFlightNumber = "ML";
            if (flight.getReturnFlight() != null) {
                System.out.println("Edit return flight number? Y/N");
                if (scanner.nextLine().equals("Y")) {
                    inputMismatchException = null;
                    do {
                        System.out.print("Enter new return flight number > ML(_________)");
                        newFlightNumber += Long.parseLong(scanner.nextLine());
                    } while (inputMismatchException != null);
                }
            }

            try {
                String flightNumber = flightSessionBeanRemote.updateFlightNumberForFlight(flight, newFlightNumber, newReturnFlightNumber);
                System.out.println("Flight Number successfully updated! New Flight Number: " + flightNumber + ".\n");
            } catch (UpdateFlightFailedException ex) {
                System.out.println(ex.getMessage());
            }
        } else if (response == 2) { // Update Flight Route
            printFlightRoutes();
            System.out.print("Enter new Flight Route Id> ");
            Long flightRouteId = Long.parseLong(scanner.nextLine());
            try {
                String flightNumber = flightSessionBeanRemote.updateFlightRouteForFlight(flight, flightRouteId);
                System.out.println("Flight Route successfully updated for Flight Number " + flightNumber + "! New Flight Route Id: " + flightRouteId + ".\n");
            } catch (UpdateFlightFailedException ex) {
                System.out.println(ex.getMessage());
            }
        } else if (response == 3) { //Update Aircraft Configuration
            printAircraftConfigurations();
            System.out.print("Enter new Aircraft Configuration Id> ");
            Long aircraftConfigurationId = Long.parseLong(scanner.nextLine());
            try {
                String flightNumber = flightSessionBeanRemote.updateAircraftConfigurationForFlight(flight, aircraftConfigurationId);
                System.out.println("Aircraft Configuration successfully updated for Flight Number " + flightNumber + "! New Aircraft Configuration Id: " + aircraftConfigurationId + ".\n");
            } catch (UpdateFlightFailedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void createFlightSchedulePlan() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Create new Flight Schedule Plan ***\n");

        System.out.print("Enter Flight Number> ");
        String flightNumber = scanner.nextLine().trim();

        FlightEntity flight = null;
        try {
            flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
        } catch (FlightNotFoundException ex) {
            System.out.println(ex.getMessage());
            return;
        }
//        List<CabinConfigurationEntity> cabinList = flight.getAircraftConfiguration().getCabinConfigurations();

        boolean isCreatingFare = true;
        String createMoreFare = "";
        List<FareEntity> fares = new ArrayList<>();

        do {
            FareEntity fare = createIndividualFare();
            fares.add(fare);

            do {
                System.out.print("Do you want to create more fares? (Y/N)> ");
                createMoreFare = scanner.nextLine().trim();
                if (!createMoreFare.equals("Y") && !createMoreFare.equals("N")) {
                    System.out.println("Invalid response! Enter Y/N");
                }
                if (createMoreFare.equals("N")) {
                    isCreatingFare = false;
                }
            } while (!createMoreFare.equals("Y") && !createMoreFare.equals("N"));
        } while (isCreatingFare);

        Integer response = 0;
        do {
            System.out.println("Schedule Plan types: 1 - Single, 2 - Multiple, 3 - Recurrent every n days, 4 - Recurrent every week");
            System.out.print("Enter Schedule Plan type> ");
            response = Integer.parseInt(scanner.nextLine());
            if (response <= 0 || response > 4) {
                System.out.println("Invalid response! Enter 1-4");
            }
        } while (response <= 0 || response > 4);

        String returnSchedulePlanResponse = "";
        if (flight.getReturnFlight() != null) {
            do {
                System.out.print("Do you want to create return Flight Schedule Plan? (Y/N)> ");
                returnSchedulePlanResponse = scanner.nextLine().trim();
                if (!returnSchedulePlanResponse.equals("Y") && !returnSchedulePlanResponse.equals("N")) {
                    System.out.println("Invalid response! Enter Y/N");
                }
            } while (!returnSchedulePlanResponse.equals("Y") && !returnSchedulePlanResponse.equals("N"));
        }
        Boolean doCreateReturnFlightSchedule = returnSchedulePlanResponse.equals("Y");

        Integer layoverDuration = null;
        System.out.print("Enter layover duration (Hours) > ");
        layoverDuration = Integer.parseInt(scanner.nextLine());

        if (response == 1 || response == 2) { // single, multiple
            createManualFlightSchedulePlan(fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
        } else if (response == 3) { // recurrent n days
            createRecurrentNDaysFlightSchedulePlan(fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
        } else if (response == 4) { // recurrent weekly
            createRecurrentWeeklyFlightSchedulePlan(fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
        }
    }

    private void createManualFlightSchedulePlan(List<FareEntity> fares, String flightNumber, boolean doCreateReturnFlightSchedule, Integer layoverDuration) {
        Scanner scanner = new Scanner(System.in);

        List<FlightScheduleEntity> flightSchedules = new ArrayList<>();

        Boolean createMoreFlightSchedule = false;
        String createFlightScheduleResponse = "";
        do {
            flightSchedules.add(createFlightSchedule());
            do {
                System.out.print("Do you want to create more Flight Schedules? (Y/N)> ");
                createFlightScheduleResponse = scanner.nextLine().trim();
                if (!createFlightScheduleResponse.equals("Y") && !createFlightScheduleResponse.equals("N")) {
                    System.out.println("Invalid response! Enter Y/N");
                } else {
                    createMoreFlightSchedule = createFlightScheduleResponse.equals("Y");
                }
            } while (!createFlightScheduleResponse.equals("Y") && !createFlightScheduleResponse.equals("N"));
        } while (createMoreFlightSchedule);

        try {
            long id = flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
            System.out.println("Success!" + id);
        } catch (FlightNotFoundException | CreateNewFlightSchedulePlanException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void createRecurrentNDaysFlightSchedulePlan(List<FareEntity> fares, String flightNumber, boolean doCreateReturnFlightSchedule, Integer layoverDuration) {
        Scanner scanner = new Scanner(System.in);

        FlightScheduleEntity baseFlightSchedule = createFlightSchedule();

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateInput = "";
        Date endDate = new Date();
        Boolean dateCheck = false;

        while (!dateCheck) {
            try {
                System.out.print("Enter the end date (DD/MM/YYYY HH:mm:ss)> ");
                dateInput = scanner.nextLine().trim();
                endDate = format.parse(dateInput);
                dateCheck = true;
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!");
            }
        }

        Integer recurrentDaysFrequency = 0;
        do {
            System.out.print("Enter recurrent days frequency> ");
            recurrentDaysFrequency = Integer.parseInt(scanner.nextLine());
            if (recurrentDaysFrequency <= 0) {
                System.out.println("Frequency must be more than 0!");
            }
        } while (recurrentDaysFrequency <= 0);

        try {
            Long id = flightSchedulePlanSessionBeanRemote.createRecurrentNDaysFlightSchedulePlan(endDate, recurrentDaysFrequency, baseFlightSchedule, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
            System.out.println("Success!" + id);
        } catch (FlightNotFoundException | CreateNewFlightSchedulePlanException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void createRecurrentWeeklyFlightSchedulePlan(List<FareEntity> fares, String flightNumber, boolean doCreateReturnFlightSchedule, Integer layoverDuration) {
        Scanner scanner = new Scanner(System.in);

        FlightScheduleEntity baseFlightSchedule = new FlightScheduleEntity();

        System.out.print("Enter the estimated flight duration hour> ");
        Integer estimatedDurationHour = Integer.parseInt(scanner.nextLine());
        baseFlightSchedule.setEstimatedFlightDurationHour(estimatedDurationHour);

        System.out.print("Enter the estimated flight duration minute> ");
        Integer estimatedDurationMinute = Integer.parseInt(scanner.nextLine());
        baseFlightSchedule.setEstimatedFlightDurationMinute(estimatedDurationMinute);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String startDateInput = "";
        String endDateInput = "";
        Date startDate = new Date();
        Date endDate = new Date();
        Boolean startDateCheck = false;
        Boolean endDateCheck = false;

        while (!startDateCheck) {
            try {
                System.out.print("Enter the start date (DD/MM/YYYY> ");
                startDateInput = scanner.nextLine().trim();
                startDate = format.parse(startDateInput);
                startDateCheck = true;
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!");
            }
        }

        while (!endDateCheck) {
            try {
                System.out.print("Enter the end date (DD/MM/YYYY)> ");
                endDateInput = scanner.nextLine().trim();
                endDate = format.parse(endDateInput);
                if (startDate.after(endDate)) {
                    endDateCheck = false;
                } else {
                    endDateCheck = true;
                }
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!");
            }
        }

        Integer startDayOfWeek = 0;
        do {
            System.out.print("Enter start day (1: Sun, 2: Mon, 3: Tues, 4: Wed, 5: Thur, 6: Fri, 7: Sat)> ");
            startDayOfWeek = Integer.parseInt(scanner.nextLine());
            if (startDayOfWeek <= 0) {
                System.out.println("Start day must be more than 0!");
            }
        } while (startDayOfWeek <= 0);

        Integer hour = 0;
        Integer minute = 0;

        do {
            System.out.print("Enter departure hour (24 hour format)> ");
            hour = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter departure minute > ");
            minute = Integer.parseInt(scanner.nextLine());
            if (hour < 0 || hour > 23) {
                System.out.println("Hour must be between 0 and 23!");
            } else if (minute < 0 || minute > 59) {
                System.out.println("Minute must be between 0 and 59!");
            }
        } while ((hour < 0 || hour > 23) && (minute < 0 || minute > 59));

        baseFlightSchedule.setEstimatedFlightDurationHour(hour);
        baseFlightSchedule.setEstimatedFlightDurationMinute(minute);

        try {
            long id = flightSchedulePlanSessionBeanRemote.createRecurrentWeeklyFlightSchedulePlan(startDayOfWeek, hour, minute, startDate, endDate, baseFlightSchedule, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
            System.out.println("Success!" + id);
        } catch (FlightNotFoundException | CreateNewFlightSchedulePlanException ex) {
            System.out.println(ex.getMessage());
        }
    }

    // helper method to create flight schedule for manual flight, and base flight schedule for recurrent n days
    private FlightScheduleEntity createFlightSchedule() {
        Scanner scanner = new Scanner(System.in);
        FlightScheduleEntity flightSchedule = new FlightScheduleEntity();
        System.out.print("\n");
        System.out.println("Create Flight Schedule:");

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateInput = "";
        Date departureDate = new Date();
        Boolean dateCheck = false;

        while (!dateCheck) {
            try {
                System.out.print("Enter the departure date and time (DD/MM/YYYY HH:mm:ss)> ");
                dateInput = scanner.nextLine().trim();
                departureDate = format.parse(dateInput);
                dateCheck = true;
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!");
            }
        }
        flightSchedule.setDepartureDate(departureDate);

        System.out.print("Enter the estimated flight duration hour> ");
        Integer estimatedDurationHour = Integer.parseInt(scanner.nextLine());
        flightSchedule.setEstimatedFlightDurationHour(estimatedDurationHour);

        System.out.print("Enter the estimated flight duration minute> ");
        Integer estimatedDurationMinute = Integer.parseInt(scanner.nextLine());
        flightSchedule.setEstimatedFlightDurationMinute(estimatedDurationMinute);

        return flightSchedule;
    }

    //called from createFlightSchedulePlan() and updateFlightSchedulePlan() case 6.
    private FareEntity createIndividualFare() {
        Scanner scanner = new Scanner(System.in);
        FareEntity fare = new FareEntity();
        System.out.println("===Create Fare Basis Code (Starts With F/J/W/Y)===");

        String cabinClass = "";
        do {
            System.out.print("Enter cabin class (F/J/W/Y)> ");
            cabinClass = scanner.nextLine().trim();
        } while (!cabinClass.equals("F") && !cabinClass.equals("J") && !cabinClass.equals("W") && !cabinClass.equals("Y"));

        String fareBasisCode = cabinClass;
        do {
            System.out.print("Enter fare basis code> ");
            fareBasisCode += scanner.nextLine().trim();
        } while (fareBasisCode.length() <= 0 || fareBasisCode.length() > 6);

        BigDecimal fareAmount = new BigDecimal(0);
        do {
            System.out.print("Enter fare amount> ");
            fareAmount = scanner.nextBigDecimal();
        } while (fareAmount.doubleValue() <= 0);

        fare.setCabinClass(CabinClassEnum.valueOf(cabinClass));
        fare.setFareBasisCode(fareBasisCode);
        fare.setFareAmount(fareAmount);

        System.out.println("Fare added!");
        return fare;
    }

    private void viewAllFlightSchedulePlans() {
        System.out.println("*** Flight Planning Module: View all Flight Schedule Plans ***\n");

        List<FlightSchedulePlanEntity> flightSchedulePlans = flightSchedulePlanSessionBeanRemote.retrieveAllFlightSchedulePlans();

        for (FlightSchedulePlanEntity flightSchedulePlan : flightSchedulePlans) {
            System.out.println("Flight number: " + flightSchedulePlan.getFlight().getFlightNumber());
            System.out.println("\tFlight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
            System.out.println("\tFirst departure date/time: " + flightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
            if (flightSchedulePlan.getReturnFlightSchedulePlan() != null) {
                FlightSchedulePlanEntity returnFlightSchedulePlan = flightSchedulePlan.getReturnFlightSchedulePlan();
                System.out.println("Return Flight number: " + returnFlightSchedulePlan.getFlight().getFlightNumber());
                System.out.println("\tReturn Flight Schedule Plan Id: " + returnFlightSchedulePlan.getFlightSchedulePlanId());
                System.out.println("\tFirst departure date/time: " + returnFlightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
            }
            System.out.print("-----------------------\n");
        }
    }

    private void viewFlightSchedulePlanDetails() {
        System.out.println("*** Flight Planning Module: View Flight Schedule Plan Details ***\n");
        Scanner scanner = new Scanner(System.in);

        List<FlightSchedulePlanEntity> flightSchedulePlans = flightSchedulePlanSessionBeanRemote.retrieveAllFlightSchedulePlans();

        for (FlightSchedulePlanEntity flightSchedulePlan : flightSchedulePlans) {
            System.out.print("Flight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId() + ", ");
            System.out.print("Flight number: " + flightSchedulePlan.getFlight().getFlightNumber() + ", ");
            System.out.println("First departure date/time: " + flightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
            if (flightSchedulePlan.getReturnFlightSchedulePlan() != null) {
                FlightSchedulePlanEntity returnFlightSchedulePlan = flightSchedulePlan.getReturnFlightSchedulePlan();
                System.out.print("Return Flight Schedule Plan Id: " + returnFlightSchedulePlan.getFlightSchedulePlanId() + ", ");
                System.out.print("Return Flight number: " + returnFlightSchedulePlan.getFlight().getFlightNumber() + ", ");
                System.out.println("First departure date/time: " + returnFlightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
            }
            System.out.print("-----------------------\n");
        }

        System.out.print("Enter Flight Schedule Plan Id> ");
        Long flightSchedulePlanId = scanner.nextLong();

        try {
            FlightSchedulePlanEntity flightSchedulePlan = flightSchedulePlanSessionBeanRemote.retrieveFlightSchedulePlanById(flightSchedulePlanId);
            FlightEntity flight = flightSchedulePlan.getFlight();

            System.out.println("Flight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
            System.out.println("Flight number: " + flight.getFlightNumber());
            System.out.println("Origin Airport: " + flight.getFlightRoute().getOriginAirport().getAirportName() + "---> Destination Airport: " + flight.getFlightRoute().getDestinationAirport().getAirportName());

            System.out.println("Fare(s):");
            List<FareEntity> faresList = flightSchedulePlan.getFares();
            for (FareEntity fare : faresList) {
                System.out.println("\tFare Id: " + fare.getFareId());
                System.out.println("\t\tCabin Class: " + fare.getCabinClass().toString());
                System.out.println("\t\tFare Basis Code: " + fare.getFareBasisCode());
                System.out.println("\t\tFare Amount: " + fare.getFareAmount());
            }

            List<FlightScheduleEntity> flightSchedulesList = flightSchedulePlan.getFlightSchedules();

            System.out.println("Flight Schedule(s):");
            for (FlightScheduleEntity flightSchedule : flightSchedulesList) {
                System.out.println("\tFlight Schedule Id: " + flightSchedule.getFlightScheduleId());
                System.out.println("\t\tDeparture Date: " + flightSchedule.getDepartureDate());
                System.out.println("\t\tEstimated Flight Duration: " + flightSchedule.getEstimatedFlightDurationHour() + " hrs " + (flightSchedule.getEstimatedFlightDurationMinute() == null ? "" : flightSchedule.getEstimatedFlightDurationMinute() + " mins"));
                //view estimated arival datetime?
                //view flight schedule type?
                //view end date (for recurrent)?
            }
            System.out.print("\n");

            Integer response = 0;
            do {
                System.out.println("1: Update Flight Schedule Plan");
                System.out.println("2: Delete Flight Schedule Plan");
                System.out.println("3: Back");
                System.out.print("> ");
                response = scanner.nextInt();
                if (response <= 0 || response > 3) {
                    System.out.println("Invalid response! Enter 1-3");
                }
            } while (response <= 0 || response > 3);

            if (response == 1) {
                updateFlightSchedulePlanDetails(flightSchedulePlan);
            } else if (response == 2) {
                try {
                    flightSchedulePlanSessionBeanRemote.deleteFlightSchedulePlanById(flightSchedulePlanId);
                    System.out.println("Flight Schedule Plan has been siccesssfully deleted!");
                } catch (FlightSchedulePlanNotFoundException | FlightSchedulePlanInUseException ex) {
                    System.out.println(ex.getMessage());
                }
            }

        } catch (FlightSchedulePlanNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //called from viewFlightSchedulePlanDetails()
    private void updateFlightSchedulePlanDetails(FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Update Flight Schedule Plan ***\n");

        Integer response = 0;
        do {
            System.out.println("1: Remove flight schedule(s) (single/multiple flight schedule plan)");
            System.out.println("2: Add flight schedule(s) (single/multiple flight schedule plan)");
            System.out.println("3: Update flight schedule departure time and flight duration (single/multiple flight schedule plan)");
            System.out.println("4: Update flight schedule plan recurrent frequency or end date (RecurrentNDays)");
            System.out.println("5: Update day of week for weekly recurrent (Weekly)");
            System.out.println("6: Update start date / end date for weekly recurrent (Weekly)");
            System.out.println("7: Update fare amount in flight schedule plan");
            System.out.println("8: Add fare to flight schedule plan");
            System.out.println("9: Remove fare from flight schedule plan");

            System.out.print("> ");
            response = scanner.nextInt();
            if (response <= 0 || response > 9) {
                System.out.println("Invalid response! Enter 1-7");
            }
        } while (response <= 0 || response > 9);

        switch (response) {
            case (1):
                if (!flightSchedulePlan.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.MANUAL)) {
                    System.out.println("Unsupported operation for selected flight schedule plan!");
                    return;
                }
                updateRemoveFlightSchedules(flightSchedulePlan);
                break;
            case (2):
                if (!flightSchedulePlan.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.MANUAL)) {
                    System.out.println("Unsupported operation for selected flight schedule plan!");
                    return;
                }
                updateAddFlightSchedules(flightSchedulePlan);
                break;
            case (3):
                if (!flightSchedulePlan.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.MANUAL)) {
                    System.out.println("Unsupported operation for selected flight schedule plan!");
                    return;
                }
                updateFlightScheduleDetails(flightSchedulePlan);
                break;
            case (4):
                if (!flightSchedulePlan.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.RECURRENTNDAYS)) {
                    System.out.println("Unsupported operation for selected flight schedule plan!");
                    return;
                }
                updateRecurrentParametersForFlightSchedulePlan(flightSchedulePlan);
                break;
            case (5):
                if (!flightSchedulePlan.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.RECURRENTWEEKLY)) {
                    System.out.println("Unsupported operation for selected flight schedule plan!");
                    return;
                }
                Integer startDayOfWeek = 0;

                do {
                    System.out.println("Enter new start day of week (1 for Sunday, 7 for Saturday)> ");
                    startDayOfWeek = Integer.parseInt(scanner.nextLine().trim());
                    if (startDayOfWeek <= 0 || startDayOfWeek > 7) {
                        System.out.println("Invalid response! Start day of week must be between 1-7!");
                    }
                } while (startDayOfWeek <= 0 || startDayOfWeek > 7);

                try {
                    flightSchedulePlanSessionBeanRemote.updateRecurrentWeeklyFlightSchedulePlanDayOfWeek(flightSchedulePlan.getFlightSchedulePlanId(), startDayOfWeek);
                    System.out.println("FLIGHT SCHEDULE PLAN START DAY OF WEEK UPDATED SUCCESSFULLY!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;
            case (6):
                if (!flightSchedulePlan.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.RECURRENTWEEKLY)) {
                    System.out.println("Unsupported operation for selected flight schedule plan!");
                    return;
                }
                String inputStartDate = "";
                String inputEndDate = "";
                Boolean dateStartCheck = false;
                Boolean dateEndCheck = false;
                Date newWeeklyStartDate = null;
                Date newWeeklyEndDate = null;

                do {
                    try {
                        System.out.print("Enter new start date> ");

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        inputStartDate = scanner.nextLine().trim();
                        newWeeklyStartDate = format.parse(inputStartDate);
                        dateStartCheck = true;
                    } catch (ParseException ex) {
                        System.out.println("Wrong format for date!");
                    }
                } while (!dateStartCheck);

                do {
                    try {
                        System.out.print("Enter new end date> ");

                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        inputEndDate = scanner.nextLine().trim();
                        newWeeklyEndDate = format.parse(inputEndDate);
                        dateEndCheck = true;
                    } catch (ParseException ex) {
                        System.out.println("Wrong format for date!");
                    }
                } while (!dateEndCheck);
                break;
            case (7):
                updateFareAmount(flightSchedulePlan);
                break;
            case (8):
                addFare(flightSchedulePlan);
                break;
            case (9):
                removeFare(flightSchedulePlan);
                break;
            default:
                break;
        }
    }

    private void updateRemoveFlightSchedules(FlightSchedulePlanEntity flightSchedulePlan) {
        for (FlightScheduleEntity flightSchedule : flightSchedulePlan.getFlightSchedules()) {
            System.out.println("ID: " + flightSchedule.getFlightScheduleId() + "\n\tDeparture date: " + flightSchedule.getDepartureDate());
        }

        Scanner scanner = new Scanner(System.in);

        HashSet<Long> flightScheduleIds = new HashSet<>();
        String addMore = "Y";
        do {
            System.out.print("Enter Flight Schedule Id to remove> ");
            flightScheduleIds.add(scanner.nextLong());

            do {
                System.out.print("Do you want to delete more Flight Schedules? (Y/N)> ");
                addMore = scanner.nextLine().trim();
                if (!addMore.equals("Y") && !addMore.equals("N")) {
                    System.out.println("Invalid response! Enter Y/N");
                }
            } while (!addMore.equals("Y") && !addMore.equals("N"));
        } while (addMore.equals("Y"));

        try {
            flightSchedulePlanSessionBeanRemote.updateRemoveFlightScheduleFromManualFlightSchedulePlan(flightSchedulePlan.getFlightSchedulePlanId(), flightScheduleIds);
            System.out.println("Flight Schedules successfully removed from Flight Schedule Plan !");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void updateAddFlightSchedules(FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);

        List<FlightScheduleEntity> newFlightSchedules = new ArrayList<>();

        String createMoreFlightSchedules = "Y";
        do {
            newFlightSchedules.add(this.createFlightSchedule());

            do {
                System.out.print("Do you want to add more Flight Schedules? (Y/N)> ");
                createMoreFlightSchedules = scanner.nextLine().trim();
                if (!createMoreFlightSchedules.equals("Y") && !createMoreFlightSchedules.equals("N")) {
                    System.out.println("Invalid response! Enter Y/N");
                }
            } while (!createMoreFlightSchedules.equals("Y") && !createMoreFlightSchedules.equals("N"));
        } while (createMoreFlightSchedules.equals("Y"));

        String doCreateReturnFlightSchedule = "N";

        do {
            System.out.print("Do you want to create return Flight Schedules? (Y/N)> ");
            doCreateReturnFlightSchedule = scanner.nextLine().trim();
            if (!doCreateReturnFlightSchedule.equals("Y") && !doCreateReturnFlightSchedule.equals("N")) {
                System.out.println("Invalid response! Enter Y/N");
            }
        } while (!doCreateReturnFlightSchedule.equals("Y") && !doCreateReturnFlightSchedule.equals("N"));

        try {
            flightSchedulePlanSessionBeanRemote.updateAddFlightScheduleToManualFlightSchedulePlan(flightSchedulePlan.getFlightSchedulePlanId(), newFlightSchedules, doCreateReturnFlightSchedule.equals("Y"));
            System.out.println("Flight Schedules are successfully added into Flight Schedule Plan !");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void updateFlightScheduleDetails(FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);

        for (FlightScheduleEntity flightSchedule : flightSchedulePlan.getFlightSchedules()) {
            System.out.println("ID: " + flightSchedule.getFlightScheduleId() + "\n\tDeparture date: " + flightSchedule.getDepartureDate());
        }

        List<FlightScheduleEntity> updatedFlightSchedules = new ArrayList<>();
        String updateMoreFlightSchedules = "N";

        String departureDate = "";
        Boolean checkDate = false;
        Date newDepartureDate = null;

        Integer estimatedFlightHour = 0;
        Integer estimatedFlightMin = 0;

        do {
            System.out.print("Enter Flight Schedule Id to update> ");
            Long flightScheduleId = Long.parseLong(scanner.nextLine());

            try {
                FlightScheduleEntity flightSchedule = flightScheduleSessionBeanRemote.retrieveFlightScheduleById(flightScheduleId);
                do {
                    System.out.print("Enter new departure date for Flight Schedule (DD/MM/YYYY)> ");
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        departureDate = scanner.nextLine().trim();
                        newDepartureDate = format.parse(departureDate);
                        checkDate = true;
                    } catch (ParseException ex) {
                        System.out.println("Wrong format for date!");
                    }
                } while (!checkDate);

                do {
                    System.out.print("Enter new flight duration hour> ");
                    estimatedFlightHour = Integer.parseInt(scanner.nextLine());
                    if (estimatedFlightHour <= 0) {
                        System.out.println("Invalid response! Flight duration hour must be greater than 0.");
                    }
                } while (estimatedFlightHour <= 0);

                do {
                    System.out.print("Enter new flight duration min> ");
                    estimatedFlightMin = Integer.parseInt(scanner.nextLine());
                    if (estimatedFlightMin <= 0) {
                        System.out.println("Invalid response! Flight duration hour must be greater than 0.");
                    }
                } while (estimatedFlightMin <= 0);

                flightSchedule.setDepartureDate(newDepartureDate);
                flightSchedule.setEstimatedFlightDurationHour(estimatedFlightHour);
                flightSchedule.setEstimatedFlightDurationMinute(estimatedFlightMin);
                updatedFlightSchedules.add(flightSchedule);

                do {
                    System.out.println("Do you want to update more flight schedules? (Y/N)");
                    updateMoreFlightSchedules = scanner.nextLine().trim();
                    if (!updateMoreFlightSchedules.equals("Y") && !updateMoreFlightSchedules.equals("N")) {
                        System.out.println("Invalied response! Please enter Y/N");
                    }
                } while (!updateMoreFlightSchedules.equals("Y") && !updateMoreFlightSchedules.equals("N"));
            } catch (FlightScheduleNotFoundException ex) {
                System.out.println(ex.getMessage());
            }
        } while (updateMoreFlightSchedules.equals("Y"));

        try {
            flightSchedulePlanSessionBeanRemote.updateFlightScheduleDetailForManualFlightSchedulePlan(flightSchedulePlan.getFlightSchedulePlanId(), updatedFlightSchedules);
            System.out.println("Flight schedules successfully updated!");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void updateRecurrentParametersForFlightSchedulePlan(FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);

        String date = "";
        Boolean dateCheck = false;
        Date newEndDate = null;
        Integer newRecurrentFrequency = null;

        do {
            try {
                System.out.print("Enter new end date for recurrent Flight Schedule Plan (DD/MM/YYYY), or \"-\" if not applicable> ");
                date = scanner.nextLine().trim();
                if (date.equals("-")) { //if user does not want to change end date
                    newEndDate = null;
                    break;
                }

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                date = scanner.nextLine().trim();
                newEndDate = format.parse(date);
                dateCheck = true;
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!");
            }
        } while (!dateCheck);

        do {
            System.out.print("Enter new recurrent frequency, or \"-\" if not applicable> ");
            newRecurrentFrequency = Integer.parseInt(scanner.nextLine());
            if (newRecurrentFrequency.equals("-")) {
                newRecurrentFrequency = null;
                break;
            }
            if (newRecurrentFrequency <= 0) {
                System.out.println("Invalid response! Recurrent frequency must be more than 0.");
            }
        } while (newRecurrentFrequency <= 0);

        try {
            flightSchedulePlanSessionBeanRemote.updateRecurrentNDaysFlightSchedulePlanParameters(flightSchedulePlan.getFlightSchedulePlanId(), newEndDate, newRecurrentFrequency);
            System.out.println("Flight Schedule Plan successfully updated!");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void updateFareAmount(FlightSchedulePlanEntity flightSchedulePlan) {
        for (FareEntity fare : flightSchedulePlan.getFares()) {
            System.out.println("Fare ID: " + fare.getFareId() + ", amount: " + fare.getFareAmount() + ", fare basis code: " + fare.getFareBasisCode());
        }

        Scanner scanner = new Scanner(System.in);

        List<FareEntity> updatedFareAmounts = new ArrayList<>();
        String updateMoreFares = "N";

        do {
            System.out.print("Enter Fare Id to update> ");
            Long fareId = Long.parseLong(scanner.nextLine());

            try {
                Double fareAmount = 0.0;

                do {
                    System.out.print("Enter new Fare amount for Flight Schedule Plan> ");
                    fareAmount = Double.parseDouble(scanner.nextLine());
                    if (fareAmount <= 0) {
                        System.out.println("Fare amount must be greater than zero!");
                    }
                } while (fareAmount <= 0);

                updatedFareAmounts.add(fareEntitySessionBeanRemote.updateFareAmount(flightSchedulePlan.getFlightSchedulePlanId(), fareId, new BigDecimal(fareAmount)));

                do {
                    System.out.println("Do you want to update more fare amounts? (Y/N)");
                    updateMoreFares = scanner.nextLine().trim();
                    if (!updateMoreFares.equals("Y") && !updateMoreFares.equals("N")) {
                        System.out.println("Invalied response! Please enter Y/N");
                    }
                } while (!updateMoreFares.equals("Y") && !updateMoreFares.equals("N"));

            } catch (FlightSchedulePlanNotFoundException | UpdateFlightSchedulePlanFailedException ex) {
                System.out.println(ex.getMessage());
            }
        } while (updateMoreFares.equals("Y"));

        try {
            flightSchedulePlanSessionBeanRemote.updateFareAmountInFlightSchedulePlan(flightSchedulePlan.getFlightSchedulePlanId(), updatedFareAmounts);
            System.out.println("Fare amounts successfully added!");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void addFare(FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);

        List<FareEntity> addFares = new ArrayList<>();
        String addMoreFares = "N";

        do {
            System.out.println("Create new Fare:");

            addFares.add(createIndividualFare());

            do {
                System.out.println("Do you want to add more fare? (Y/N)");
                addMoreFares = scanner.nextLine().trim();
                if (!addMoreFares.equals("Y") && !addMoreFares.equals("N")) {
                    System.out.println("Invalied response! Please enter Y/N");
                }
            } while (!addMoreFares.equals("Y") && !addMoreFares.equals("N"));
        } while (addMoreFares.equals("Y"));

        try {
            flightSchedulePlanSessionBeanRemote.updateAddFareToFlightSchedulePlan(flightSchedulePlan.getFlightSchedulePlanId(), addFares);
            System.out.println("Fares successfully added!");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void removeFare(FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);
        for (FareEntity fare : flightSchedulePlan.getFares()) {
            System.out.println("Fare ID: " + fare.getFareId() + ", amount: " + fare.getFareAmount() + ", fare basis code: " + fare.getFareBasisCode());
        }
        HashSet<Long> removeFares = new HashSet<>();
        String removeMoreFares = "N";

        do {
            System.out.print("Enter Fare Id for fare to be removed> ");

            removeFares.add(Long.parseLong(scanner.nextLine()));

            do {
                System.out.println("Do you want to remove more fares? (Y/N)");
                removeMoreFares = scanner.nextLine().trim();
                if (!removeMoreFares.equals("Y") && !removeMoreFares.equals("N")) {
                    System.out.println("Invalied response! Please enter Y/N");
                }
            } while (!removeMoreFares.equals("Y") && !removeMoreFares.equals("N"));
        } while (removeMoreFares.equals("Y"));

        try {
            flightSchedulePlanSessionBeanRemote.updateRemoveFareFromFlightSchedulePlan(flightSchedulePlan.getFlightSchedulePlanId(), removeFares);
            System.out.println("Fares successfully removed!");
        } catch (UpdateFlightSchedulePlanFailedException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
