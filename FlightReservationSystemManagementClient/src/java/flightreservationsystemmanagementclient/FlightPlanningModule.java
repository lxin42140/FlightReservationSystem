/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flightreservationsystemmanagementclient;

import ejb.session.stateless.AircraftConfigurationSessionBeanRemote;
import ejb.session.stateless.AircraftTypeSessionBeanRemote;
import ejb.session.stateless.AirportEntitySessionBeanRemote;
import ejb.session.stateless.FlightRouteSessionBeanRemote;
import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import entity.AirportEntity;
import entity.CabinConfigurationEntity;
import entity.FlightRouteEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import util.enumeration.CabinClassEnum;
import util.enumeration.EmployeeAccessRightEnum;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.AircraftTypeNotFoundException;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.CreateNewFlightRouteException;
import util.exception.FlightRouteInUseException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author kiyon
 */
public class FlightPlanningModule {

    @EJB
    private AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote;
    @EJB
    private FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;
    @EJB
    private AirportEntitySessionBeanRemote airportEntitySessionBeanRemote;
    @EJB
    private AircraftTypeSessionBeanRemote aircraftTypeSessionBeanRemote;

    public FlightPlanningModule() {
    }

    public FlightPlanningModule(AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote, FlightRouteSessionBeanRemote flightRouteSessionBeanRemote, AirportEntitySessionBeanRemote airportEntitySessionBeanRemote, AircraftTypeSessionBeanRemote aircraftTypeSessionBeanRemote) {
        this.aircraftConfigurationSessionBeanRemote = aircraftConfigurationSessionBeanRemote;
        this.flightRouteSessionBeanRemote = flightRouteSessionBeanRemote;
        this.airportEntitySessionBeanRemote = airportEntitySessionBeanRemote;
        this.aircraftTypeSessionBeanRemote = aircraftTypeSessionBeanRemote;
    }

    public void flightPlanningMenu(EmployeeAccessRightEnum employeeAccessRightEnum) {
        if (employeeAccessRightEnum == EmployeeAccessRightEnum.FLEETMANAGER) {
            aircraftConfigurationMenu();
        } else if (employeeAccessRightEnum == EmployeeAccessRightEnum.ROUTEMANAGER) {
            flightRouteMenu();
        }
    }

    private void aircraftConfigurationMenu() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Flight Management System: Flight Planning Module ***\n");
            System.out.println("1: Create Aircraft Configuration");
            System.out.println("2: View All Aircraft Configurations");
            System.out.println("3: View Aircraft Configuration Details");
            System.out.println("4: Logout\n");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    createAircraftConfiguration();
                } else if (response == 2) {
                    viewAllAircraftConfigurations();
                } else if (response == 3) {
                    viewAircraftConfigurationDetails();
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

    private void flightRouteMenu() {
        Scanner scanner = new Scanner(System.in);
        Integer response = 0;

        while (true) {
            System.out.println("*** Flight Management System: Flight Planning Module ***\n");
            System.out.println("1: Create Flight Route");
            System.out.println("2: View All Flight Routes");
            System.out.println("3: Delete Flight Route");
            System.out.println("4: Logout\n");
            response = 0;

            while (response < 1 || response > 3) {
                System.out.print("> ");

                response = scanner.nextInt();

                if (response == 1) {
                    createFlightRoute();
                } else if (response == 2) {
                    viewAllFlightRoutes();
                } else if (response == 3) {
                    deleteFlightRoute();
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

    private void createAircraftConfiguration() {
        Scanner scanner = new Scanner(System.in);
        Long numCabinClasses = 0L;
        Long aircraftTypeId;

        AircraftConfigurationEntity aircraftConfiguration = new AircraftConfigurationEntity();

        System.out.println("*** Flight Planning Module: Create new Aircraft Configuration ***\n");

        System.out.print("Enter Aircraft Configuration name> ");
        aircraftConfiguration.setAircraftConfigurationName(scanner.nextLine().trim());

        System.out.println("=====List of Aircraft Types=====");
        List<AircraftTypeEntity> aircraftTypes = aircraftTypeSessionBeanRemote.retrieveAllAircraftTypes();
        for (AircraftTypeEntity aircraftType : aircraftTypes) {
            System.out.println("Aircraft Type Id " + aircraftType.getAricraftId() + ": " + aircraftType.getAricraftTypeName());
        }
        System.out.println("================================");

        System.out.print("Enter Aircraft Type Id> ");
        aircraftTypeId = scanner.nextLong();

        do {
            System.out.print("Number of Cabin Classes> ");
            numCabinClasses = scanner.nextLong();
            scanner.nextLine();
            if (numCabinClasses <= 0 || numCabinClasses > 4) {
                System.out.println("Number of cabins should be between 1 and 4!");
            }
        } while (numCabinClasses <= 0 || numCabinClasses > 4);

        List<CabinConfigurationEntity> cabinConfigurations = new ArrayList<>();
        for (int i = 0; i < numCabinClasses; i++) {
            cabinConfigurations.add(createCabinConfiguration(scanner, i + 1));
        }
        try {
            Long createdAircraftConfigurationId = aircraftConfigurationSessionBeanRemote.createNewAircraftConfiguration(aircraftConfiguration, cabinConfigurations, aircraftTypeId);
            System.out.println("AIRCRAFT CONFIGURATION SUCCESSFULLY CREATED! Aircraft Configuration Id: " + createdAircraftConfigurationId + ".\n");
        } catch (CreateNewAircraftConfigurationException | AircraftTypeNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private CabinConfigurationEntity createCabinConfiguration(Scanner scanner, Integer number) {
        CabinConfigurationEntity cabinConfigurationEntity = new CabinConfigurationEntity();

        String cabinType;
        Boolean invalidCabinType = true;
        Long numberOfAisles;
        Long numberOfSeatsAbreast;
        String seatingConfiguration;
        Boolean invalidSeatingConfiguration = true;

        System.out.println("=====Create Cabin #" + number + "=====");

        do {
            System.out.print("Enter cabin type (F/J/W/Y)> ");
            cabinType = scanner.nextLine().trim();
            if (!cabinType.equals("F") && !cabinType.equals("J") && !cabinType.equals("W") && !cabinType.equals("Y")) {
                System.out.println("Invalid cabin type! Cabin type must be F/J/W/Y.");
            } else {
                invalidCabinType = false;
                cabinConfigurationEntity.setCabinClass(CabinClassEnum.valueOf(cabinType));
            }
        } while (invalidCabinType);

        System.out.print("Enter number of aisles> ");
        numberOfAisles = scanner.nextLong();
        cabinConfigurationEntity.setNumberOfAisles(numberOfAisles);

        System.out.print("Enter number of rows> ");
        cabinConfigurationEntity.setNumberOfRows(scanner.nextLong());

        System.out.print("Enter number of seats abreast> ");
        numberOfSeatsAbreast = scanner.nextLong();
        cabinConfigurationEntity.setNumberOfSeatsAbreast(numberOfSeatsAbreast);

        scanner.nextLine();

        String pattern = "[1-9]";
        for (int i = 0; i < numberOfAisles; i++) {
            pattern += "-[1-9]";
        }

        do {
            System.out.print("Enter seating configuration for each row (e.g. 3-4-3 for 2 aisles)> ");
            seatingConfiguration = scanner.nextLine().trim();
            if (!Pattern.matches(pattern, seatingConfiguration)) {
                System.out.println("Invalid seating configuration! Follow the format.");
            } else {
                Long numSeats = 0L;
                for (int j = 0; j < seatingConfiguration.length(); j += 2) {
                    numSeats += Character.getNumericValue(seatingConfiguration.charAt(j));
                }
                if (!numSeats.equals(numberOfSeatsAbreast)) {
                    System.out.println("Invalid seating configuration! Number of seats does not add up to number of seats abreast.");
                } else {
                    invalidSeatingConfiguration = false;
                    cabinConfigurationEntity.setSeatingConfiguration(seatingConfiguration);
                }
            }
        } while (invalidSeatingConfiguration);

        return cabinConfigurationEntity;
    }

    private void viewAllAircraftConfigurations() {
        System.out.println("*** Flight Planning Module: View all Aircraft Configurations ***\n");

        List<AircraftConfigurationEntity> list = aircraftConfigurationSessionBeanRemote.retrieveAllAircraftConfiguration();

        for (AircraftConfigurationEntity aircraftConfiguration : list) {
            System.out.print("Aircraft type name: " + aircraftConfiguration.getAircraftType().getAricraftTypeName() + ", ");
            System.out.print("Aircraft configuration name: " + aircraftConfiguration.getAircraftConfigurationName() + ", ");
            System.out.println("Aircraft configuration id: " + aircraftConfiguration.getAircraftConfigurationId());
        }
        System.out.print("\n");
    }

    private void viewAircraftConfigurationDetails() {
        System.out.println("*** Flight Planning Module: View Aircraft Configuration Detail ***\n");

        List<AircraftConfigurationEntity> aircraftConfigurationEntitylist = aircraftConfigurationSessionBeanRemote.retrieveAllAircraftConfiguration();

        System.out.println("=====Aircraft Configurations=====");
        for (AircraftConfigurationEntity aircraftConfigurationEntity : aircraftConfigurationEntitylist) {
            System.out.print("Aircraft configuration id " + aircraftConfigurationEntity.getAircraftConfigurationId() + ": ");
            System.out.println(aircraftConfigurationEntity.getAircraftConfigurationName());
        }
        System.out.println("=================================");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Aircraft Configuration ID> ");
        Long aircraftConfigurationId = scanner.nextLong();
        AircraftConfigurationEntity aircraftConfiguration;

        try {
            aircraftConfiguration = aircraftConfigurationSessionBeanRemote.retrieveAircraftConfigurationById(aircraftConfigurationId);

            System.out.println("Aircraft Configuration id: " + aircraftConfiguration.getAircraftConfigurationId());
            System.out.println("Aircraft Configuration name: " + aircraftConfiguration.getAircraftConfigurationName());
            System.out.println("Aircraft type id: " + aircraftConfiguration.getAircraftType().getAricraftId());
            System.out.println("Total seat capacity: " + aircraftConfiguration.getMaximumConfigurationSeatCapacity());
            System.out.println("Number of cabin classes: " + aircraftConfiguration.getNumberOfCabins());

            List<CabinConfigurationEntity> list = aircraftConfiguration.getCabinConfigurations();
            for (int i = 0; i < aircraftConfiguration.getNumberOfCabins(); i++) {
                CabinConfigurationEntity cabinConfiguration = list.get(i);
                System.out.println("\tCabin configuration " + (i + 1) + ":");
                System.out.println("\tCabin type: " + cabinConfiguration.getCabinClass().toString());
                System.out.println("\tNumber of rows: " + cabinConfiguration.getNumberOfRows());
                System.out.println("\tNumber of seats abreast: " + cabinConfiguration.getNumberOfSeatsAbreast());
                System.out.println("\tSeating configuration: " + cabinConfiguration.getSeatingConfiguration() + "\n");
            }
        } catch (AircraftConfigurationNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void createFlightRoute() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Create Flight Route ***\n");

        List<AirportEntity> listAirport = airportEntitySessionBeanRemote.retrieveAllAirports();
        
        System.out.println("=====Airports=====");
        for (AirportEntity airport : listAirport) {
            System.out.println("Airport Id " + airport.getAirportId() + ": " + airport.getAirportName());
        }
        System.out.println("==================");
        
        System.out.print("Enter origin airport Id> ");
        Long originAirportId = scanner.nextLong();
        System.out.print("Enter destination airport Id> ");
        Long destinationAirportId = scanner.nextLong();
        scanner.nextLine();

        String response;

        do {
            System.out.print("Do you want to create a complementary return flight route? (Y/N)> ");
            response = scanner.nextLine().trim();
            if (!response.equals("Y") && !response.equals("N")) {
                System.out.println("Invalid response! Input Y/N.");
            }
        } while (!response.equals("Y") && !response.equals("N"));

        Boolean createReturnFlightRoute = response.equals("Y");

        try {
            Long flightRouteId = flightRouteSessionBeanRemote.createNewFlightRoute(originAirportId, destinationAirportId, createReturnFlightRoute);
            System.out.print("FLIGHT ROUTE SUCCESSFULLY CREATED! Flight Route Id " + flightRouteId + ": ");
            System.out.println(airportEntitySessionBeanRemote.retrieveAirportByid(originAirportId).getAirportName() + " ---> " + airportEntitySessionBeanRemote.retrieveAirportByid(destinationAirportId).getAirportName() + "\n");
        } catch (CreateNewFlightRouteException | AirportNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

    private void viewAllFlightRoutes() {
        System.out.println("*** Flight Planning Module: View All Flight Routes ***\n");

        List<FlightRouteEntity> list = flightRouteSessionBeanRemote.retrieveAllFlightRoutes();

        for (FlightRouteEntity flightRoute : list) {
            System.out.println("Flight Route Id " + flightRoute.getFlightRouteId() + ": ");
            System.out.print(flightRoute.getOriginAirport().getAirportName() + " ---> ");
            System.out.println(flightRoute.getDestinationAirport().getAirportName());

            if (flightRoute.getReturnFlightRoute() != null) {
                FlightRouteEntity returnFlightRoute = flightRoute.getReturnFlightRoute();
                System.out.println("ReturnFlight Route Id " + returnFlightRoute.getFlightRouteId() + ": ");
                System.out.print(returnFlightRoute.getOriginAirport().getAirportName() + " ---> ");
            }
            System.out.print("\n");
        }
    }

    private void deleteFlightRoute() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("*** Flight Planning Module: Delete Flight Route ***\n");

        System.out.print("Enter Flight Route Id> ");
        Long flightRouteId = scanner.nextLong();

        try {
            flightRouteSessionBeanRemote.deleteFlightRouteById(flightRouteId);
            System.out.println("Flight route " + flightRouteId + " successfully deleted!\n");
        } catch (FlightRouteNotFoundException | FlightRouteInUseException ex) {
            System.out.println(ex.getMessage() + "\n");
        }
    }

}
