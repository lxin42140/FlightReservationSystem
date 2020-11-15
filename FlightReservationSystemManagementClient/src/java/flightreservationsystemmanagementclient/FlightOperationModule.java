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
import java.awt.BorderLayout;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import javax.ejb.EJB;
import util.enumeration.CabinClassEnum;
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
//        @EJB
//    private AirportEntitySessionBeanRemote airportEntitySessionBeanRemote;

    public FlightOperationModule() {
    }

    public FlightOperationModule(FlightRouteSessionBeanRemote flightRouteSessionBeanRemote,
            FlightSessionBeanRemote flightSessionBeanRemote,
            FlightSchedulePlanSessionBeanRemote flightSchedulePlanSessionBeanRemote,
            FareEntitySessionBeanRemote fareEntitySessionBeanRemote,
            FlightScheduleSessionBeanRemote flightScheduleSessionBeanRemote,
            AirportEntitySessionBeanRemote airportEntitySessionBeanRemote,
            AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote) {
        this.flightRouteSessionBeanRemote = flightRouteSessionBeanRemote;
        this.flightSessionBeanRemote = flightSessionBeanRemote;
        this.flightSchedulePlanSessionBeanRemote = flightSchedulePlanSessionBeanRemote;
        this.fareEntitySessionBeanRemote = fareEntitySessionBeanRemote;
        this.flightScheduleSessionBeanRemote = flightScheduleSessionBeanRemote;
        this.aircraftConfigurationSessionBeanRemote = aircraftConfigurationSessionBeanRemote;
//        this.airportEntitySessionBeanRemote= airportEntitySessionBeanRemote;
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

        this.printAllFlightRoutes();

        try {
            System.out.print("Enter Flight Route Id> ");
            Long flightRouteId = scanner.nextLong();
            scanner.nextLine(); // skip to next line

            FlightRouteEntity flightRoute = flightRouteSessionBeanRemote.retrieveFlightRouteById(flightRouteId);

            String flightNumber; //flight number needs to begin with ML
            do {
                System.out.print("Enter flight number starting with ML> ");
                flightNumber = scanner.nextLine().trim();
                if (!flightNumber.substring(0, 2).equals("ML")) {
                    System.out.println("Invalid flight number! Flight number must start with \"ML\"!");
                }
            } while (!flightNumber.substring(0, 2).equals("ML"));

            flight.setFlightNumber(flightNumber);

            this.printAllAircraftConfiguration();
            System.out.print("Enter Aircraft Configuration Id> ");
            Long aircraftConfigurationId = scanner.nextLong();

            String response = "N";

            // only prompt client to create return flight if flight route has return flight route
            if (flightRoute.getReturnFlightRoute() != null) {
                scanner.nextLine();
                do {
                    System.out.print("Do you want to create a complementary return flight? (Y/N)> ");
                    response = scanner.nextLine().trim();
                    if (!response.equals("Y") && !response.equals("N")) {
                        System.out.println("Invalid response! Input Y/N.");
                    }
                } while (!response.equals("Y") && !response.equals("N"));
            }

            Boolean createReturnFlight = response.equals("Y");

            String returnFlightNumber = ""; //flight number needs to begin with ML
            if (createReturnFlight) {
                do {
                    System.out.print("Enter flight number starting with ML> ");
                    returnFlightNumber = scanner.nextLine().trim();
                    if (!returnFlightNumber.substring(0, 2).equals("ML")) {
                        System.out.println("Invalid flight number! Flight number must start with \"ML\"!");
                    }
                } while (!returnFlightNumber.substring(0, 2).equals("ML"));
            }

            String flightId = flightSessionBeanRemote.createNewFlight(flight, flightRouteId, aircraftConfigurationId, createReturnFlight, returnFlightNumber);

            if (returnFlightNumber.length() > 0) { // print return flight number if any 
                System.out.println("FLIGHT SUCCESSFULLY CREATED! Flight Number: " + flightNumber + ", return flight number: " + returnFlightNumber + "\n");
            } else {
                System.out.println("FLIGHT SUCCESSFULLY CREATED! Flight Number: " + flightNumber + "\n");
            }
            System.out.print("\n");

        } catch (FlightRouteNotFoundException | CreateNewFlightException | AircraftConfigurationNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
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
            AirportEntity destinationAirport = flight.getFlightRoute().getDestinationAirport();
            System.out.println("Origin country, province and city: " + originAirport.getCity() + ", " + originAirport.getProvince() + ", " + originAirport.getCountry());
            System.out.println("Airport name: " + originAirport.getAirportName());
            System.out.println("Destination country, province and city: " + destinationAirport.getCity() + ", " + destinationAirport.getProvince() + ", " + destinationAirport.getCountry());
            System.out.println("Airport name: " + destinationAirport.getAirportName());

            System.out.println("Cabin Configurations:");

            List<CabinConfigurationEntity> cabinConfigurations = flight.getAircraftConfiguration().getCabinConfigurations();
            for (CabinConfigurationEntity cabinConfiguration : cabinConfigurations) {
                System.out.println("\tCabin type: " + cabinConfiguration.getCabinClass().toString());
                System.out.println("\tNumber of rows: " + cabinConfiguration.getNumberOfRows());
                System.out.println("\tSeating configuration: " + cabinConfiguration.getSeatingConfiguration());
                System.out.println("\tTotal number of seats: " + cabinConfiguration.getMaximumCabinSeatCapacity() + "\n");
            }
            System.out.print("\n");

            Integer response = 0;
            do {
                System.out.println("1: Update Flight");
                System.out.println("2: Delete Flight");
                System.out.println("3: Back");
                System.out.print("> ");
                response = scanner.nextInt();
                if (response <= 0 || response > 3) {
                    System.out.println("Invalid response! Enter 1-3");
                }
            } while (response <= 0 || response > 3);

            if (response == 1) {
                updateFlight(flight);
            } else if (response == 2) {
                deleteFlight(flightNumber);
            }
        } catch (FlightNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
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
            response = scanner.nextInt();
            if (response <= 0 || response > 3) {
                System.out.println("Invalid response! Enter 1-4");
            }
        } while (response <= 0 || response > 4);

        if (response == 1) {
            String newFlightNumber = readFlightNumber(false);
            String newReturnFlightNumber = null;

            if (flight.getReturnFlight() != null) {
                newReturnFlightNumber = readFlightNumber(true);
            }
            try {
                String flightNumber = flightSessionBeanRemote.updateFlightNumberForFlight(flight, newFlightNumber, newReturnFlightNumber);
                System.out.println("FLIGHT NUMBER SUCCESSFULLY UPDATED! New Flight Number: " + flightNumber + ".\n");
            } catch (UpdateFlightFailedException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        } else if (response == 2) {
            this.printAllFlightRoutes();
            System.out.print("Enter new Flight Route Id> ");
            Long flightRouteId = scanner.nextLong();
            try {
                String flightNumber = flightSessionBeanRemote.updateFlightRouteForFlight(flight, flightRouteId);
                System.out.println("FLIGHT ROUTE SUCCESSFULLT UPDATED FOR FLIGHT NUMBER " + flightNumber + "! New Flight Route Id: " + flightRouteId + ".\n");
            } catch (UpdateFlightFailedException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        } else if (response == 3) {
            this.printAllAircraftConfiguration();
            System.out.print("Enter new Aircraft Configuration Id> ");
            Long aircraftConfigurationId = scanner.nextLong();
            try {
                String flightNumber = flightSessionBeanRemote.updateAircraftConfigurationForFlight(flight, aircraftConfigurationId);
                System.out.println("AIRCRAFT CONFIGURATION SUCCESSFULLY UPDATED FOR FLIGHT NUMBER " + flightNumber + "! New Aircraft Configuration Id: " + aircraftConfigurationId + ".\n");
            } catch (UpdateFlightFailedException ex) {
                System.out.println(ex.getMessage() + "\n");
            }
        }
    }

    //helper method for update flight
    private String readFlightNumber(Boolean isReturnFlight) {
        Scanner scanner = new Scanner(System.in);
        String newFlightNumber = "";
        do {
            if (!isReturnFlight) {
                System.out.print("Enter new Flight Number> ");
            } else {
                System.out.print("Enter new Return Flight Number> ");
            }
            newFlightNumber = scanner.nextLine().trim();
            if (newFlightNumber.length() < 3 || newFlightNumber.length() > 10) {
                System.out.println("Invalid Flight Number length! Length must be more than 0 and less than 10");
            } else if (!newFlightNumber.substring(0, 2).equals("ML")) {
                System.out.println("Flight Number must begin with \"ML\"!");
            }
        } while (newFlightNumber.length() < 3 || newFlightNumber.length() > 10 || !newFlightNumber.substring(0, 2).equals("ML"));
        return newFlightNumber;
    }

    //called from viewFlightDetails()
    private void deleteFlight(String flightNumber) {
        try {
            flightSessionBeanRemote.deleteFlightByFlightNumber(flightNumber);
            System.out.println("FLIGHT NUMBER " + flightNumber + " HAS BEEN SUCCESFULLY DELETED!\n");
        } catch (FlightNotFoundException | FlightInUseException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void createFlightSchedulePlan() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Create new Flight Schedule Plan ***\n");

        System.out.print("Enter Flight Number> ");
        String flightNumber = scanner.nextLine().trim();

        try {
            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
            List<CabinConfigurationEntity> cabinList = flight.getAircraftConfiguration().getCabinConfigurations();

            List<FlightScheduleEntity> flightSchedules = new ArrayList<>();

            Date endDate = new Date();
            Integer recurrentDaysFrequency = 0;
            FlightScheduleEntity baseFlightSchedule = null;

            Integer startDayOfWeek = 0;
            Integer hour = 0;
            Integer minute = 0;
            Date startDate = new Date();

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

            // prompt user only if flight has a return flight
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

            if (response == 1) {
                flightSchedules.add(createFlightSchedule());
            } else if (response == 2) {
                Boolean createMoreFlightSchedule = true;
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

            } else if (response == 3) {
                baseFlightSchedule = createFlightSchedule();

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                String dateInput = "";
                Boolean dateCheck = false;

                while (!dateCheck) {
                    try {
                        System.out.print("Enter the end date (DD/MM/YYYY)> ");
                        dateInput = scanner.nextLine().trim();
                        endDate = format.parse(dateInput);
                        dateCheck = true;
                    } catch (ParseException ex) {
                        System.out.println("Wrong format for date!");
                    }
                }

                do {
                    System.out.print("Enter recurrent days frequency> ");
                    recurrentDaysFrequency = Integer.parseInt(scanner.nextLine());
                    if (recurrentDaysFrequency <= 0) {
                        System.out.println("Frequency must be more than 0!");
                    }
                } while (recurrentDaysFrequency <= 0);
            } else if (response == 4) {
                baseFlightSchedule = new FlightScheduleEntity();

                do {
                    System.out.print("Enter start day (1 for Sunday, 7 for Saturday)> ");
                    startDayOfWeek = Integer.parseInt(scanner.nextLine());
                    if (startDayOfWeek <= 0 || startDayOfWeek > 7) {
                        System.out.println("Start day must be more than 0!");
                    }
                } while (startDayOfWeek <= 0 || startDayOfWeek > 7);

                do {
                    System.out.print("Enter departure hour > ");
                    hour = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter departure minute > ");
                    minute = Integer.parseInt(scanner.nextLine());
                    if (hour < 0 || hour > 23) {
                        System.out.println("Hour must be between 0 and 23!");
                    } else if (minute < 0 || minute > 59) {
                        System.out.println("Minute must be between 0 and 59!");
                    }
                } while ((hour < 0 || hour > 23) || (minute < 0 || minute > 59));

//                baseFlightSchedule.setEstimatedFlightDurationHour(hour);
//                baseFlightSchedule.setEstimatedFlightDurationMinute(minute);
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                String startDateInput = "";
                String endDateInput = "";
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
                        endDateCheck = true;
                    } catch (ParseException ex) {
                        System.out.println("Wrong format for date!");
                    }
                }

                System.out.print("Enter the estimated flight duration hour> ");
                Integer estimatedDurationHour = Integer.parseInt(scanner.nextLine());
                baseFlightSchedule.setEstimatedFlightDurationHour(estimatedDurationHour);

                System.out.print("Enter the estimated flight duration minute> ");
                Integer estimatedDurationMinute = Integer.parseInt(scanner.nextLine());
                baseFlightSchedule.setEstimatedFlightDurationMinute(estimatedDurationMinute);
            }

            Integer layoverDuration = 0;
            if (flight.getReturnFlight() != null) {
                do {
                    System.out.print("Enter layover duration (Hours)> ");
                    layoverDuration = Integer.parseInt(scanner.nextLine());
                    if (layoverDuration <= 0) {
                        System.out.println("Invalid response! Layover duration must be more than 0!");
                    }
                } while (layoverDuration <= 0);
            }

//            HashMap<String, HashSet<FareEntity>> fareList = new HashMap<>();
            List<FareEntity> fares = new ArrayList<>();
            boolean isCreatingFare = true;
            String createMoreFare = "";

            do {
                FareEntity fare = createIndividualFare();
                fares.add(fare);
//                if (fareList.containsKey(fare.getCabinClass().toString())) {
//                    fareList.get(fare.getCabinClass().toString()).add(fare);
//                } else {
//                    HashSet<FareEntity> list = new HashSet<>();
//                    list.add(fare);
//                    fareList.put(fare.getCabinClass().toString(), list);
//                }
                do {
                    System.out.print("Do you want to create more fares? (Y/N)> ");
                    createMoreFare = scanner.nextLine().trim();
                    if (!createMoreFare.equals("Y") && !createMoreFare.equals("N")) {
                        System.out.println("Invalid response! Enter Y/N");
                    }
                    if (createMoreFare.equals("N")) {
                        isCreatingFare = false;
//                        if (fareList.keySet().size() != cabinList.size()) {
//                            isCreatingFare = true;
//                            System.out.println("One or more cabin classes do not have a fare basis code!");
//                            // no fare basis code ?
//                        }
                    }
                } while (!createMoreFare.equals("Y") && !createMoreFare.equals("N"));
            } while (isCreatingFare);

//            List<FareEntity> fares = new ArrayList<>();
//            for (HashSet<FareEntity> set : fareList.values()) {
//                fares.addAll(set);
//            }

            Long flightSchedulePlanId = 0l;
            if (response == 1 || response == 2) {
                flightSchedulePlanId = flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
            } else if (response == 3) {
                flightSchedulePlanId = flightSchedulePlanSessionBeanRemote.createRecurrentNDaysFlightSchedulePlan(endDate, recurrentDaysFrequency, baseFlightSchedule, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
            } else if (response == 4) {
                System.out.println(startDayOfWeek);
                System.out.println(hour);

                System.out.println(minute);

                System.out.println(startDate.toString());

                System.out.println(endDate.toString());

                System.out.println(baseFlightSchedule);
                System.out.println(fares.size());
                System.out.println(flightNumber);
                System.out.println(doCreateReturnFlightSchedule);

                System.out.println(layoverDuration);

                flightSchedulePlanId = flightSchedulePlanSessionBeanRemote.createRecurrentWeeklyFlightSchedulePlan(startDayOfWeek, hour, minute, startDate, endDate, baseFlightSchedule, fares, flightNumber, doCreateReturnFlightSchedule, layoverDuration);
            }
            System.out.println("FLIGHT SCHEDULE SUCCESSFULLY CREATED! Flight Schedule Plan Id: " + flightSchedulePlanId + ".\n");
        } catch (FlightNotFoundException | CreateNewFlightSchedulePlanException ex) {
            System.out.println(ex.getMessage() + "\n");
        }

    }

//called from createFlightSchedulePlan()
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
        String basisCode = "";
        do {
            System.out.print("Enter fare basis code (without the prefix)> ");
            basisCode = scanner.nextLine().trim();
            if (basisCode.length() <= 0 || basisCode.length() > 5) {
                System.out.println("Invalid response! Fare basis code must have length between 0 and 5!");
            } else {
                fareBasisCode += basisCode;
            }
        } while (basisCode.length() <= 0 || basisCode.length() > 5);

        BigDecimal fareAmount = new BigDecimal(0);
        do {
            System.out.print("Enter fare amount> ");
//            fareAmount = scanner.nextBigDecimal();
            fareAmount = BigDecimal.valueOf(Double.parseDouble(scanner.nextLine().trim()));
        } while (fareAmount.doubleValue() <= 0);

        fare.setCabinClass(CabinClassEnum.valueOf(cabinClass));
        fare.setFareBasisCode(fareBasisCode);
        fare.setFareAmount(fareAmount);

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
            System.out.print("\n");
        }
    }

    private void viewFlightSchedulePlanDetails() {
        System.out.println("*** Flight Planning Module: View Flight Schedule Plan Details ***\n");
        Scanner scanner = new Scanner(System.in);

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
                updateFlightSchedulePlanDetails(flightSchedulePlanId, flightSchedulePlan);
            } else if (response == 2) {
                deleteFlightSchedulePlan(flightSchedulePlanId);
            }

        } catch (FlightSchedulePlanNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    //called from viewFlightSchedulePlanDetails()
    private void updateFlightSchedulePlanDetails(Long flightSchedulePlanId, FlightSchedulePlanEntity flightSchedulePlan) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Update Flight Schedule Plan ***\n");

        Integer response = 0;
        do {
            System.out.println("1: Remove Flight Schedule(s) from Flight Schedule Plan");
            System.out.println("2: Add Flight Schedule(s) to Flight Schedule Plan");
            System.out.println("3: Update Flight Schedule Plan recurrent frequency or end date");
            System.out.println("4: Update Flight Schedule departure time and flight duration (for non recurrent flight schedules)");
            System.out.println("5: Update fare amount in Flight Schedule Plan");
            System.out.println("6: Add fare to Flight Schedule Plan");
            System.out.println("7: Remove fare from Flight Schedule Plan");
            System.out.println("8: Update day of week for weekly recurrent");
            System.out.println("9: Update start date / end date for weekly recurrent");

            System.out.print("> ");
            response = scanner.nextInt();
            if (response <= 0 || response > 9) {
                System.out.println("Invalid response! Enter 1-7");
            }
        } while (response <= 0 || response > 9);

        switch (response) {
            case (1):
                HashSet<Long> flightScheduleIds = new HashSet<>();
                String addMore = "Y";

                this.printAllFlightSchedule(flightSchedulePlan);

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
                    flightSchedulePlanSessionBeanRemote.updateRemoveFlightScheduleFromManualFlightSchedulePlan(flightSchedulePlanId, flightScheduleIds);
                    System.out.println("FLIGHT SCHEDULES SUCCESSFULLY REMOVED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;

            case (2):
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
                    flightSchedulePlanSessionBeanRemote.updateAddFlightScheduleToManualFlightSchedulePlan(flightSchedulePlanId, newFlightSchedules, doCreateReturnFlightSchedule.equals("Y"));
                    System.out.println("FLIGHT SCHEDULES SUCCESSFULLY ADDED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;

            case (3):
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
                    newRecurrentFrequency = scanner.nextInt();
                    if (newRecurrentFrequency.equals("-")) {
                        newRecurrentFrequency = null;
                        break;
                    }
                    if (newRecurrentFrequency <= 0) {
                        System.out.println("Invalid response! Recurrent frequency must be more than 0.");
                    }
                } while (newRecurrentFrequency <= 0);

                try {
                    flightSchedulePlanSessionBeanRemote.updateRecurrentNDaysFlightSchedulePlanParameters(flightSchedulePlanId, newEndDate, newRecurrentFrequency);
                    System.out.println("FLIGHT SCHEDULE PLAN SUCCESSFULLY UPDATED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;

            case (4):
                List<FlightScheduleEntity> updatedFlightSchedules = new ArrayList<>();
                String updateMoreFlightSchedules = "N";

                String departureDate = "";
                Boolean checkDate = false;
                Date newDepartureDate = null;

                Integer estimatedFlightDuration = 0;

                this.printAllFlightSchedule(flightSchedulePlan);
                do {
                    System.out.print("Enter Flight Schedule Id to update> ");
                    Long flightScheduleId = scanner.nextLong();

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
                            System.out.print("Enter new flight duration> ");
                            estimatedFlightDuration = scanner.nextInt();
                            if (estimatedFlightDuration <= 0) {
                                System.out.println("Invalid response! Flight duration must be greater than 0.");
                            }
                        } while (estimatedFlightDuration <= 0);

                        flightSchedule.setDepartureDate(newDepartureDate);
                        flightSchedule.setEstimatedFlightDurationHour(estimatedFlightDuration);
                        updatedFlightSchedules.add(flightSchedule);

                        do {
                            System.out.println("Do you want to update more flight schedules? (Y/N)");
                            updateMoreFlightSchedules = scanner.nextLine().trim();
                            if (!updateMoreFlightSchedules.equals("Y") && !updateMoreFlightSchedules.equals("N")) {
                                System.out.println("Invalied response! Please enter Y/N");
                            }
                        } while (!updateMoreFlightSchedules.equals("Y") && !updateMoreFlightSchedules.equals("N"));
                    } catch (FlightScheduleNotFoundException ex) {
                        System.out.println(ex.getMessage() + "\n");
                    }
                } while (updateMoreFlightSchedules.equals("Y"));

                try {
                    flightSchedulePlanSessionBeanRemote.updateFlightScheduleDetailForManualFlightSchedulePlan(flightSchedulePlanId, updatedFlightSchedules);
                    System.out.println("FLIGHT SCHEDULES SUCCESSFULLY ADDED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;

            case (5):
                List<FareEntity> updatedFareAmounts = new ArrayList<>();
                String updateMoreFares = "N";

                this.printAllFares(flightSchedulePlan);

                do {
                    System.out.print("Enter Fare Id to update> ");
                    Long fareId = scanner.nextLong();

                    try {
                        Double fareAmount = 0.0;

                        do {
                            System.out.print("Enter new Fare amount for Flight Schedule Plan> ");
                            fareAmount = scanner.nextDouble();
                            if (fareAmount <= 0) {
                                System.out.println("Fare amount must be greater than zero!");
                            }
                        } while (fareAmount <= 0);

                        updatedFareAmounts.add(fareEntitySessionBeanRemote.updateFareAmount(flightSchedulePlanId, fareId, new BigDecimal(fareAmount)));

                        scanner.nextLine();
                        do {
                            System.out.println("Do you want to update more fare amounts? (Y/N)");
                            updateMoreFares = scanner.nextLine().trim();
                            if (!updateMoreFares.equals("Y") && !updateMoreFares.equals("N")) {
                                System.out.println("Invalied response! Please enter Y/N");
                            }
                        } while (!updateMoreFares.equals("Y") && !updateMoreFares.equals("N"));
                    } catch (FlightSchedulePlanNotFoundException | UpdateFlightSchedulePlanFailedException ex) {
                        System.out.println(ex.getMessage() + "\n");
                    }
                } while (updateMoreFares.equals("Y"));

                try {
                    flightSchedulePlanSessionBeanRemote.updateFareAmountInFlightSchedulePlan(flightSchedulePlanId, updatedFareAmounts);
                    System.out.println("FARE AMOUNTS SUCCESSFULLY ADDED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;

            case (6):
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
                    flightSchedulePlanSessionBeanRemote.updateAddFareToFlightSchedulePlan(flightSchedulePlanId, addFares);
                    System.out.println("FARES SUCCESSFULLY ADDED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;

            case (7):
                HashSet<Long> removeFares = new HashSet<>();
                String removeMoreFares = "N";

                this.printAllFares(flightSchedulePlan);

                do {
                    System.out.print("Enter Fare Id for fare to be removed> ");

                    removeFares.add(scanner.nextLong());

                    do {
                        System.out.println("Do you want to remove more fares? (Y/N)");
                        removeMoreFares = scanner.nextLine().trim();
                        if (!removeMoreFares.equals("Y") && !removeMoreFares.equals("N")) {
                            System.out.println("Invalied response! Please enter Y/N");
                        }
                    } while (!removeMoreFares.equals("Y") && !removeMoreFares.equals("N"));
                } while (removeMoreFares.equals("Y"));

                try {
                    flightSchedulePlanSessionBeanRemote.updateRemoveFareFromFlightSchedulePlan(flightSchedulePlanId, removeFares);
                    System.out.println("FARES SUCCESSFULLY REMOVED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;
            case (8):
                Integer startDayOfWeek = 0;

                do {
                    System.out.println("Enter new start day of week (1 for Sunday, 7 for Saturday)> ");
                    startDayOfWeek = Integer.parseInt(scanner.nextLine().trim());
                    if (startDayOfWeek <= 0 || startDayOfWeek > 7) {
                        System.out.println("Invalid response! Start day of week must be between 1-7!");
                    }
                } while (startDayOfWeek <= 0 || startDayOfWeek > 7);

                try {
                    flightSchedulePlanSessionBeanRemote.updateRecurrentWeeklyFlightSchedulePlanDayOfWeek(flightSchedulePlanId, startDayOfWeek);
                    System.out.println("FLIGHT SCHEDULE PLAN START DAY OF WEEK UPDATED SUCCESSFULLY!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;
            case (9):
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

                try {
                    flightSchedulePlanSessionBeanRemote.updateRecurrentWeeklyFlightSchedulePlanRange(flightSchedulePlanId, newWeeklyStartDate, newWeeklyEndDate);
                    System.out.println("FLIGHT SCHEDULE PLAN START AND END DATE SUCCESSFULY UPDATED!\n");
                } catch (UpdateFlightSchedulePlanFailedException ex) {
                    System.out.println(ex.getMessage() + "\n");
                }
                break;
            default:
                break;
        }
    }

    //called from viewFlightSchedulePlanDetails()
    private void deleteFlightSchedulePlan(Long flightSchedulePlanId) {
        try {
            flightSchedulePlanSessionBeanRemote.deleteFlightSchedulePlanById(flightSchedulePlanId);
            System.out.println("Flight Schedule Plan has been siccesssfully deleted!");
        } catch (FlightSchedulePlanNotFoundException | FlightSchedulePlanInUseException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    //helper method for createFlight() and updateFlight()
    private void printAllFlightRoutes() {
        List<FlightRouteEntity> list = flightRouteSessionBeanRemote.retrieveAllFlightRoutes();

        System.out.println("=====Flight Routes=====");
        for (FlightRouteEntity flightRoute : list) {
            System.out.print("Flight Route Id " + flightRoute.getFlightRouteId() + ": ");
            System.out.print(flightRoute.getOriginAirport().getAirportName() + " ---> ");
            System.out.println(flightRoute.getDestinationAirport().getAirportName());

            if (flightRoute.getReturnFlightRoute() != null) {
                FlightRouteEntity returnFlightRoute = flightRoute.getReturnFlightRoute();
                System.out.print("Flight Route Id " + returnFlightRoute.getFlightRouteId() + ": ");
                System.out.print(returnFlightRoute.getOriginAirport().getAirportName() + " ---> ");
                System.out.println(returnFlightRoute.getDestinationAirport().getAirportName() + " (Return)");
            }
        }
        System.out.println("=======================");
    }

    private void printAllAircraftConfiguration() {
        List<AircraftConfigurationEntity> aircraftConfigurationEntitylist = aircraftConfigurationSessionBeanRemote.retrieveAllAircraftConfiguration();

        System.out.println("=====Aircraft Configurations=====");
        for (AircraftConfigurationEntity aircraftConfigurationEntity : aircraftConfigurationEntitylist) {
            System.out.print("Aircraft configuration id " + aircraftConfigurationEntity.getAircraftConfigurationId() + ": ");
            System.out.println(aircraftConfigurationEntity.getAircraftConfigurationName());
        }
        System.out.println("=================================");
    }

    private void printAllFlightSchedule(FlightSchedulePlanEntity flightSchedulePlan) {
        System.out.println("=====Flight Schedules=====");
        for (FlightScheduleEntity flightSchedule : flightSchedulePlan.getFlightSchedules()) {
            System.out.println("Flight Schedule Id " + flightSchedule.getFlightScheduleId() + ": " + flightSchedule.getDepartureDate());
        }
        System.out.println("==========================");
    }

    private void printAllFares(FlightSchedulePlanEntity flightSchedulePlan) {
        System.out.println("=====Fares=====");
        for (FareEntity fare : flightSchedulePlan.getFares()) {
            System.out.println("Fare Id " + fare.getFareId() + ": " + fare.getFareBasisCode() + ", $" + fare.getFareAmount());
        }
        System.out.println("===============");
    }
}
