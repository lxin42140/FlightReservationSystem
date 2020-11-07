/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemmanagementclient;

import ejb.session.stateless.FareEntitySessionBeanRemote;
import ejb.session.stateless.FlightRouteSessionBeanRemote;
import ejb.session.stateless.FlightSchedulePlanSessionBeanRemote;
import ejb.session.stateless.FlightSessionBeanRemote;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.ejb.EJB;
import util.enumeration.CabinClassEnum;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightInUseException;
import util.exception.FlightNotFoundException;
import util.exception.FlightRouteNotFoundException;
import util.exception.FlightSchedulePlanInUseException;
import util.exception.FlightSchedulePlanNotFoundException;

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

    public void flightOperationMenu() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Flight Management System: Flight Operation Module ***\n");
            System.out.println("1: Create new Flight");
            System.out.println("2: View All Flights");
            System.out.println("3: View Flight Details");
            System.out.println("4: Update Flight Details");
            System.out.println("5: Delete Flight\n");
            System.out.println("6: Create Flight Schedule Plan");
            System.out.println("7: View All Flight Schedule Plans");
            System.out.println("8: View Flight Schedule Plan Details");
//            System.out.println("9: View All Flight Schedule Plans");
//            System.out.println("10: View All Flight Schedule Plans");

            System.out.println("4: Logout\n");
            response = 0;

            //change this
            while (response < 1 || response > 10) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    createFlight();
                } else if (response == 2) {
                    viewAllFlights();
                } else if (response == 3) {
                    viewFlightDetails();
                } else if (response == 4) {
//                    updateFlight();
                } else if (response == 5) {
                    deleteFlight();
                } else if (response == 6) {
                    createFlightSchedulePlan();
                } else if (response == 7) {
                    viewAllFlightSchedulePlans();
                } else if (response == 8) {
                    viewFlightSchedulePlanDetails();
                } else if (response == 9) {
//                    updateFlightSchedulePlanDetails();
                } else if (response == 10) {

                } else if (response == 15) {
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

    private void createFlight() {
        Scanner scanner = new Scanner(System.in);

        FlightEntity flight = new FlightEntity();

        System.out.println("*** Flight Planning Module: Create new Flight ***\n");

        try {
            System.out.print("Enter Flight Route Id>");
            Long flightRouteId = scanner.nextLong();
            scanner.nextLine(); // skip to next line

            FlightRouteEntity flightRoute = flightRouteSessionBeanRemote.retrieveFlightRouteById(flightRouteId);
            //flight.setFlightRoute(flightRoute);//not required

            //String originAirportCode = flightRoute.getOriginAirport().getIataAirlineCode();
            String flightNumber = "ML" + generateRandomNumber(); //flight number needs to begin with ML
            flight.setFlightNumber(flightNumber);

            System.out.print("Enter Aircraft Configuration Id>");
            Long aircraftConfigurationId = scanner.nextLong();

            String response = "N";

            // only prompt client to create return flight if flight route has return flight route
            if (flightRoute.getReturnFlightRoute() != null) {
                do {
                    System.out.println("Do you want to create a complementary return flight? (Y/N)>");
                    response = scanner.nextLine().trim();
                    if (!response.equals("Y") && !response.equals("N")) {
                        System.out.println("Invalid response! Input Y/N.");
                    }
                } while (!response.equals("Y") && !response.equals("N"));
            }

            Boolean createReturnFlight = response.equals("Y");

            String returnFlightNumber = "";
            if (createReturnFlight) {
                //returnFlightNumber = flightRoute.getDestinationAirport().getIataAirlineCode() + generateRandomNumber();
                returnFlightNumber = "ML" + generateRandomNumber();
                while (returnFlightNumber.equals(flightNumber)) { // auto generated return flight number should not be same as origin flight number
                    returnFlightNumber = "ML" + generateRandomNumber();
                }
            }

            String flightId = flightSessionBeanRemote.createNewFlight(flight, flightRouteId, aircraftConfigurationId, createReturnFlight, returnFlightNumber);

            System.out.println("Flight successfully created!");
            // print both flight number since they are auto generated by the system
            System.out.println("Flight Number> " + flightId);
            if (returnFlightNumber.length() > 0) { // print return flight number if any 
                System.out.println("Return Flight Number> " + returnFlightNumber);
            }

        } catch (FlightRouteNotFoundException | CreateNewFlightException | AircraftConfigurationNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String generateRandomNumber() {
        Long randomNumber = 0L;

        for (int i = 0; i < 3; i++) {
            randomNumber *= 10;
            if (i == 0) { //first digit cannot be 0
                randomNumber += new Random().nextInt(9) + 1;
            } else {
                randomNumber += new Random().nextInt(10);
            }
        }
        return randomNumber.toString();
    }

    private void viewAllFlights() {
        System.out.println("*** Flight Planning Module: View all Flights ***\n");

        List<FlightEntity> list = flightSessionBeanRemote.retrieveAllFlights();
        for (FlightEntity flight : list) {

            System.out.println("Flight number: " + flight.getFlightNumber());
            System.out.println("\tOrigin Airport: " + flight.getFlightRoute().getOriginAirport().getAirportName() + "---> Destination Airport: " + flight.getFlightRoute().getDestinationAirport().getAirportName());

            if (flight.getReturnFlight() != null) {
                FlightEntity returnFlight = flight.getReturnFlight();
                System.out.println("\t\tReturn Flight number: " + returnFlight.getFlightNumber());
                System.out.println("\t\tOrigin Airport: " + returnFlight.getFlightRoute().getOriginAirport().getAirportName() + "---> Destination Airport: " + returnFlight.getFlightRoute().getDestinationAirport().getAirportName());
            }
            System.out.println("-----");
        }
    }

    private void viewFlightDetails() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: View Flight Details ***\n");

        System.out.print("Enter Flight Number>");
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
        } catch (FlightNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //KIV on what to update
//    private void updateFlight() {
//        
//    }
    private void deleteFlight() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Delete Flight ***\n");

        System.out.print("Enter Flight Number> ");
        String flightNumber = scanner.nextLine().trim();

        try {
            flightSessionBeanRemote.deleteFlightByFlightNumber(flightNumber);
        } catch (FlightNotFoundException | FlightInUseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void createFlightSchedulePlan() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Create new Flight Schedule Plan ***\n");

        System.out.println("Enter Flight Number> ");
        String flightNumber = scanner.nextLine().trim();

        try {
            FlightEntity flight = flightSessionBeanRemote.retrieveFlightByFlightNumber(flightNumber);
            List<CabinConfigurationEntity> cabinList = flight.getAircraftConfiguration().getCabinConfigurations();

            HashMap<String, HashSet<FareEntity>> fareList = new HashMap<>();

            boolean isCreatingFare = true;
            String createMoreFare = "";

            do {
                FareEntity fare = new FareEntity();
                System.out.println("Create Fare Basis Code (Starts With F/J/W/Y)> ");

                String cabinClass = "";
                do {
                    System.out.print("Enter cabin class (F/J/W/Y)> ");
                    cabinClass = scanner.nextLine().trim();
                } while (!cabinClass.equals("F") || !cabinClass.equals("J") || !cabinClass.equals("W") || !cabinClass.equals("Y"));

                String fareBasisCode = cabinClass;
                do {
                    System.out.print("Enter fare basis code>");
                    fareBasisCode = scanner.nextLine().trim();
                } while (fareBasisCode.length() <= 0 || fareBasisCode.length() > 6);

                BigDecimal fareAmount = new BigDecimal(0);
                do {
                    System.out.print("Enter fare amount>");
                    fareAmount = scanner.nextBigDecimal();
                } while (fareAmount.doubleValue() <= 0);

                fare.setCabinClass(CabinClassEnum.valueOf(cabinClass));
                fare.setFareBasisCode(fareBasisCode);
                fare.setFareAmount(fareAmount);

                if (fareList.containsKey(cabinClass)) {
                    fareList.get(cabinClass).add(fare);
                } else {
                    HashSet<FareEntity> list = new HashSet<>();
                    list.add(fare);
                    fareList.put(cabinClass, list);
                }

                do {
                    System.out.print("Do you want to create more fares? (Y/N)>");
                    createMoreFare = scanner.nextLine().trim();
                    if (!createMoreFare.equals("Y") || !createMoreFare.equals("N")) {
                        System.out.println("Invalid response! Enter Y/N");
                    }
                    if (createMoreFare.equals("N")) {
                        isCreatingFare = false;
                        if (fareList.keySet().size() != cabinList.size()) {
                            isCreatingFare = true;
                            System.out.println("One or more cabin classes do not have a fare basis code!");
                            // no fare basis code ?
                        }
                    }
                } while (!createMoreFare.equals("Y") || !createMoreFare.equals("N"));
            } while (isCreatingFare);

            List<FareEntity> fares = new ArrayList<>();
            for (HashSet<FareEntity> set : fareList.values()) {
                fares.addAll(set);
            }

            Integer response = 0;
            do {
                System.out.println("Schedule Plan types: 1 - Single, 2 - Multiple, 3 - Recurrent every n days, 4 - Recurrent every week");
                System.out.print("Enter Schedule Plan type>");
                response = scanner.nextInt();
                if (response <= 0 || response > 4) {
                    System.out.println("Invalid response! Enter 1-4");
                }
            } while (response <= 0 || response > 4);

            String returnSchedulePlanResponse = "";
            // prompt user only if flight has a return flight
            if (flight.getReturnFlight() != null) {
                do {
                    System.out.print("Do you want to create return Flight Schedule Plan? (Y/N)>");
                    returnSchedulePlanResponse = scanner.nextLine().trim();
                    if (!returnSchedulePlanResponse.equals("Y") || !returnSchedulePlanResponse.equals("N")) {
                        System.out.println("Invalid response! Enter Y/N");
                    }
                } while (!returnSchedulePlanResponse.equals("Y") || !returnSchedulePlanResponse.equals("N"));
            }

            Boolean doCreateReturnFlightSchedule = returnSchedulePlanResponse.equals("Y");

            if (response == 1 || response == 2) {
                List<FlightScheduleEntity> flightSchedules = new ArrayList<>();
                if (response == 1) {
                    flightSchedules.add(createFlightSchedule());
                } else {
                    Boolean createMoreFlightSchedule = true;
                    String createFlightScheduleResponse = "";
                    do {
                        flightSchedules.add(createFlightSchedule());
                        do {
                            System.out.print("Do you want to create more Flight Schedules? (Y/N)>");
                            createFlightScheduleResponse = scanner.nextLine().trim();
                            if (!createFlightScheduleResponse.equals("Y") || !createFlightScheduleResponse.equals("N")) {
                                System.out.println("Invalid response! Enter Y/N");
                            } else {
                                createMoreFlightSchedule = createFlightScheduleResponse.equals("Y");
                            }
                        } while (!createFlightScheduleResponse.equals("Y") || !createFlightScheduleResponse.equals("N"));
                    } while (createMoreFlightSchedule);
                }

                flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules, fares, flightNumber, doCreateReturnFlightSchedule);
            } else if (response == 3 || response == 4) {
                FlightScheduleEntity baseFlightSchedule = createFlightSchedule();

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                String dateInput = "";
                Date endDate = new Date();
                Boolean dateCheck = false;

                while (!dateCheck) {
                    try {
                        System.out.print("Enter the end date (DD/MM/YYYY)>");
                        dateInput = scanner.nextLine();
                        endDate = format.parse(dateInput);
                        dateCheck = true;
                    } catch (ParseException ex) {
                        System.out.println("Wrong format for date!");
                    }
                }

                Integer recurrentDaysFrequency = 7;
                if (response == 3) {
                    do {
                        System.out.print("Enter recurrent days frequency>");
                        recurrentDaysFrequency = scanner.nextInt();
                        if (recurrentDaysFrequency <= 0) {
                            System.out.println("Frequency must be more than 0!");
                        }
                    } while (recurrentDaysFrequency <= 0);
                }

                flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(endDate, recurrentDaysFrequency, baseFlightSchedule, fares, flightNumber, doCreateReturnFlightSchedule);
            }

            System.out.println("Flight schedule plan successfully created!");

        } catch (FlightNotFoundException | CreateNewFlightSchedulePlanException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private FlightScheduleEntity createFlightSchedule() {
        Scanner scanner = new Scanner(System.in);
        FlightScheduleEntity flightSchedule = new FlightScheduleEntity();
        System.out.println("Create Flight Schedule:");

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateInput = "";
        Date departureDate = new Date();
        Boolean dateCheck = false;

        while (!dateCheck) {
            try {
                System.out.print("Enter the departure date (DD/MM/YYYY)>");
                dateInput = scanner.nextLine();
                departureDate = format.parse(dateInput);
                dateCheck = true;
            } catch (ParseException ex) {
                System.out.println("Wrong format for date!");
            }
        }
        flightSchedule.setDepartureDate(departureDate);

        System.out.print("Enter the estimated flight duration>");
        Integer estimatedDuration = scanner.nextInt();
        flightSchedule.setEstimatedFlightDuration(estimatedDuration);

        return flightSchedule;
    }

    private void viewAllFlightSchedulePlans() {
        System.out.println("*** Flight Planning Module: View all Flight Schedule Plans ***\n");

        List<FlightSchedulePlanEntity> flightSchedulePlans = flightSchedulePlanSessionBeanRemote.retrieveAllFlightSchedulePlans();

        for (FlightSchedulePlanEntity flightSchedulePlan : flightSchedulePlans) {
            System.out.println("Flight number: " + flightSchedulePlan.getFlight().getFlightNumber());
            System.out.println("\tFlight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
            System.out.println("\tFirst departure date/time: " + flightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
            if (flightSchedulePlan.getReturnFlightSchedulePlan() != null) {
                System.out.println("Return Flight number: " + flightSchedulePlan.getFlight().getFlightNumber());
                System.out.println("\tReturn Flight Schedule Plan Id: " + flightSchedulePlan.getFlightSchedulePlanId());
                System.out.println("\tFirst departure date/time: " + flightSchedulePlan.getFlightSchedules().get(0).getDepartureDate());
            }
            System.out.print("\n");
        }
    }

    private void viewFlightSchedulePlanDetails() {
        System.out.println("*** Flight Planning Module: View Flight Schedule Plan Details ***\n");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Flight Schedule Plan Id>");
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
                System.out.println("\t\tEstimated Flight Duration: " + flightSchedule.getEstimatedFlightDuration());
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
                System.out.print(">");
                response = scanner.nextInt();
                if (response <= 0 || response > 3) {
                    System.out.println("Invalid response! Enter 1-3");
                }
            } while (response <= 0 || response > 3);
            
            if (response == 1) {
//                updateFlightSchedulePlanDetails(flightSchedulePlanId);
            } else if (response == 2) {
                deleteFlightSchedulePlan(flightSchedulePlanId);
            }
            
        } catch (FlightSchedulePlanNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    //KIV on update methods
//    private void updateFlightSchedulePlanDetails(Long flightSchedulePlanId) {
//        
//    }
    private void deleteFlightSchedulePlan(Long flightSchedulePlanId) {
        try {
            flightSchedulePlanSessionBeanRemote.deleteFlightSchedulePlanById(flightSchedulePlanId);
            System.out.println("Flight Schedule Plan has been siccesssfully deleted!");
        } catch(FlightSchedulePlanNotFoundException | FlightSchedulePlanInUseException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
