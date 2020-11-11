///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package flightreservationsystemmanagementclient;
//
//import ejb.session.stateless.FareEntitySessionBeanRemote;
//import ejb.session.stateless.FlightRouteSessionBeanRemote;
//import ejb.session.stateless.FlightSchedulePlanSessionBeanRemote;
//import ejb.session.stateless.FlightScheduleSessionBeanRemote;
//import ejb.session.stateless.FlightSessionBeanRemote;
//import entity.AirportEntity;
//import entity.CabinConfigurationEntity;
//import entity.FareEntity;
//import entity.FlightEntity;
//import entity.FlightRouteEntity;
//import entity.FlightScheduleEntity;
//import entity.FlightSchedulePlanEntity;
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Random;
//import java.util.Scanner;
//import javax.ejb.EJB;
//import util.enumeration.CabinClassEnum;
//import util.exception.AircraftConfigurationNotFoundException;
//import util.exception.CreateNewFlightException;
//import util.exception.CreateNewFlightSchedulePlanException;
//import util.exception.FlightInUseException;
//import util.exception.FlightNotFoundException;
//import util.exception.FlightRouteNotFoundException;
//import util.exception.FlightScheduleNotFoundException;
//import util.exception.FlightSchedulePlanInUseException;
//import util.exception.FlightSchedulePlanNotFoundException;
//import util.exception.UpdateFlightFailedException;
//import util.exception.UpdateFlightSchedulePlanFailedException;
//
///**
// *
// * @author kiyon
// */
//public class FlightOperationModule {
//
//    @EJB
//    private FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;
//    @EJB
//    private FlightSessionBeanRemote flightSessionBeanRemote;
//    @EJB
//    private FlightSchedulePlanSessionBeanRemote flightSchedulePlanSessionBeanRemote;
//    @EJB
//    private FareEntitySessionBeanRemote fareEntitySessionBeanRemote;
//    @EJB
//    private FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote;
//
//    public FlightOperationModule() {
//    }
//
//    public FlightOperationModule(FlightRouteSessionBeanRemote flightRouteSessionBeanRemote,
//            FlightSessionBeanRemote flightSessionBeanRemote,
//            FlightSchedulePlanSessionBeanRemote flightSchedulePlanSessionBeanRemote,
//            FareEntitySessionBeanRemote fareEntitySessionBeanRemote,
//            FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote) {
//        this.flightRouteSessionBeanRemote = flightRouteSessionBeanRemote;
//        this.flightSessionBeanRemote = flightSessionBeanRemote;
//        this.flightSchedulePlanSessionBeanRemote = flightSchedulePlanSessionBeanRemote;
//        this.fareEntitySessionBeanRemote = fareEntitySessionBeanRemote;
//        this.flightScheduleSessionBeanRemote = flightScheduleSessionBeanRemote;
//    }
//
//    public void flightOperationMenu() {
//        Scanner scanner = new Scanner(System.in);
//        Integer response = 0;
//
//        while (true) {
//            System.out.println("*** Flight Management System: Flight Operation Module ***\n");
//            System.out.println("1: Create new Flight");
//            System.out.println("2: View All Flights");
//            System.out.println("3: View Flight Details\n");
//
//            System.out.println("4: Create Flight Schedule Plan");
//            System.out.println("5: View All Flight Schedule Plans");
//            System.out.println("6: View Flight Schedule Plan Details\n");
//            System.out.println("7: Logout\n");
//            response = 0;
//
//            while (response < 1 || response > 7) {
//                System.out.print("> ");
//
//                response = scanner.nextInt();
//
//                if (response == 1) {
//                    createFlight();
//                } else if (response == 2) {
//                    viewAllFlights();
//                } else if (response == 3) {
//                    viewFlightDetails();
//                } else if (response == 4) {
//                    createFlightSchedulePlan();
//                } else if (response == 5) {
//                    viewAllFlightSchedulePlans();
//                } else if (response == 6) {
//                    viewFlightSchedulePlanDetails();
//                } else if (response == 7) {
//                    break;
//                } else {
//                    System.out.println("Invalid option, please try again!\n");
//                }
//            }
//
//            if (response == 7) {
//                break;
//            }
//        }
//    }
//
//    private void createFlight() {
//        Scanner scanner = new Scanner(System.in);
//
//        FlightEntity flight = new FlightEntity();
//
//        System.out.println("*** Flight Planning Module: Create new Flight ***\n");
//
//        try {
//            System.out.print("Enter Flight Route Id> ");
//            Long flightRouteId = scanner.nextLong();
//            scanner.nextLine(); // skip to next line
//
//            FlightRouteEntity flightRoute = flightRouteSessionBeanRemote.retrieveFlightRouteById(flightRouteId);
//
//            //String originAirportCode = flightRoute.getOriginAirport().getIataAirlineCode();
//            String flightNumber = "ML" + generateRandomNumber(); //flight number needs to begin with ML
//            flight.setFlightNumber(flightNumber);
//
//            System.out.print("Enter Aircraft Configuration Id> ");
//            Long aircraftConfigurationId = scanner.nextLong();
//
//            String response = "N";
//
//            // only prompt client to create return flight if flight route has return flight route
//            if (flightRoute.getReturnFlightRoute() != null) {
//                do {
//                    System.out.println("Do you want to create a complementary return flight? (Y/N)> ");
//                    response = scanner.nextLine().trim();
//                    if (!response.equals("Y") && !response.equals("N")) {
//                        System.out.println("Invalid response! Input Y/N.");
//                    }
//                } while (!response.equals("Y") && !response.equals("N"));
//            }
//
//            Boolean createReturnFlight = response.equals("Y");
//
//            String returnFlightNumber = "";
//            if (createReturnFlight) {
//                //returnFlightNumber = flightRoute.getDestinationAirport().getIataAirlineCode() + generateRandomNumber();
//                returnFlightNumber = "ML" + generateRandomNumber();
//                while (returnFlightNumber.equals(flightNumber)) { // auto generated return flight number should not be same as origin flight number
//                    returnFlightNumber = "ML" + generateRandomNumber();
//                }
//            }
//
//            String flightId = flightSessionBeanRemote.createNewFlight(flight, flightRouteId, aircraftConfigurationId, createReturnFlight, returnFlightNumber);
//
//            System.out.println("Flight successfully created!");
//            // print both flight number since they are auto generated by the system
//            System.out.println("Flight Number: " + flightId);
//            if (returnFlightNumber.length() > 0) { // print return flight number if any 
//                System.out.println("Return Flight Number: " + returnFlightNumber);
//            }
//            System.out.print("\n");
//
//        } catch (FlightRouteNotFoundException | CreateNewFlightException | AircraftConfigurationNotFoundException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//
//    private String generateRandomNumber() {
//        Long randomNumber = 0L;
//
//        for (int i = 0; i < 3; i++) {
//            randomNumber *= 10;
//            if (i == 0) { //first digit cannot be 0
//                randomNumber += new Random().nextInt(9) + 1;
//            } else {
//                randomNumber += new Random().nextInt(10);
//            }
//        }
//        return randomNumber.toString();
//    }
//
//    private void viewAllFlights() {
//        System.out.println("*** Flight Planning Module: View all Flights ***\n");
//
//        List<FlightEntity> list = flightSessionBeanRemote.retrieveAllFlights();
//        for (FlightEntity flight : list) {
//
//            System.out.println("Flight number: " + flight.getFlightNumber());
//            System.out.println("Origin Airport: " + flight.getFlightRoute().getOriginAirport().getAirportName() + " ---> Destination Airport: " + flight.getFlightRoute().getDestinationAirport().getAirportName());
//
//            if (flight.getReturnFlight() != null) {
//                FlightEntity returnFlight = flight.getReturnFlight();
//                System.out.println("\tReturn Flight number: " + returnFlight.getFlightNumber());
//                System.out.println("\tOrigin Airport: " + returnFlight.getFlightRoute().getOriginAirport().getAirportName() + " ---> Destination Airport: " + returnFlight.getFlightRoute().getDestinationAirport().getAirportName());
//            }
//            System.out.println("-----");
//        }
//    }
//
//    private void viewFlightDetails() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("*** Flight Planning Module: View Flight Details ***\n");
//
//        System.out.print("Enter Flight Number> ");
//        String flightNumber = scanner.nextLine().trim();
//
//        try {
//            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
//
//            System.out.println("Flight number: " + flightNumber);
//
//            AirportEntity originAirport = flight.getFlightRoute().getOriginAirport();
//            System.out.println("Origin country, province and city: " + originAirport.getCity() + ", " + originAirport.getProvince() + ", " + originAirport.getCountry());
//            System.out.println("Airport name: " + originAirport.getAirportName());
//
//            System.out.println("Cabin Configurations:");
//
//            List<CabinConfigurationEntity> cabinConfigurations = flight.getAircraftConfiguration().getCabinConfigurations();
//            for (CabinConfigurationEntity cabinConfiguration : cabinConfigurations) {
//                System.out.println("\tCabin type: " + cabinConfiguration.getCabinClass().toString());
//                System.out.println("\tNumber of rows: " + cabinConfiguration.getNumberOfRows());
//                System.out.println("\tSeating configuration: " + cabinConfiguration.getSeatingConfiguration());
//                System.out.println("\tTotal number of seats: " + cabinConfiguration.getMaximumCabinSeatCapacity());
//            }
//            System.out.print("\n");
//
//            Integer response = 0;
//            do {
//                System.out.println("1: Update Flight");
//                System.out.println("2: Delete Flight");
//                System.out.println("3: Back");
//                System.out.print("> ");
//                response = scanner.nextInt();
//                if (response <= 0 || response > 3) {
//                    System.out.println("Invalid response! Enter 1-3");
//                }
//            } while (response <= 0 || response > 3);
//
//            if (response == 1) {
//                updateFlight(flight);
//            } else if (response == 2) {
//                deleteFlight(flightNumber);
//            }
//        } catch (FlightNotFoundException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//
//    //called from viewFlightDetails()
//    private void updateFlight(FlightEntity flight) {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("*** Flight Planning Module: Update Flight ***\n");
//
//        Integer response = 0;
//        do {
//            System.out.println("1: Update Flight Number");
//            System.out.println("2: Update Flight Route");
//            System.out.println("3: Update Aircraft Configuration");
//            System.out.println("4: Back");
//            System.out.print("> ");
//            response = scanner.nextInt();
//            if (response <= 0 || response > 3) {
//                System.out.println("Invalid response! Enter 1-4");
//            }
//        } while (response <= 0 || response > 4);
//
//        if (response == 1) {
//            String newFlightNumber = readFlightNumber(false);
//            String newReturnFlightNumber = null;
//
//            if (flight.getReturnFlight() != null) {
//                newReturnFlightNumber = readFlightNumber(true);
//            }
//            try {
//                String flightNumber = flightSessionBeanRemote.updateFlightNumberForFlight(flight, newFlightNumber, newReturnFlightNumber);
//                System.out.println("Flight Number successfully updated! New Flight Number: " + flightNumber + ".\n");
//            } catch (UpdateFlightFailedException ex) {
//                System.out.println(ex.getMessage());
//            }
//        } else if (response == 2) {
//            System.out.print("Enter new Flight Route Id> ");
//            Long flightRouteId = scanner.nextLong();
//            try {
//                String flightNumber = flightSessionBeanRemote.updateFlightRouteForFlight(flight, flightRouteId);
//                System.out.println("Flight Route successfully updated for Flight Number " + flightNumber + "! New Flight Route Id: " + flightRouteId + ".\n");
//            } catch (UpdateFlightFailedException ex) {
//                System.out.println(ex.getMessage());
//            }
//        } else if (response == 3) {
//            System.out.print("Enter new Aircraft Configuration Id> ");
//            Long aircraftConfigurationId = scanner.nextLong();
//            try {
//                String flightNumber = flightSessionBeanRemote.updateAircraftConfigurationForFlight(flight, aircraftConfigurationId);
//                System.out.println("Aircraft Configuration successfully updated for Flight Number " + flightNumber + "! New Aircraft Configuration Id: " + aircraftConfigurationId + ".\n");
//            } catch (UpdateFlightFailedException ex) {
//                System.out.println(ex.getMessage());
//            }
//        }
//    }
//
//    //helper method for update flight
//    private String readFlightNumber(Boolean isReturnFlight) {
//        Scanner scanner = new Scanner(System.in);
//        String newFlightNumber = "";
//        do {
//            if (!isReturnFlight) {
//                System.out.print("Enter new Flight Number> ");
//            } else {
//                System.out.print("Enter new Return Flight Number> ");
//            }
//            newFlightNumber = scanner.nextLine().trim();
//            if (newFlightNumber.length() < 3 || newFlightNumber.length() > 10) {
//                System.out.println("Invalid Flight Number length! Length must be more than 0 and less than 10");
//            } else if (!newFlightNumber.substring(0, 2).equals("ML")) {
//                System.out.println("Flight Number must begin with \"ML\"!");
//            }
//        } while (newFlightNumber.length() < 3 || newFlightNumber.length() > 10 || !newFlightNumber.substring(0, 2).equals("ML"));
//        return newFlightNumber;
//    }
//
//    //called from viewFlightDetails()
//    private void deleteFlight(String flightNumber) {
//        try {
//            flightSessionBeanRemote.deleteFlightByFlightNumber(flightNumber);
//            System.out.println("Flight with Flight Number " + flightNumber + " has been deleted!\n");
//        } catch (FlightNotFoundException | FlightInUseException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//
//    private void createFlightSchedulePlan() {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("*** Flight Planning Module: Create new Flight Schedule Plan ***\n");
//
//        System.out.print("Enter Flight Number> ");
//        String flightNumber = scanner.nextLine().trim();
//
//        try {
//            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
//            List<CabinConfigurationEntity> cabinList = flight.getAircraftConfiguration().getCabinConfigurations();
//
//            HashMap<String, HashSet<FareEntity>> fareList = new HashMap<>();
//
//            boolean isCreatingFare = true;
//            String createMoreFare = "";
//
//            do {
//                FareEntity fare = createIndividualFare();
//
//                if (fareList.containsKey(fare.getCabinClass().toString())) {
//                    fareList.get(fare.getCabinClass().toString()).add(fare);
//                } else {
//                    HashSet<FareEntity> list = new HashSet<>();
//                    list.add(fare);
//                    fareList.put(fare.getCabinClass().toString(), list);
//                }
//
//                do {
//                    System.out.print("Do you want to create more fares? (Y/N)> ");
//                    createMoreFare = scanner.nextLine().trim();
//                    if (!createMoreFare.equals("Y") && !createMoreFare.equals("N")) {
//                        System.out.println("Invalid response! Enter Y/N");
//                    }
//                    if (createMoreFare.equals("N")) {
//                        isCreatingFare = false;
//                        if (fareList.keySet().size() != cabinList.size()) {
//                            isCreatingFare = true;
//                            System.out.println("One or more cabin classes do not have a fare basis code!");
//                            // no fare basis code ?
//                        }
//                    }
//                } while (!createMoreFare.equals("Y") && !createMoreFare.equals("N"));
//            } while (isCreatingFare);
//
//            List<FareEntity> fares = new ArrayList<>();
//            for (HashSet<FareEntity> set : fareList.values()) {
//                fares.addAll(set);
//            }
//
//            Integer response = 0;
//            do {
//                System.out.println("Schedule Plan types: 1 - Single, 2 - Multiple, 3 - Recurrent every n days, 4 - Recurrent every week");
//                System.out.print("Enter Schedule Plan type> ");
//                response = scanner.nextInt();
//                if (response <= 0 || response > 4) {
//                    System.out.println("Invalid response! Enter 1-4");
//                }
//            } while (response <= 0 || response > 4);
//            scanner.nextLine();
//
//            String returnSchedulePlanResponse = "";
//            Integer layoverDuration = null;
//            // prompt user only if flight has a return flight
//            if (flight.getReturnFlight() != null) {
//                do {
//                    System.out.print("Do you want to create return Flight Schedule Plan? (Y/N)> ");
//                    returnSchedulePlanResponse = scanner.nextLine().trim();
//                    if (!returnSchedulePlanResponse.equals("Y") && !returnSchedulePlanResponse.equals("N")) {
//                        System.out.println("Invalid response! Enter Y/N");
//                    }
//                } while (!returnSchedulePlanResponse.equals("Y") && !returnSchedulePlanResponse.equals("N"));
//
//                System.out.print("Enter layover duration (Hours) > ");
//                layoverDuration = Integer.parseInt(scanner.nextLine());
//            }
//
//            Boolean doCreateReturnFlightSchedule = returnSchedulePlanResponse.equals("Y");
//
//            if (response == 1 || response == 2) {
//                List<FlightScheduleEntity> flightSchedules = new ArrayList<>();
//                if (response == 1) {
//                    flightSchedules.add(createFlightSchedule());
//                } else {
//                    Boolean createMoreFlightSchedule = true;
//                    String createFlightScheduleResponse = "";
//                    do {
//                        flightSchedules.add(createFlightSchedule());
//                        do {
//                            System.out.print("Do you want to create more Flight Schedules? (Y/N)> ");
//                            createFlightScheduleResponse = scanner.nextLine().trim();
//                            if (!createFlightScheduleResponse.equals("Y") && !createFlightScheduleResponse.equals("N")) {
//                                System.out.println("Invalid response! Enter Y/N");
//                            } else {
//                                createMoreFlightSchedule = createFlightScheduleResponse.equals("Y");
//                            }
//                        } while (!createFlightScheduleResponse.equals("Y") && !createFlightScheduleResponse.equals("N"));
//                    } while (createMoreFlightSchedule);
//                }
//
//                flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
//            } else if (response == 3 || response == 4) {
//                FlightScheduleEntity baseFlightSchedule = createFlightSchedule();
//
//                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                String dateInput = "";
//                Date endDate = new Date();
//                Boolean dateCheck = false;
//
//                while (!dateCheck) {
//                    try {
//                        System.out.print("Enter the end date (DD/MM/YYYY HH:mm:ss)> ");
//                        dateInput = scanner.nextLine().trim();
//                        endDate = format.parse(dateInput);
//                        dateCheck = true;
//                    } catch (ParseException ex) {
//                        System.out.println("Wrong format for date!");
//                    }
//                }
//
//                Integer recurrentDaysFrequency = 7;
//                if (response == 3) {
//                    do {
//                        System.out.print("Enter recurrent days frequency> ");
//                        recurrentDaysFrequency = scanner.nextInt();
//                        if (recurrentDaysFrequency <= 0) {
//                            System.out.println("Frequency must be more than 0!");
//                        }
//                    } while (recurrentDaysFrequency <= 0);
//                }
//
//                flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(endDate, recurrentDaysFrequency, baseFlightSchedule, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
//            }
//
//            System.out.println("Flight schedule plan successfully created!");
//
//        } catch (FlightNotFoundException | CreateNewFlightSchedulePlanException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//
//    //called from createFlightSchedulePlan()
//    private FlightScheduleEntity createFlightSchedule() {
//        Scanner scanner = new Scanner(System.in);
//        FlightScheduleEntity flightSchedule = new FlightScheduleEntity();
//        System.out.print("\n");
//        System.out.println("Create Flight Schedule:");
//
//        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//        String dateInput = "";
//        Date departureDate = new Date();
//        Boolean dateCheck = false;
//
//        while (!dateCheck) {
//            try {
//                System.out.print("Enter the departure date and time (DD/MM/YYYY HH:mm:ss)> ");
//                dateInput = scanner.nextLine().trim();
//                departureDate = format.parse(dateInput);
//                dateCheck = true;
//            } catch (ParseException ex) {
//                System.out.println("Wrong format for date!");
//            }
//        }
//        flightSchedule.setDepartureDate(departureDate);
//
//        System.out.print("Enter the estimated flight duration hour> ");
//        Integer estimatedDurationHour = Integer.parseInt(scanner.nextLine());
//        flightSchedule.setEstimatedFlightDurationHour(estimatedDurationHour);
//
//        System.out.print("Enter the estimated flight duration minute> ");
//        Integer estimatedDurationMinute = Integer.parseInt(scanner.nextLine());
//        flightSchedule.setEstimatedFlightDurationMinute(estimatedDurationMinute);
//
//        return flightSchedule;
//    }
//
//    //called from createFlightSchedulePlan() and updateFlightSchedulePlan() case 6.
//    private FareEntity createIndividualFare() {
//        Scanner scanner = new Scanner(System.in);
//        FareEntity fare = new FareEntity();
//        System.out.println("Create Fare Basis Code (Starts With F/J/W/Y)");
//
//        String cabinClass = "";
//        do {
//            System.out.print("Enter cabin class (F/J/W/Y)> ");
//            cabinClass = scanner.nextLine().trim();
//        } while (!cabinClass.equals("F") && !cabinClass.equals("J") && !cabinClass.equals("W") && !cabinClass.equals("Y"));
//
//        String fareBasisCode = cabinClass;
//        do {
//            System.out.print("Enter fare basis code> ");
//            fareBasisCode = scanner.nextLine().trim();
//        } while (fareBasisCode.length() <= 0 || fareBasisCode.length() > 6);
//
//        BigDecimal fareAmount = new BigDecimal(0);
//        do {
//            System.out.print("Enter fare amount> ");
//            fareAmount = scanner.nextBigDecimal();
//        } while (fareAmount.doubleValue() <= 0);
//
//        fare.setCabinClass(CabinClassEnum.valueOf(cabinClass));
//        fare.setFareBasisCode(fareBasisCode);
//        fare.setFareAmount(fareAmount);
//
//        return fare;
//    }
//
//    private void viewAllFlightSchedulePlans() {
//        System.out.println("*** Flight Planning Module: View all Flight Schedule Plans ***\n");
//
//        List<FlightSchedulePlanEntity> flightSchedulePlans = flightSchedulePlanSessionBeanRemote.retrieveAllFlightSchedulePlans();
//
//        for (FlightSchedulePlanEntity flightSchedulePlan : flightSchedulePlans) {
//            System.out.println("Flight number: " + flightSchedulePlan.getFlight().getFlightNumber());
//            System.out.println("\tFlight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
//            System.out.println("\tFirst departure date/time: " + flightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
//            if (flightSchedulePlan.getReturnFlightSchedulePlan() != null) {
//                System.out.println("Return Flight number: " + flightSchedulePlan.getFlight().getFlightNumber());
//                System.out.println("\tReturn Flight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
//                System.out.println("\tFirst departure date/time: " + flightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
//            }
//            System.out.print("\n");
//        }
//    }
//
//    private void viewFlightSchedulePlanDetails() {
//        System.out.println("*** Flight Planning Module: View Flight Schedule Plan Details ***\n");
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.print("Enter Flight Schedule Plan Id> ");
//        Long flightSchedulePlanId = scanner.nextLong();
//
//        try {
//            FlightSchedulePlanEntity flightSchedulePlan = flightSchedulePlanSessionBeanRemote.retrieveFlightSchedulePlanById(flightSchedulePlanId);
//            FlightEntity flight = flightSchedulePlan.getFlight();
//
//            System.out.println("Flight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
//            System.out.println("Flight number: " + flight.getFlightNumber());
//            System.out.println("Origin Airport: " + flight.getFlightRoute().getOriginAirport().getAirportName() + "---> Destination Airport: " + flight.getFlightRoute().getDestinationAirport().getAirportName());
//
//            System.out.println("Fare(s):");
//            List<FareEntity> faresList = flightSchedulePlan.getFares();
//            for (FareEntity fare : faresList) {
//                System.out.println("\tFare Id: " + fare.getFareId());
//                System.out.println("\t\tCabin Class: " + fare.getCabinClass().toString());
//                System.out.println("\t\tFare Basis Code: " + fare.getFareBasisCode());
//                System.out.println("\t\tFare Amount: " + fare.getFareAmount());
//            }
//
//            List<FlightScheduleEntity> flightSchedulesList = flightSchedulePlan.getFlightSchedules();
//
//            System.out.println("Flight Schedule(s):");
//            for (FlightScheduleEntity flightSchedule : flightSchedulesList) {
//                System.out.println("\tFlight Schedule Id: " + flightSchedule.getFlightScheduleId());
//                System.out.println("\t\tDeparture Date: " + flightSchedule.getDepartureDate());
//                System.out.println("\t\tEstimated Flight Duration: " + flightSchedule.getEstimatedFlightDurationHour());
//                //view estimated arival datetime?
//                //view flight schedule type?
//                //view end date (for recurrent)?
//            }
//            System.out.print("\n");
//
//            Integer response = 0;
//            do {
//                System.out.println("1: Update Flight Schedule Plan");
//                System.out.println("2: Delete Flight Schedule Plan");
//                System.out.println("3: Back");
//                System.out.print("> ");
//                response = scanner.nextInt();
//                if (response <= 0 || response > 3) {
//                    System.out.println("Invalid response! Enter 1-3");
//                }
//            } while (response <= 0 || response > 3);
//
//            if (response == 1) {
//                updateFlightSchedulePlanDetails(flightSchedulePlanId);
//            } else if (response == 2) {
//                deleteFlightSchedulePlan(flightSchedulePlanId);
//            }
//
//        } catch (FlightSchedulePlanNotFoundException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//
//    //called from viewFlightSchedulePlanDetails()
//    private void updateFlightSchedulePlanDetails(Long flightSchedulePlanId) {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("*** Flight Planning Module: Update Flight Schedule Plan ***\n");
//
//        Integer response = 0;
//        do {
//            System.out.println("1: Remove Flight Schedule(s) from Flight Schedule Plan");
//            System.out.println("2: Add Flight Schedule(s) to Flight Schedule Plan");
//            System.out.println("3: Update Flight Schedule Plan recurrent frequency or end date");
//            System.out.println("4: Update Flight Schedule departure time and flight duration (for non recurrent flight schedules)");
//            System.out.println("5: Update fare amount in Flight Schedule Plan");
//            System.out.println("6: Add fare to Flight Schedule Plan");
//            System.out.println("7: Remove fare from Flight Schedule Plan");
//
//            System.out.print("> ");
//            response = scanner.nextInt();
//            if (response <= 0 || response > 7) {
//                System.out.println("Invalid response! Enter 1-7");
//            }
//        } while (response <= 0 || response > 7);
//
//        switch (response) {
//            case (1):
//                HashSet<Long> flightScheduleIds = new HashSet<>();
//                String addMore = "Y";
//                do {
//                    System.out.print("Enter Flight Schedule Id to remove> ");
//                    flightScheduleIds.add(scanner.nextLong());
//
//                    do {
//                        System.out.print("Do you want to delete more Flight Schedules? (Y/N)> ");
//                        addMore = scanner.nextLine().trim();
//                        if (!addMore.equals("Y") && !addMore.equals("N")) {
//                            System.out.println("Invalid response! Enter Y/N");
//                        }
//                    } while (!addMore.equals("Y") && !addMore.equals("N"));
//                } while (addMore.equals("Y"));
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateRemoveFlightScheduleFromFlightSchedulePlan(flightSchedulePlanId, flightScheduleIds);
//                    System.out.println("Flight Schedules successfully removed from Flight Schedule Plan " + flightSchedulePlanId);
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//
//            case (2):
//                List<FlightScheduleEntity> newFlightSchedules = new ArrayList<>();
//
//                String createMoreFlightSchedules = "Y";
//                do {
//                    newFlightSchedules.add(this.createFlightSchedule());
//
//                    do {
//                        System.out.print("Do you want to add more Flight Schedules? (Y/N)> ");
//                        createMoreFlightSchedules = scanner.nextLine().trim();
//                        if (!createMoreFlightSchedules.equals("Y") && !createMoreFlightSchedules.equals("N")) {
//                            System.out.println("Invalid response! Enter Y/N");
//                        }
//                    } while (!createMoreFlightSchedules.equals("Y") && !createMoreFlightSchedules.equals("N"));
//                } while (createMoreFlightSchedules.equals("Y"));
//
//                String doCreateReturnFlightSchedule = "N";
//
//                do {
//                    System.out.print("Do you want to create return Flight Schedules? (Y/N)> ");
//                    doCreateReturnFlightSchedule = scanner.nextLine().trim();
//                    if (!doCreateReturnFlightSchedule.equals("Y") && !doCreateReturnFlightSchedule.equals("N")) {
//                        System.out.println("Invalid response! Enter Y/N");
//                    }
//                } while (!doCreateReturnFlightSchedule.equals("Y") && !doCreateReturnFlightSchedule.equals("N"));
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateAddFlightScheduleToFlightSchedulePlan(flightSchedulePlanId, newFlightSchedules, doCreateReturnFlightSchedule.equals("Y"));
//                    System.out.println("Flight Schedules are successfully added into Flight Schedule Plan " + flightSchedulePlanId);
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//
//            case (3):
//                String date = "";
//                Boolean dateCheck = false;
//                Date newEndDate = null;
//                Integer newRecurrentFrequency = null;
//
//                do {
//                    try {
//                        System.out.print("Enter new end date for recurrent Flight Schedule Plan (DD/MM/YYYY), or \"-\" if not applicable> ");
//                        date = scanner.nextLine().trim();
//                        if (date.equals("-")) { //if user does not want to change end date
//                            newEndDate = null;
//                            break;
//                        }
//
//                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//                        date = scanner.nextLine().trim();
//                        newEndDate = format.parse(date);
//                        dateCheck = true;
//                    } catch (ParseException ex) {
//                        System.out.println("Wrong format for date!");
//                    }
//                } while (!dateCheck);
//
//                do {
//                    System.out.print("Enter new recurrent frequency, or \"-\" if not applicable> ");
//                    newRecurrentFrequency = scanner.nextInt();
//                    if (newRecurrentFrequency.equals("-")) {
//                        newRecurrentFrequency = null;
//                        break;
//                    }
//                    if (newRecurrentFrequency <= 0) {
//                        System.out.println("Invalid response! Recurrent frequency must be more than 0.");
//                    }
//                } while (newRecurrentFrequency <= 0);
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateRecurrentFlightSchedulePlanParameters(flightSchedulePlanId, newEndDate, newRecurrentFrequency);
//                    System.out.println("Flight Schedule Plan successfully updated!");
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//
//            case (4):
//                List<FlightScheduleEntity> updatedFlightSchedules = new ArrayList<>();
//                String updateMoreFlightSchedules = "N";
//
//                String departureDate = "";
//                Boolean checkDate = false;
//                Date newDepartureDate = null;
//
//                Integer estimatedFlightDuration = 0;
//
//                do {
//                    System.out.print("Enter Flight Schedule Id to update> ");
//                    Long flightScheduleId = scanner.nextLong();
//
//                    try {
//                        FlightScheduleEntity flightSchedule = flightScheduleSessionBeanRemote.retrieveFlightScheduleById(flightScheduleId);
//                        do {
//                            System.out.print("Enter new departure date for Flight Schedule (DD/MM/YYYY)> ");
//                            try {
//                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
//                                departureDate = scanner.nextLine().trim();
//                                newDepartureDate = format.parse(departureDate);
//                                checkDate = true;
//                            } catch (ParseException ex) {
//                                System.out.println("Wrong format for date!");
//                            }
//                        } while (!checkDate);
//
//                        do {
//                            System.out.print("Enter new flight duration> ");
//                            estimatedFlightDuration = scanner.nextInt();
//                            if (estimatedFlightDuration <= 0) {
//                                System.out.println("Invalid response! Flight duration must be greater than 0.");
//                            }
//                        } while (estimatedFlightDuration <= 0);
//
//                        flightSchedule.setDepartureDate(newDepartureDate);
//                        flightSchedule.setEstimatedFlightDurationHour(estimatedFlightDuration);
//                        updatedFlightSchedules.add(flightSchedule);
//
//                        do {
//                            System.out.println("Do you want to update more flight schedules? (Y/N)");
//                            updateMoreFlightSchedules = scanner.nextLine().trim();
//                            if (!updateMoreFlightSchedules.equals("Y") && !updateMoreFlightSchedules.equals("N")) {
//                                System.out.println("Invalied response! Please enter Y/N");
//                            }
//                        } while (!updateMoreFlightSchedules.equals("Y") && !updateMoreFlightSchedules.equals("N"));
//                    } catch (FlightScheduleNotFoundException ex) {
//                        System.out.println(ex.getMessage());
//                    }
//                } while (updateMoreFlightSchedules.equals("Y"));
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateFlightScheduleDetailForNonRecurrentFlightSchedulePlan(flightSchedulePlanId, updatedFlightSchedules);
//                    System.out.println("Flight Schedules successfully added!");
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//
//            case (5):
//                List<FareEntity> updatedFareAmounts = new ArrayList<>();
//                String updateMoreFares = "N";
//
//                do {
//                    System.out.print("Enter Fare Id to update> ");
//                    Long fareId = scanner.nextLong();
//
//                    try {
//                        Double fareAmount = 0.0;
//
//                        do {
//                            System.out.print("Enter new Fare amount for Flight Schedule Plan> ");
//                            fareAmount = scanner.nextDouble();
//                            if (fareAmount <= 0) {
//                                System.out.println("Fare amount must be greater than zero!");
//                            }
//                        } while (fareAmount <= 0);
//
//                        updatedFareAmounts.add(fareEntitySessionBeanRemote.updateFareAmount(flightSchedulePlanId, fareId, new BigDecimal(fareAmount)));
//
//                        do {
//                            System.out.println("Do you want to update more fare amounts? (Y/N)");
//                            updateMoreFares = scanner.nextLine().trim();
//                            if (!updateMoreFares.equals("Y") && !updateMoreFares.equals("N")) {
//                                System.out.println("Invalied response! Please enter Y/N");
//                            }
//                        } while (!updateMoreFares.equals("Y") && !updateMoreFares.equals("N"));
//                    } catch (FlightSchedulePlanNotFoundException | UpdateFlightSchedulePlanFailedException ex) {
//                        System.out.println(ex.getMessage());
//                    }
//                } while (updateMoreFares.equals("Y"));
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateFareAmountInFlightSchedulePlan(flightSchedulePlanId, updatedFareAmounts);
//                    System.out.println("Fare amounts successfully added!");
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//
//            case (6):
//                List<FareEntity> addFares = new ArrayList<>();
//                String addMoreFares = "N";
//
//                do {
//                    System.out.println("Create new Fare:");
//
//                    addFares.add(createIndividualFare());
//
//                    do {
//                        System.out.println("Do you want to add more fare? (Y/N)");
//                        addMoreFares = scanner.nextLine().trim();
//                        if (!addMoreFares.equals("Y") && !addMoreFares.equals("N")) {
//                            System.out.println("Invalied response! Please enter Y/N");
//                        }
//                    } while (!addMoreFares.equals("Y") && !addMoreFares.equals("N"));
//                } while (addMoreFares.equals("Y"));
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateAddFareToFlightSchedulePlan(flightSchedulePlanId, addFares);
//                    System.out.println("Fares successfully added!");
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//
//            case (7):
//                HashSet<Long> removeFares = new HashSet<>();
//                String removeMoreFares = "N";
//
//                do {
//                    System.out.print("Enter Fare Id for fare to be removed> ");
//
//                    removeFares.add(scanner.nextLong());
//
//                    do {
//                        System.out.println("Do you want to remove more fares? (Y/N)");
//                        removeMoreFares = scanner.nextLine().trim();
//                        if (!removeMoreFares.equals("Y") && !removeMoreFares.equals("N")) {
//                            System.out.println("Invalied response! Please enter Y/N");
//                        }
//                    } while (!removeMoreFares.equals("Y") && !removeMoreFares.equals("N"));
//                } while (removeMoreFares.equals("Y"));
//
//                try {
//                    flightSchedulePlanSessionBeanRemote.updateRemoveFareFromFlightSchedulePlan(flightSchedulePlanId, removeFares);
//                    System.out.println("Fares successfully removed!");
//                } catch (UpdateFlightSchedulePlanFailedException ex) {
//                    System.out.println(ex.getMessage());
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    //called from viewFlightSchedulePlanDetails()
//    private void deleteFlightSchedulePlan(Long flightSchedulePlanId) {
//        try {
//            flightSchedulePlanSessionBeanRemote.deleteFlightSchedulePlanById(flightSchedulePlanId);
//            System.out.println("Flight Schedule Plan has been siccesssfully deleted!");
//        } catch (FlightSchedulePlanNotFoundException | FlightSchedulePlanInUseException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
//}
