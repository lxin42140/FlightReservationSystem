///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package flightreservationsystemmanagementclient;
//
//import ejb.session.stateless.AircraftConfigurationSessionBeanRemote;
//import ejb.session.stateless.EmployeeEntitySessionBeanRemote;
//import ejb.session.stateless.FlightRouteSessionBeanRemote;
//import entity.EmployeeEntity;
//import java.util.Scanner;
//import javax.ejb.EJB;
//import util.exception.EmployeeNotFoundException;
//import util.exception.InvalidLoginCredentialsException;
//
///**
// *
// * @author kiyon
// */
//public class MainApp {
//
//    @EJB
//    private EmployeeEntitySessionBeanRemote employeeEntitySessionBeanRemote;
//    @EJB
//    private AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote;
//    @EJB
//    private FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;
//    
//
//    private FlightPlanningModule flightPlanningModule;
//    private FlightOperationModule flightOperationModule;
//    private SalesManagementModule salesManagementModule;
//
//    private EmployeeEntity employeeEntity;
//
//    public MainApp() {
//    }
//    
//    public MainApp(EmployeeEntitySessionBeanRemote employeeEntitySessionBeanRemote, AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote, FlightRouteSessionBeanRemote flightRouteSessionBeanRemote) {
//        this.employeeEntitySessionBeanRemote = employeeEntitySessionBeanRemote;
//        this.aircraftConfigurationSessionBeanRemote = aircraftConfigurationSessionBeanRemote;
//        this.flightRouteSessionBeanRemote = flightRouteSessionBeanRemote;
//    }
//
//    public void runApp() {
//        Scanner scanner = new Scanner(System.in);
//        Integer response = 0;
//
//        while (true) {
//            System.out.println("*** Welcome to Flight Reservation System Management ***\n");
//            System.out.println("1: Login");
//            System.out.println("2: Exit\n");
//            response = 0;
//
//            while (response < 1 || response > 2) {
//                System.out.print("> ");
//
//                response = scanner.nextInt();
//
//                if (response == 1) {
//                    try {
//                        doLogin();
//                        System.out.println("Login successful!\n");
//
//                        flightPlanningModule = new FlightPlanningModule(aircraftConfigurationSessionBeanRemote, flightRouteSessionBeanRemote);
//                        flightOperationModule = new FlightOperationModule();
//                        salesManagementModule = new SalesManagementModule();
//
//                        menuMain();
//                    } catch (InvalidLoginCredentialsException ex) {
//                        System.out.println("InvalidLoginCredentialsException: " + ex.getMessage() + "\n");
//                    } catch (EmployeeNotFoundException ex) {
//                        System.out.println("EmployeeNotFoundException: " + ex.getMessage() + "\n");
//                    }
//                } else if (response == 2) {
//                    break;
//                } else {
//                    System.out.println("Invalid option, please try again!\n");
//                }
//            }
//
//            if (response == 2) {
//                break;
//            }
//        }
//    }
//
//    private void doLogin() throws InvalidLoginCredentialsException, EmployeeNotFoundException {
//        Scanner scanner = new Scanner(System.in);
//        String username = "";
//        String password = "";
//
//        System.out.println("*** Flight Reservation System :: Login ***\n");
//        System.out.print("Enter username> ");
//        username = scanner.nextLine().trim();
//        System.out.print("Enter password> ");
//        password = scanner.nextLine().trim();
//
//        if (username.length() > 0 && password.length() > 0) {
//            try {
//                employeeEntity = employeeEntitySessionBeanRemote.retrieveEmployeeByUsernamePassword(username, password);
//            } catch (InvalidLoginCredentialsException ex) {
//                throw new InvalidLoginCredentialsException(ex.getMessage());
//            } catch (EmployeeNotFoundException ex) {
//                throw new EmployeeNotFoundException(ex.getMessage());
//            }
//        } else {
//            throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Enter username and/or password!");
//        }
//    }
//
//    private void menuMain() {
//        String employeeAccessRight = employeeEntity.getEmployeeAccessRight().toString();
//
//        System.out.println("You are login as " + employeeEntity.getFirstName() + " " + employeeEntity.getLastName() + " with " + employeeAccessRight + " rights\n");
//
//        switch (employeeAccessRight) {
//            case ("FLEETMANAGER"):
//                flightPlanningModule.flightPlanningMenu("FLEETMANAGER");
//                break;
//            case ("ROUTEPLANNER"):
//                flightPlanningModule.flightPlanningMenu("ROUTEPLANNER");
//                break;
//            case ("SCHEDULEMANAGER"):
//                flightOperationModule.flightOperationMenu();
//                break;
//            case ("SALESMANAGER"):
//            default:
//                break;
//        }
//    }
//}
