/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftConfigurationSessionBeanRemote;
import ejb.session.stateless.AircraftTypeSessionBeanRemote;
import ejb.session.stateless.CustomerSessionBeanRemote;
import ejb.session.stateless.FlightReservationSessionBeanRemote;
import ejb.session.stateless.FlightRouteSessionBeanRemote;
import ejb.session.stateless.FlightSchedulePlanSessionBeanRemote;
import ejb.session.stateless.FlightSearchSessionBeanRemote;
import ejb.session.stateless.FlightSessionBeanRemote;
import ejb.session.stateless.SeatInventorySessionBeanRemote;
import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import java.util.ArrayList;
import entity.CabinConfigurationEntity;
import entity.CreditCardEntity;
import entity.CustomerEntity;
import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightRouteEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
//@DependsOn({"AircraftTypeInitSessionBean", "AirportInitSessionBean", "EmployeeInitSessionBean", "PartnerInitSessionBean"})
public class TestSessionBean {

    @EJB(name = "CustomerSessionBeanRemtoe")
    private CustomerSessionBeanRemote customerSessionBeanRemtoe;

    @EJB
    private FlightReservationSessionBeanRemote flightReservationSessionBeanRemote;

    @EJB
    private FlightSearchSessionBeanRemote flightSearchSessionBeanRemote;

    @EJB
    private SeatInventorySessionBeanRemote seatInventorySessionBeanRemote;

    @EJB
    private FlightRouteSessionBeanRemote flightRouteSessionBeanRemote;

    @EJB
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
            //viewAllAircraftConfigurations();
            //
//            createFlightRoute();
            //viewAllFlightRoute();
            //deleteFlightRoute();
            //            
//            createFlight();
            //updateFlight();
            //viewAllFlights();
            //deleteFlight();
            //
//            createFlightSchedulePlan();
            //viewAllFlightSchedulePlans(); 
            //updateFlightSchedulePlan();
            //deleteFlightSchedulePlan();
            //updateFlightSchedulePlanEntity();
            //
            //viewSeatInventory();
            //search();
            //

//            viewAllFlightReservation();
            //makeReservation();
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

        CabinConfigurationEntity firstCabinConfig = new CabinConfigurationEntity(CabinClassEnum.F, 2L, 10L, 2L, "3-4-3", 20L);
        CabinConfigurationEntity secondCabinConfig = new CabinConfigurationEntity(CabinClassEnum.J,2L, 10L, 4L, "3-4-3", 40L);
        CabinConfigurationEntity thirdCabinConfig = new CabinConfigurationEntity(CabinClassEnum.W,2L, 10L, 8L, "3-4-3", 80L);

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
            flightRouteSessionBeanRemote.createNewFlightRoute(5l, 6l, true);
            flightRouteSessionBeanRemote.createNewFlightRoute(1l, 6l, true);;
            //flightRouteSessionBeanRemote.createNewFlightRoute(3l, 4l, false);
        } catch (CreateNewFlightRouteException | AirportNotFoundException ex) {
            System.out.println(ex.toString());
        }
    }

    private void viewAllFlightRoute() {
        List<FlightRouteEntity> list = flightRouteSessionBeanRemote.retrieveAllFlightRoutes();

        for (FlightRouteEntity flightRouteEntity : list) {
            System.out.println(flightRouteEntity.getFlightRouteId() + " " + flightRouteEntity.getOriginAirport().getAirportName() + "--->" + flightRouteEntity.getDestinationAirport().getAirportName());
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

        /*
  2 Singapore Changi Airport--->Gimpo International Airport|#]
  	RETURN FLIGHT ROUTE Gimpo International Airport--->Singapore Changi Airport|#]
  4 Gimpo International Airport--->Sydney International Airport|#]
  	RETURN FLIGHT ROUTE Sydney International Airport--->Gimpo International Airport|#]
  6 Singapore Changi Airport--->Sydney International Airport|#]
  	RETURN FLIGHT ROUTE Sydney International Airport--->Singapore Changi Airport|#]
        
        
        
         */
        FlightEntity flightEntity = new FlightEntity("ML001");
        FlightEntity flightEntity1 = new FlightEntity("ML003");
        FlightEntity flightEntity2 = new FlightEntity("ML005");
        try {

            System.out.println(flightSessionBeanRemote.createNewFlight(flightEntity, 2l, 3l, Boolean.TRUE, "ML002"));
            System.out.println(flightSessionBeanRemote.createNewFlight(flightEntity1, 4l, 3l, Boolean.TRUE, "ML004"));
            System.out.println(flightSessionBeanRemote.createNewFlight(flightEntity2, 6l, 3l, Boolean.TRUE, "ML006"));
//            System.out.println(flightSessionBeanRemote.createNewFlight(flightEntity, 2l, 3l, Boolean.TRUE, "ML002"));
//            System.out.println(flightSessionBean.createNewFlight(flightEntity1, 5l, 3l, Boolean.TRUE, "ML004"));
//            System.out.println(flightSessionBean.createNewFlight(flightEntity2, 3l, 4l, Boolean.FALSE, "ML006"));

        } catch (CreateNewFlightException | FlightRouteNotFoundException | AircraftConfigurationNotFoundException ex) {
            System.out.println(ex);
        }
    }

    private void updateFlight() {
        try {
            FlightEntity flightEntity = flightSessionBeanRemote.retrieveFlightByFlightNumber("ML555");
            //             flightSessionBeanRemote.updateFlightRoute(flightEntity, 2l);

            //            flightSessionBeanRemote.updateFlightNumber(flightEntity, "ML555", "ML666");
            //            System.out.println(flightEntity.getAircraftConfiguration().getAircraftConfigurationId());
            //            flightSessionBeanRemote.updateAircraftConfiguration(flightEntity, 1l);
            //            flightEntity = flightSessionBeanRemote.retrieveFlightByFlightNumber("ML001");
            //            System.out.println(flightEntity.getAircraftConfiguration().getAircraftConfigurationId());
        } catch (Exception ex) {
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
            flightSessionBeanRemote.deleteFlightByFlightNumber("ML111");
            flightSessionBeanRemote.deleteFlightByFlightNumber("ML222");

            // delete flight with return flight
            flightSessionBeanRemote.deleteFlightByFlightNumber("ML003");
            viewAllFlights();
            flightSessionBeanRemote.deleteFlightByFlightNumber("ML001");
            flightSessionBeanRemote.deleteFlightByFlightNumber("ML005");
        } catch (FlightNotFoundException | FlightInUseException ex) {
            System.out.println(ex);
        }
    }

    private void createFlightSchedulePlan() {
        try {
            /*
  ML001: Singapore Changi Airport--->Gimpo International Airport via SIABudget|#]
  	ML002: Gimpo International Airport--->Singapore Changi Airport via SIABudget|#]
  ML003: Gimpo International Airport--->Sydney International Airport via SIABudget|#]
  	ML004: Sydney International Airport--->Gimpo International Airport via SIABudget|#]
  ML005: Singapore Changi Airport--->Sydney International Airport via SIABudget|#]
  	ML006: Sydney International Airport--->Singapore Changi Airport via SIABudget|#]
             */
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y HH:mm:ss");

//            FlightScheduleEntity flightScheduleEntity = new FlightScheduleEntity();
//            flightScheduleEntity.setDepartureDate(inputDateFormat.parse("01/11/2020 00:00:00"));
//            
//            flightScheduleEntity.setEstimatedFlightDuration(2);
//            FlightScheduleEntity flightScheduleEntity1 = new FlightScheduleEntity();
//            
//            flightScheduleEntity1.setDepartureDate(inputDateFormat.parse("06/11/2020"));
//            flightScheduleEntity1.setEstimatedFlightDuration(3);
//            
//            List<FlightScheduleEntity> flightSchedules = new ArrayList<>();
//            flightSchedules.add(flightScheduleEntity);
//            //flightSchedules.add(flightScheduleEntity1);
//
//            FareEntity fareEntity3 = new FareEntity("W005", BigDecimal.valueOf(100.0), CabinClassEnum.W);
//            FareEntity fareEntity6 = new FareEntity("W006", BigDecimal.valueOf(200.0), CabinClassEnum.W);
//            
//            FareEntity fareEntity4 = new FareEntity("J005", BigDecimal.valueOf(100.0), CabinClassEnum.J);
//            FareEntity fareEntity7 = new FareEntity("J006", BigDecimal.valueOf(200.0), CabinClassEnum.J);
//            
//            FareEntity fareEntity5 = new FareEntity("F005", BigDecimal.valueOf(100.0), CabinClassEnum.F);
//            FareEntity fareEntity8 = new FareEntity("F006", BigDecimal.valueOf(200.0), CabinClassEnum.F);
//            
//            List<FareEntity> fares1 = new ArrayList<>();
//            fares1.add(fareEntity3);
//            fares1.add(fareEntity6);
//            
//            fares1.add(fareEntity4);
//            fares1.add(fareEntity7);
//            
//            fares1.add(fareEntity5);
//            fares1.add(fareEntity8);
//            
//            flightSchedulePlanSessionBeanRemote.createNewNonRecurrentFlightSchedulePlan(flightSchedules, fares1, "ML005", true);
//
            FlightScheduleEntity base = new FlightScheduleEntity();

            base.setDepartureDate(inputDateFormat.parse("1/11/2020 00:00:00"));
            base.setEstimatedFlightDurationHour(1);

            FareEntity fareEntity3 = new FareEntity("W001", BigDecimal.valueOf(10.0), CabinClassEnum.W);
            FareEntity fareEntity6 = new FareEntity("W002", BigDecimal.valueOf(20.0), CabinClassEnum.W);
            FareEntity fareEntity4 = new FareEntity("J001", BigDecimal.valueOf(10.0), CabinClassEnum.J);
            FareEntity fareEntity7 = new FareEntity("J002", BigDecimal.valueOf(20.0), CabinClassEnum.J);
            FareEntity fareEntity5 = new FareEntity("F001", BigDecimal.valueOf(10.0), CabinClassEnum.F);
            FareEntity fareEntity8 = new FareEntity("F002", BigDecimal.valueOf(20.0), CabinClassEnum.F);

            List<FareEntity> fares1 = new ArrayList<>();
            fares1.add(fareEntity3);
            fares1.add(fareEntity4);
            fares1.add(fareEntity5);
            fares1.add(fareEntity6);
            fares1.add(fareEntity7);
            fares1.add(fareEntity8);

//            flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(inputDateFormat.parse("5/11/2020 00:00:00"), 1, base, fares1, "ML003", Boolean.TRUE);
//            flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(inputDateFormat.parse("5/11/2020 00:00:00"), 1, base, fares1, "ML001", Boolean.TRUE);
//            flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(inputDateFormat.parse("3/11/2020 00:00:00"), 1, base, fares1, "ML005", Boolean.TRUE);
            flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(inputDateFormat.parse("6/11/2020 00:00:00"), 2, base, fares1, "ML001", Boolean.TRUE, 8);
            flightSchedulePlanSessionBeanRemote.createRecurrentFlightSchedulePlan(inputDateFormat.parse("30/12/2020 00:00:00"), 10, base, fares1, "ML003", Boolean.TRUE, 8);

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

    private void updateFlightSchedulePlan() {
        try {
//            HashSet<Long> set = new HashSet<>();
//            set.add(8l);
//            set.add(10l);
//
//            flightSchedulePlanSessionBeanRemote.updateRemoveFlightScheduleFromFlightSchedulePlan(2l, set);
//            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y");
//
//            FlightScheduleEntity flightScheduleEntity = new FlightScheduleEntity();
//            flightScheduleEntity.setDepartureDate(inputDateFormat.parse("05/11/2020"));
//            flightScheduleEntity.setEstimatedFlightDuration(2);
//
//            FlightScheduleEntity flightScheduleEntity1 = new FlightScheduleEntity();
//            flightScheduleEntity1.setDepartureDate(inputDateFormat.parse("06/11/2020"));
//            flightScheduleEntity1.setEstimatedFlightDuration(3);
//
//            List<FlightScheduleEntity> flightSchedules = new ArrayList<>();
//            flightSchedules.add(flightScheduleEntity);
//            flightSchedules.add(flightScheduleEntity1);
//
//            flightSchedulePlanSessionBeanRemote.updateAddFlightScheduleToFlightSchedulePlan(2l, flightSchedules, false);
//            flightSchedulePlanSessionBeanRemote.updateRecurrentFlightSchedulePlanParameters(4l, null, 1);
//            viewAllFlightSchedulePlans();
//            FlightSchedulePlanEntity flightSchedulePlan = flightSchedulePlanSessionBeanRemote.retrieveFlightSchedulePlanById(6l);
//            List<FlightScheduleEntity> flightSchedules = flightSchedulePlan.getFlightSchedules();
//            for (FlightScheduleEntity flightScheduleEntity : flightSchedules) {
//                if (flightScheduleEntity.getFlightScheduleId().equals(71l)) {
//                    Date date = inputDateFormat.parse("11/11/2020");
//                    flightScheduleEntity.setDepartureDate(date);
//                    flightScheduleEntity.setEstimatedFlightDuration(1);
//                }
//            }
//
//            flightSchedulePlanSessionBeanRemote.updateFlightScheduleDetailForNonRecurrentFlightSchedulePlan(6l, flightSchedules);
//            FlightSchedulePlanEntity flightSchedulePlan = flightSchedulePlanSessionBeanRemote.retrieveFlightSchedulePlanById(5l);
//            List<FareEntity> fares = flightSchedulePlan.getFares();
//            for (FareEntity fare : fares) {
//                fare.setFareAmount(BigDecimal.valueOf(20.0));
//            }
//            HashSet<Long> set = new HashSet<>();
//            set.add(26l);
//            flightSchedulePlanSessionBeanRemote.updateRemoveFareFromFlightSchedulePlan(6l, set);
//            FlightSchedulePlanEntity flightSchedulePlan = flightSchedulePlanSessionBeanRemote.retrieveFlightSchedulePlanById(6l);
//            FareEntity fareEntity1 = new FareEntity("F001", BigDecimal.valueOf(300.0), CabinClassEnum.F);
//            FareEntity fareEntity2 = new FareEntity("F002", BigDecimal.valueOf(300.0), CabinClassEnum.F);
//            FareEntity fareEntity3 = new FareEntity("F003", BigDecimal.valueOf(300.0), CabinClassEnum.F);
//
//            List<FareEntity> fares1 = new ArrayList<>();
//            fares1.add(fareEntity1);
//            fares1.add(fareEntity2);
//            fares1.add(fareEntity3);
//            
//            flightSchedulePlanSessionBeanRemote.updateAddFareToFlightSchedulePlan(6l, fares1);
        } catch (Exception ex) {
            System.out.println(ex);

        }
    }

    private void deleteFlightSchedulePlan() {
        try {
            flightSchedulePlanSessionBeanRemote.deleteFlightSchedulePlanById(6l);
        } catch (FlightSchedulePlanNotFoundException | FlightSchedulePlanInUseException ex) {
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

    private void search() {
        try {
//        1 -> 5
//        ID: 76 Airport: Singapore Changi Airport Departure date: Sun Nov 01 00:00:00 SGT 2020-->Gimpo International Airport Arrival date: Sun Nov 01 02:00:00 SGT 2020|#]            ID: 73 Airport: Gimpo International Airport Departure date: Thu Nov 05 10:00:00 SGT 2020-->Singapore Changi Airport Arrival date: Thu Nov 05 11:00:00 SGT 2020|#]
//             ID: 75 Airport: Gimpo International Airport Departure date: Sun Nov 01 10:00:00 SGT 2020-->Singapore Changi Airport Arrival date: Sun Nov 01 11:00:00 SGT 2020|#]
//        
//        5 -> 6
//        ID: 80 Airport: Gimpo International Airport Departure date: Sun Nov 01 05:00:00 SGT 2020-->Sydney International Airport Arrival date: Sun Nov 01 10:00:00 SGT 2020|#]
//            ID: 79 Airport: Sydney International Airport Departure date: Sun Nov 01 18:00:00 SGT 2020-->Gimpo International Airport Arrival date: Sun Nov 01 21:00:00 SGT 2020|#]
//
//        1 -> 6
//        ID: 82 Airport: Singapore Changi Airport Departure date: Sun Nov 01 00:00:00 SGT 2020-->Sydney International Airport Arrival date: Sun Nov 01 05:00:00 SGT 2020|#]
//            ID: 81 Airport: Sydney International Airport Departure date: Sun Nov 01 13:00:00 SGT 2020-->Singapore Changi Airport Arrival date: Sun Nov 01 15:00:00 SGT 2020|#]
//             
            //one way test
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y");

            HashMap<Integer, List<FlightScheduleEntity>> oneWayFlights = flightSearchSessionBeanRemote.searchOneWayFlights(1l, 6l, inputDateFormat.parse("02/11/2020"), 1, null, null);
            for (int i = 1; i <= oneWayFlights.size(); i++) {
                System.out.print("S/N: " + i);
                List<FlightScheduleEntity> routes = oneWayFlights.get(i);
                for (FlightScheduleEntity flightSchedule : routes) {
                    System.out.print("[" + flightSchedule.getFlightScheduleId() + "] " + flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getOriginAirport().getAirportName() + "-->" + flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportName());
                }
                System.out.println("========================");
            }

//            HashMap<Integer, List<List<FlightScheduleEntity>>> twoWayFlights = flightSearchSessionBeanRemote.searchTwoWaysFlights(1l, 6l, inputDateFormat.parse("02/11/2020"), inputDateFormat.parse("03/11/2020"), 1, null, null);
//            List<List<FlightScheduleEntity>> toFlights = twoWayFlights.get(1);
//            List<List<FlightScheduleEntity>> returnFlights = twoWayFlights.get(2);
//
//            System.out.println("TO========================================");
//            for (int i = 0; i < toFlights.size(); i++) {
//                System.out.print(i + " ");
//                List<FlightScheduleEntity> routes = toFlights.get(i);
//                for (FlightScheduleEntity route : routes) {
//                    System.out.print(route.getFlightScheduleId() + " -> ");
//                }
//                System.out.println("-------------------------");
//            }
//
//            System.out.println("RETURN==========================");
//            for (int i = 0; i < returnFlights.size(); i++) {
//                System.out.print(i + " ");
//                List<FlightScheduleEntity> routes = returnFlights.get(i);
//                for (FlightScheduleEntity route : routes) {
//                    System.out.print(route.getFlightScheduleId() + " -> ");
//                }
//                System.out.println("-------------------------");
//            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

//    private void viewAllFlightReservation() {
//        try {
//            List<SeatEntity> seats = flightReservationSessionBeanRemote.viewFlightReservationsByFlightScheduleId(75l);
//            seats.forEach(x -> System.out.println(x.getSeatNumber()));
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//    }
    private void makeReservation() {
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y");

            HashMap<Integer, List<FlightScheduleEntity>> oneWayFlights = flightSearchSessionBeanRemote.searchOneWayFlights(1l, 6l, inputDateFormat.parse("01/11/2020"), 1, null, null);
            for (int i = 1; i <= oneWayFlights.size(); i++) {
                System.out.print("S/N: " + i);
                List<FlightScheduleEntity> routes = oneWayFlights.get(i);
                for (FlightScheduleEntity flightSchedule : routes) {
                    System.out.print("[" + flightSchedule.getFlightScheduleId() + "] " + flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getOriginAirport().getAirportName() + "-->" + flightSchedule.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportName());
                }
                System.out.println("========================");
            }

            CustomerEntity customer = customerSessionBeanRemtoe.retrieveCustomerByUsernameAndPassword("Li Xin", "111");
            List<PassengerEntity> passengers = new ArrayList<>();
            PassengerEntity passenger1 = new PassengerEntity("First", "Customer", "000000");
            passenger1.getSeats().add(seatInventorySessionBeanRemote.retrieveAvailableSeatFromFlightScheduleAndCabin(2l, CabinClassEnum.J, "1B"));
            passenger1.getSeats().add(seatInventorySessionBeanRemote.retrieveAvailableSeatFromFlightScheduleAndCabin(2l, CabinClassEnum.W, "2B"));
            passengers.add(passenger1);
            CreditCardEntity creditCard = new CreditCardEntity("123", "First", "Customer", inputDateFormat.parse("02/11/2020 00:00:00"), "222");

            flightReservationSessionBeanRemote.createNewFlightReservation(oneWayFlights.get(1), passengers, creditCard, customer);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

}
