/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftConfigurationSessionBeanRemote;
import ejb.session.stateless.AircraftTypeSessionBeanRemote;
import ejb.session.stateless.FlightRouteSessionBeanRemote;
import ejb.session.stateless.FlightSchedulePlanSessionBeanRemote;
import ejb.session.stateless.FlightSessionBeanRemote;
import ejb.session.stateless.SeatInventorySessionBeanRemote;
import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import java.util.ArrayList;
import entity.CabinConfigurationEntity;
import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightRouteEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.AircraftTypeNotFoundException;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.CreateNewFlightRouteException;
import util.exception.FlightInUseException;
import util.exception.FlightNotFoundException;
import util.exception.FlightRouteInUseException;
import util.exception.FlightRouteNotFoundException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.FlightSchedulePlanInUseException;
import util.exception.FlightSchedulePlanNotFoundException;
import util.exception.UpdateFlightSchedulePlanFailedException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
@DependsOn({"AircraftTypeInitSessionBean", "AirportInitSessionBean", "EmployeeInitSessionBean", "PartnerInitSessionBean"})
public class TestSessionBean {

    @EJB(name = "SeatInventorySessionBeanRemote")
    private SeatInventorySessionBeanRemote seatInventorySessionBeanRemote;

    @EJB(name = "FlightRouteSessionBeanRemote")
    private FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;

    @EJB(name = "FlightSessionBeanRemote")
    private FlightSessionBeanRemote flightSessionBeanRemote;

    @EJB
    private AircraftTypeSessionBeanRemote aircraftTypeSessionBeanRemote;

    @EJB
    private AircraftConfigurationSessionBeanRemote aircraftConfigurationSessionBeanRemote;

    @EJB
    private FlightSchedulePlanSessionBeanRemote flightSchedulePlanSessionBeanRemote;

    @PostConstruct
    public void postConstruct() {
        System.out.println("-----------------------TEST------------------------------\n");
        try {
//            createAircraftConfig(); 
//            viewAllAircraftConfigurations();
            
//            createFlightRoute();
//            viewAllFlightRoute();
//            deleteFlightRoute();
                        
//            createFlight();
//            viewAllFlights();
//            deleteFlight();
            
//            createFlightSchedulePlan();
//            viewAllFlightSchedulePlans();
//            deleteFlightSchedulePlan();
//            updateFlightSchedulePlanEntity();
            
//            viewSeatInventory();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        System.out.println("-----------------------END TEST------------------------------\n");
    }

    private void createAircraftConfig() {
        List<AircraftTypeEntity> list = aircraftTypeSessionBeanRemote.retrieveAllAircraftTypes();
        Long aircraftTypeId = list.get(0).getAricraftId();
        Long aircraftTypeId1 = list.get(1).getAricraftId();

        AircraftConfigurationEntity firstAircraftConfig = new AircraftConfigurationEntity("SIAPremium");
        AircraftConfigurationEntity secondAircraftConfig = new AircraftConfigurationEntity("SIAEconomy");
        //AircraftConfigurationEntity thirdAircraftConfig = new AircraftConfigurationEntity("SIAPremium");
        AircraftConfigurationEntity fourthAircraftConfig = new AircraftConfigurationEntity("SIABudget");

        CabinConfigurationEntity firstCabinConfig = new CabinConfigurationEntity(2L, 10L, 2L, 20L, "3-4-3", CabinClassEnum.F);
        CabinConfigurationEntity secondCabinConfig = new CabinConfigurationEntity(2L, 10L, 4L, 40L, "3-4-3", CabinClassEnum.J);
        CabinConfigurationEntity thirdCabinConfig = new CabinConfigurationEntity(2L, 10L, 8L, 80L, "3-4-3", CabinClassEnum.W);

        List<CabinConfigurationEntity> firstList = new ArrayList<>();
        firstList.add(firstCabinConfig);

        List<CabinConfigurationEntity> secondList = new ArrayList<>();
        secondList.add(secondCabinConfig);
        secondList.add(thirdCabinConfig);

        List<CabinConfigurationEntity> thirdList = new ArrayList<>();
        thirdList.add(firstCabinConfig);
        thirdList.add(secondCabinConfig);
        thirdList.add(thirdCabinConfig);

        try {
            aircraftConfigurationSessionBeanRemote.createNewAircraftConfiguration(firstAircraftConfig, firstList, aircraftTypeId);
            aircraftConfigurationSessionBeanRemote.createNewAircraftConfiguration(secondAircraftConfig, secondList, aircraftTypeId);
            aircraftConfigurationSessionBeanRemote.createNewAircraftConfiguration(fourthAircraftConfig, thirdList, aircraftTypeId1);

            //should print an error because aircraft configuration have same name (SUCCESS)
            //aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(thirdAircraftConfig, firstList, aircraftTypeId);
            //should print an error because exceed seat limit (SUCCESS)
            //aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(fourthAircraftConfig, thirdList, aircraftTypeId);
        } catch (CreateNewAircraftConfigurationException | AircraftTypeNotFoundException ex) {
            System.out.println(ex);
        }
    }

    private void viewAllAircraftConfigurations() {
        List<AircraftConfigurationEntity> list = aircraftConfigurationSessionBeanRemote.retrieveAllAircraftConfiguration();

        for (AircraftConfigurationEntity aircraftConfigurationEntity : list) {
            System.out.println(aircraftConfigurationEntity.getAircraftType().getAricraftTypeName() + " " + aircraftConfigurationEntity.getAircraftConfigurationName());
        }
        System.out.println("");
    }

    private void createFlightRoute() {
        try {
            flightRouteSessionBeanRemote.createNewFlightRoute(1l, 5l, true);
//            flightRouteSessionBean.createNewFlightRoute(3l, 4l, false);
//            flightRouteSessionBean.createNewFlightRoute(5l, 6l, true);
        } catch (CreateNewFlightRouteException | AirportNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    private void viewAllFlightRoute() {
        List<FlightRouteEntity> list = flightRouteSessionBeanRemote.retrieveAllFlightRoutes();

        for (FlightRouteEntity flightRouteEntity : list) {
            System.out.println(flightRouteEntity.getOriginAirport().getAirportName() + "--->" + flightRouteEntity.getDestinationAirport().getAirportName());
            if (flightRouteEntity.getReturnFlightRoute() != null) {
                FlightRouteEntity returnFlightRouteEntity = flightRouteEntity.getReturnFlightRoute();
                System.out.println("\tRETURN FLIGHT ROUTE " + returnFlightRouteEntity.getOriginAirport().getAirportName() + "--->" + returnFlightRouteEntity.getDestinationAirport().getAirportName());
            }
        }
        System.out.println("");
    }

    private void deleteFlightRoute() {
        try {
            // delete return flight route only
            flightRouteSessionBeanRemote.deleteFlightRouteById(6l);
            viewAllFlightRoute();

            //delete main flight route with return flight route
//            flightRouteSessionBean.deleteFlightRouteById(5l);
//            viewAllFlightRoute();
            //delete flight route with no return flight route
//            flightRouteSessionBean.deleteFlightRouteById(3l);
//            viewAllFlightRoute();
        } catch (FlightRouteNotFoundException | FlightRouteInUseException ex) {
            System.out.println(ex);
        }
    }

    private void createFlight() {
        FlightEntity flightEntity = new FlightEntity("ML001");
//        FlightEntity flightEntity1 = new FlightEntity("ML003");
//        FlightEntity flightEntity2 = new FlightEntity("ML005");
        try {
            System.out.println(flightSessionBeanRemote.createNewFlight(flightEntity, 3l, 4l, Boolean.FALSE, "ML001"));
            //System.out.println(flightSessionBeanRemote.createNewFlight(flightEntity, 2l, 3l, Boolean.FALSE, "ML002"));
//            System.out.println(flightSessionBean.createNewFlight(flightEntity1, 5l, 3l, Boolean.TRUE, "ML004"));
//            System.out.println(flightSessionBean.createNewFlight(flightEntity2, 3l, 4l, Boolean.FALSE, "ML006"));
        } catch (CreateNewFlightException | FlightRouteNotFoundException | AircraftConfigurationNotFoundException ex) {
            System.out.println(ex);
        }
    }

    private void viewAllFlights() {
        List<FlightEntity> list = flightSessionBeanRemote.retrieveAllFlights();

        for (FlightEntity flightEntity : list) {
            System.out.println(flightEntity.getFlightNumber() + ": " + flightEntity.getFlightRoute().getOriginAirport().getAirportName() + "--->" + flightEntity.getFlightRoute().getDestinationAirport().getAirportName() + " via " + flightEntity.getAircraftConfiguration().getAircraftConfigurationName());
            if (flightEntity.getReturnFlight() != null) {
                FlightEntity returnFlightEntity = flightEntity.getReturnFlight();
                System.out.println("\t" + returnFlightEntity.getFlightNumber() + ": " + returnFlightEntity.getFlightRoute().getOriginAirport().getAirportName() + "--->" + returnFlightEntity.getFlightRoute().getDestinationAirport().getAirportName() + " via " + returnFlightEntity.getAircraftConfiguration().getAircraftConfigurationName());
            }
        }
    }

    private void deleteFlight() {
        try {
            // delete return flight only
            flightSessionBeanRemote.deleteFlightByFlightNumber("ML002");
            viewAllFlights();

            // delete flight with return flight
//            flightSessionBean.deleteFlightByFlightNumber("ML003");
//            viewAllFlights();
//            flightSessionBean.deleteFlightByFlightNumber("ML001");
//            flightSessionBean.deleteFlightByFlightNumber("ML005");
            viewAllFlights();
        } catch (FlightNotFoundException | FlightInUseException ex) {
            System.out.println(ex);
        }
    }

    private void createFlightSchedulePlan() {
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y");

            FlightScheduleEntity flightScheduleEntity = new FlightScheduleEntity();
            flightScheduleEntity.setDepartureDate(inputDateFormat.parse("05/11/2020"));
            flightScheduleEntity.setEstimatedFlightDuration(2);

            FlightScheduleEntity flightScheduleEntity1 = new FlightScheduleEntity();
            flightScheduleEntity1.setDepartureDate(inputDateFormat.parse("06/11/2020"));
            flightScheduleEntity1.setEstimatedFlightDuration(3);

            FlightScheduleEntity flightScheduleEntity2 = new FlightScheduleEntity();
            flightScheduleEntity1.setDepartureDate(inputDateFormat.parse("05/11/2020"));
            flightScheduleEntity1.setEstimatedFlightDuration(3);

            List<FlightScheduleEntity> flightSchedules = new ArrayList<>();
            flightSchedules.add(flightScheduleEntity);
            flightSchedules.add(flightScheduleEntity1);

            List<FlightScheduleEntity> flightSchedules1 = new ArrayList<>();
            flightSchedules.add(flightScheduleEntity2);

            FareEntity fareEntity = new FareEntity("F123", BigDecimal.valueOf(100.0), CabinClassEnum.F);
            FareEntity fareEntity1 = new FareEntity("F456", BigDecimal.valueOf(200.0), CabinClassEnum.F);
            List<FareEntity> fares = new ArrayList<>();
            fares.add(fareEntity);
            fares.add(fareEntity1);

            flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules, fares, "ML001", true);
            flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules1, fares, "ML001", true);
//            FlightScheduleEntity base = new FlightScheduleEntity();
//            base.setDepartureDate(inputDateFormat.parse("10/11/2020"));
//            base.setEstimatedFlightDuration(2);
//
//            FareEntity fareEntity3 = new FareEntity("W111", BigDecimal.valueOf(100.0), CabinClassEnum.W);
//            FareEntity fareEntity4 = new FareEntity("J111", BigDecimal.valueOf(200.0), CabinClassEnum.J);
//            FareEntity fareEntity5 = new FareEntity("F111", BigDecimal.valueOf(200.0), CabinClassEnum.F);
//            FareEntity fareEntity6 = new FareEntity("W222", BigDecimal.valueOf(300.0), CabinClassEnum.W);
//            FareEntity fareEntity7 = new FareEntity("J222", BigDecimal.valueOf(300.0), CabinClassEnum.J);
//            FareEntity fareEntity8 = new FareEntity("F222", BigDecimal.valueOf(300.0), CabinClassEnum.F);
//
//            List<FareEntity> fares1 = new ArrayList<>();
//            fares1.add(fareEntity3);
//            fares1.add(fareEntity4);
//            fares1.add(fareEntity5);
//            fares1.add(fareEntity6);
//            fares1.add(fareEntity7);
//            fares1.add(fareEntity8);
//
//            flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(inputDateFormat.parse("13/11/2020"), 1, base, fares1, "ML001", Boolean.FALSE);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private void viewAllFlightSchedulePlans() {
        List<FlightSchedulePlanEntity> list = flightSchedulePlanSessionBeanRemote.retrieveAllFlightSchedulePlans();
        for (FlightSchedulePlanEntity flightSchedulePlanEntity : list) {
            System.out.println("Flight schedule plan ID: " + flightSchedulePlanEntity.getFlightSchedulePlanId());
            System.out.println("\tFlight schedules> ");
            for (FlightScheduleEntity flightScheduleEntity : flightSchedulePlanEntity.getFlightSchedules()) {
                System.out.println("\tID: " + flightScheduleEntity.getFlightScheduleId() + " Airport: " + flightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getOriginAirport().getAirportName() + " Departure date: " + flightScheduleEntity.getDepartureDate() + "-->" + flightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportName() + " Arrival date: " + flightScheduleEntity.getArrivalDateTime());
//                System.out.println("\t\tSeat inventory> ");
//                List<SeatEntity> seats = flightScheduleEntity.getSeatInventory();
//                for (SeatEntity seatEntity : seats) {
//                    System.out.println("\t\tCabin: " + seatEntity.getCabinClassEnum() + " Seat number: " + seatEntity.getSeatNumber());
//                }
            }

            System.out.println("\tFares> ");
            for (FareEntity fareEntity : flightSchedulePlanEntity.getFares()) {
                System.out.println("\tFare basis code: " + fareEntity.getFareBasisCode() + " Amount: " + fareEntity.getFareAmount() + " Cabin class: " + fareEntity.getCabinClass());
            }

            if (flightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                System.out.println("Return flight schedule plan>");
                printReturnFlightSchedulePlan(flightSchedulePlanEntity.getReturnFlightSchedulePlan());
            }

        }
    }

    private void printReturnFlightSchedulePlan(FlightSchedulePlanEntity flightSchedulePlanEntity) {
        System.out.println("\tReturn flight schedule plan ID: " + flightSchedulePlanEntity.getFlightSchedulePlanId());
        System.out.println("\t\tReturn Flight schedules> ");

        for (FlightScheduleEntity flightScheduleEntity : flightSchedulePlanEntity.getFlightSchedules()) {
            System.out.println("\tID: " + flightScheduleEntity.getFlightScheduleId() + " Airport: " + flightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getOriginAirport().getAirportName() + " Departure date: " + flightScheduleEntity.getDepartureDate() + "-->" + flightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportName() + " Arrival date: " + flightScheduleEntity.getArrivalDateTime());
//            System.out.println("\t\t\tReturn Seat inventory> ");
//            List<SeatEntity> seats = flightScheduleEntity.getSeatInventory();
//            for (SeatEntity seatEntity : seats) {
//                System.out.println("\t\t\tCabin: " + seatEntity.getCabinClassEnum() + " Seat number: " + seatEntity.getSeatNumber());
//            }
        }
        System.out.println("\t\tReturn Fares> ");
        for (FareEntity fareEntity : flightSchedulePlanEntity.getFares()) {
            System.out.println("\t\tReturn Fare basis code: " + fareEntity.getFareBasisCode() + " Amount: " + fareEntity.getFareAmount() + " Cabin class: " + fareEntity.getCabinClass());
        }
    }

    private void deleteFlightSchedulePlan() {
        try {
            flightSchedulePlanSessionBeanRemote.deleteFlightSchedulePlanById(2l);
        } catch (FlightSchedulePlanNotFoundException | FlightSchedulePlanInUseException ex) {
            System.out.println(ex);
        }
    }

    private void updateFlightSchedulePlanEntity() {
        try {
            FlightSchedulePlanEntity flightSchedulePlanEntity = flightSchedulePlanSessionBeanRemote.retrieveFlightSchedulePlanById(2l);
            List<FlightScheduleEntity> flightSchedules = flightSchedulePlanEntity.getFlightSchedules();
            //test update remove
//            Iterator<FlightScheduleEntity> iter = flightSchedules.iterator();
//            while (iter.hasNext()) {
//                FlightScheduleEntity flightScheduleEntity = iter.next();
//                if (!flightScheduleEntity.getFlightScheduleId().equals(5l)) {
//                    iter.remove();
//                }
//            }
            // test add new flight schedule
            FlightScheduleEntity flightScheduleEntity = new FlightScheduleEntity();

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y");
            flightScheduleEntity.setDepartureDate(inputDateFormat.parse("05/11/2020"));
            flightScheduleEntity.setEstimatedFlightDuration(2);
            flightSchedules.add(flightScheduleEntity);
            viewAllFlightSchedulePlans();
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private void viewSeatInventory() {
        SeatInventory seatInventory;
        try {
            seatInventory = seatInventorySessionBeanRemote.viewSeatsInventoryByFlightScheduleId(11l);
            System.out.println("total avail seats: " + seatInventory.getTotalAvailSeats());
            System.out.println("total reserved seats: " + seatInventory.getTotalReservedSeats());
            System.out.println("total balanced seats: " + seatInventory.getTotalBalancedSeats());

            seatInventory.getCabinSeatsInventory().forEach((CabinClassEnum cabinClassEnum, Integer[] cabinSeat) -> {
                if (cabinSeat[0] == null) {
                    return;
                }
                System.out.println(cabinClassEnum);
                System.out.println("\tTotal avail for cabin " + cabinSeat[0]);
                System.out.println("\tTotal reserved for cabin " + cabinSeat[1]);
                System.out.println("\tTotal balanced for cabin " + cabinSeat[2]);

            });
        } catch (FlightScheduleNotFoundException ex) {
            System.out.println(ex);
        }

    }

}
