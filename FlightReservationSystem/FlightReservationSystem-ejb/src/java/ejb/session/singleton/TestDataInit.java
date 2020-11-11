/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftConfigurationSessionBeanLocal;
import ejb.session.stateless.FlightRouteSessionBeanLocal;
import ejb.session.stateless.FlightSchedulePlanSessionBeanLocal;
import ejb.session.stateless.FlightSessionBeanLocal;
import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import entity.AirportEntity;
import entity.CabinConfigurationEntity;
import entity.EmployeeEntity;
import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightRouteEntity;
import entity.FlightScheduleEntity;
import entity.PartnerEntity;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import util.enumeration.CabinClassEnum;
import util.enumeration.EmployeeAccessRightEnum;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.AircraftTypeNotFoundException;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.CreateNewFlightException;
import util.exception.CreateNewFlightRouteException;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightNotFoundException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup

public class TestDataInit {

    @EJB
    private FlightSchedulePlanSessionBeanLocal flightSchedulePlanSessionBeanLocal;

    @EJB
    private FlightSessionBeanLocal flightSessionBeanLocal;

    @EJB
    private FlightRouteSessionBeanLocal flightRouteSessionBeanLocal;

    @EJB
    private AircraftConfigurationSessionBeanLocal aircraftConfigurationSessionBeanLocal;

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void initData() {
        System.out.println("---------------------------------INIT---------------------------------");
        initEmployee();
        initPartner();
        initAirport();
        initAircraftType();
        initAircraftConfiguration();
        initFlightRoute();
        initFlight();
        initFlightSchedulePlan();
        System.out.println("---------------------------------END INIT---------------------------------");

    }

    private void initEmployee() {
        EmployeeEntity employee1 = new EmployeeEntity("Fleet", "Manager", "fleetmanager", "password", EmployeeAccessRightEnum.FLEETMANAGER);
        em.persist(employee1);

        EmployeeEntity employee2 = new EmployeeEntity("Route", "Planner", "routeplanner", "password", EmployeeAccessRightEnum.ROUTEMANAGER);
        em.persist(employee2);

        EmployeeEntity employee3 = new EmployeeEntity("Schedule", "Manager", "schedulemanager", "password", EmployeeAccessRightEnum.SCHEDULEMANAGER);
        em.persist(employee3);

        EmployeeEntity employee4 = new EmployeeEntity("Sales", "Manager", "salesmanager", "password", EmployeeAccessRightEnum.SALESMANAGER);
        em.persist(employee4);

        em.flush();
    }

    private void initPartner() {
        PartnerEntity partner = new PartnerEntity("Holiday.com", "holidaydotcom", "password");
        em.persist(partner);
        em.flush();
    }

    private void initAirport() {
        em.persist(new AirportEntity("SIN", "Changi", "Singapore", "Singapore", "Singapore", "Asia/Singapore"));
        em.flush();

        em.persist(new AirportEntity("HKG", "Hong Kong", "Chek Lap Kok", " Hong Kong", " China", "Asia/Hong_Kong"));
        em.flush();

        em.persist(new AirportEntity("TPE", "Taoyuan", "Taoyuan", "Taipei", "Taiwan R.O.C.", "Asia/Taipei"));
        em.flush();

        em.persist(new AirportEntity("NRT", "Narita", "Narita", "Chiba", "Japan", "Asia/Tokyo"));
        em.flush();

        em.persist(new AirportEntity("SYD", "Sydney", "Sydney", "New South Wales", "Australia", "Australia/NSW"));
        em.flush();
    }

    private void initAircraftType() {
        em.persist(new AircraftTypeEntity(200l, "Boeing 737"));
        em.flush();
        em.persist(new AircraftTypeEntity(400l, "Boeing 747"));
        em.flush();
    }

    private void initAircraftConfiguration() {
        try {
            AircraftConfigurationEntity allEconomy = new AircraftConfigurationEntity("Boeing 737 All Economy");
            CabinConfigurationEntity allEconomyc1 = new CabinConfigurationEntity(CabinClassEnum.Y, 1l, 30l, 6l, "3-3", 180l);
            List<CabinConfigurationEntity> list = new ArrayList<>();
            list.add(allEconomyc1);
            aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(allEconomy, list, 1l);

            list.clear();

            AircraftConfigurationEntity threeClasses = new AircraftConfigurationEntity("Boeing 737 Three Classes");
            CabinConfigurationEntity f1 = new CabinConfigurationEntity(CabinClassEnum.F, 1l, 5l, 2l, "1-1", 10l);
            CabinConfigurationEntity j1 = new CabinConfigurationEntity(CabinClassEnum.J, 1l, 5l, 4l, "2-2", 20l);
            CabinConfigurationEntity y1 = new CabinConfigurationEntity(CabinClassEnum.Y, 1l, 25l, 6l, "3-3", 150l);
            list.add(f1);
            list.add(j1);
            list.add(y1);
            aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(threeClasses, list, 1l);

            list.clear();

            AircraftConfigurationEntity allEconomy2 = new AircraftConfigurationEntity("Boeing 747 All Economy");
            CabinConfigurationEntity allEconomyc2 = new CabinConfigurationEntity(CabinClassEnum.Y, 2l, 38l, 10l, "3-4-3", 380l);
            list.add(allEconomyc2);
            aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(allEconomy2, list, 2l);

            list.clear();

            AircraftConfigurationEntity threeClasses2 = new AircraftConfigurationEntity("Boeing 747 Three Classes");
            CabinConfigurationEntity f2 = new CabinConfigurationEntity(CabinClassEnum.F, 1l, 5l, 2l, "1-1", 10l);
            CabinConfigurationEntity j2 = new CabinConfigurationEntity(CabinClassEnum.J, 2l, 5l, 6l, "2-2-2", 30l);
            CabinConfigurationEntity y2 = new CabinConfigurationEntity(CabinClassEnum.Y, 2l, 32l, 10l, "3-4-3", 320l);
            list.add(f2);
            list.add(j2);
            list.add(y2);
            aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(threeClasses2, list, 2l);
        } catch (CreateNewAircraftConfigurationException | AircraftTypeNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void initFlightRoute() {
        try {
            flightRouteSessionBeanLocal.createNewFlightRoute(1l, 2l, Boolean.TRUE); // SIN, HKG 2
            flightRouteSessionBeanLocal.createNewFlightRoute(1l, 3l, Boolean.TRUE); // SIN, TPE 4
            flightRouteSessionBeanLocal.createNewFlightRoute(1l, 4l, Boolean.TRUE); // SIN, NRT 6
            flightRouteSessionBeanLocal.createNewFlightRoute(2l, 4l, Boolean.TRUE); // HKG, NRT 8 
            flightRouteSessionBeanLocal.createNewFlightRoute(3l, 4l, Boolean.TRUE); // TPE, NRT 10
            flightRouteSessionBeanLocal.createNewFlightRoute(1l, 5l, Boolean.TRUE); // SIN, SYD 12
            flightRouteSessionBeanLocal.createNewFlightRoute(5l, 4l, Boolean.TRUE); // SYD, NRT 14
        } catch (AirportNotFoundException | CreateNewFlightRouteException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void initFlight() {
//Boeing 737 All Economy, 1, 180 //1
//Boeing 737 Three Classes, 3, 180  //2
//Boeing 747 All Economy, 1, 380  //3
//Boeing 747 Three Classes, 3, 360  //4
        try {
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML111"), 2l, 2l, Boolean.TRUE, "ML112");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML211"), 4l, 2l, Boolean.TRUE, "ML212");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML311"), 6l, 4l, Boolean.TRUE, "ML312");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML411"), 8l, 2l, Boolean.TRUE, "ML412");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML511"), 10l, 2l, Boolean.TRUE, "ML512");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML611"), 12l, 2l, Boolean.TRUE, "ML612");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML621"), 12l, 1l, Boolean.TRUE, "ML6222");
            flightSessionBeanLocal.createNewFlight(new FlightEntity("ML711"), 14l, 4l, Boolean.TRUE, "ML712");
        } catch (AircraftConfigurationNotFoundException | CreateNewFlightException | FlightRouteNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void initFlightSchedulePlan() {
        try {
// Sun 1
// M 2
// T 3
// W 4
// T 5
// F 6
// Sat 7
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("d/M/y HH:mm:ss");

            //ML711, Recurrent Weekly
            FlightScheduleEntity flight = new FlightScheduleEntity(14, 0);
            List<FareEntity> fares = new ArrayList<>();
//            fares.add(new FareEntity("F001", BigDecimal.valueOf(6500.0), CabinClassEnum.F));
//            fares.add(new FareEntity("F002", BigDecimal.valueOf(6000.0), CabinClassEnum.F));
//            fares.add(new FareEntity("J001", BigDecimal.valueOf(3500.0), CabinClassEnum.J));
//            fares.add(new FareEntity("J002", BigDecimal.valueOf(3000.0), CabinClassEnum.J));
//            fares.add(new FareEntity("Y001", BigDecimal.valueOf(1500.0), CabinClassEnum.Y));
//            fares.add(new FareEntity("Y002", BigDecimal.valueOf(1000.0), CabinClassEnum.Y));
//            flightSchedulePlanSessionBeanLocal.createRecurrentWeeklyFlightSchedulePlan(2, 9, 0, inputDateFormat.parse("01/12/2020 12:00:00"), inputDateFormat.parse("31/12/2020 12:00:00"), flight, fares, "ML711", Boolean.TRUE, 2);
//
//            fares.clear();
//
//            //ML611, Recurrent Weekly
//            flight = new FlightScheduleEntity(8, 0);
//            fares.add(new FareEntity("F001", BigDecimal.valueOf(3250.0), CabinClassEnum.F));
//            fares.add(new FareEntity("F002", BigDecimal.valueOf(3000.0), CabinClassEnum.F));
//            fares.add(new FareEntity("J001", BigDecimal.valueOf(1750.0), CabinClassEnum.J));
//            fares.add(new FareEntity("J002", BigDecimal.valueOf(1500.0), CabinClassEnum.J));
//            fares.add(new FareEntity("Y001", BigDecimal.valueOf(750.0), CabinClassEnum.Y));
//            fares.add(new FareEntity("Y002", BigDecimal.valueOf(500.0), CabinClassEnum.Y));
//            flightSchedulePlanSessionBeanLocal.createRecurrentWeeklyFlightSchedulePlan(1, 12, 0, inputDateFormat.parse("01/12/2020 12:00:00"), inputDateFormat.parse("31/12/2020 12:00:00"), flight, fares, "ML611", Boolean.TRUE, 2);
//
//            fares.clear();
//
//            //ML621, Recurrent Weekly
//            flight = new FlightScheduleEntity(8, 0);
//            fares.add(new FareEntity("Y001", BigDecimal.valueOf(700.0), CabinClassEnum.Y));
//            fares.add(new FareEntity("Y002", BigDecimal.valueOf(700.0), CabinClassEnum.Y));
//            flightSchedulePlanSessionBeanLocal.createRecurrentWeeklyFlightSchedulePlan(3, 10, 0, inputDateFormat.parse("01/12/2020 12:00:00"), inputDateFormat.parse("31/12/2020 12:00:00"), flight, fares, "ML621", Boolean.TRUE, 2);
//
//            fares.clear();
//
////            ML311, Recurrent Weekly
//            flight = new FlightScheduleEntity(6, 30);
//            fares.add(new FareEntity("F001", BigDecimal.valueOf(3350.0), CabinClassEnum.F));
//            fares.add(new FareEntity("F002", BigDecimal.valueOf(3100.0), CabinClassEnum.F));
//            fares.add(new FareEntity("J001", BigDecimal.valueOf(1850.0), CabinClassEnum.J));
//            fares.add(new FareEntity("J002", BigDecimal.valueOf(1600.0), CabinClassEnum.J));
//            fares.add(new FareEntity("Y001", BigDecimal.valueOf(850.0), CabinClassEnum.Y));
//            fares.add(new FareEntity("Y002", BigDecimal.valueOf(600.0), CabinClassEnum.Y));
//            flightSchedulePlanSessionBeanLocal.createRecurrentWeeklyFlightSchedulePlan(2, 10, 0, inputDateFormat.parse("01/12/2020 12:00:00"), inputDateFormat.parse("31/12/2020 12:00:00"), flight, fares, "ML311", Boolean.TRUE, 3);
//
//            fares.clear();

            //ML411, Recurrent NDay
            flight = new FlightScheduleEntity(inputDateFormat.parse("1/12/2020 13:00:00"), 4);
            fares.add(new FareEntity("F001", BigDecimal.valueOf(3150.0), CabinClassEnum.F));
            fares.add(new FareEntity("F002", BigDecimal.valueOf(2900.0), CabinClassEnum.F));
            fares.add(new FareEntity("J001", BigDecimal.valueOf(1650.0), CabinClassEnum.J));
            fares.add(new FareEntity("J002", BigDecimal.valueOf(1400.0), CabinClassEnum.J));
            fares.add(new FareEntity("Y001", BigDecimal.valueOf(650.0), CabinClassEnum.Y));
            fares.add(new FareEntity("Y002", BigDecimal.valueOf(400.0), CabinClassEnum.Y));
            flightSchedulePlanSessionBeanLocal.createRecurrentNDaysFlightSchedulePlan(inputDateFormat.parse("31/12/2020 13:00:00"), 2, flight, fares, "ML411", Boolean.TRUE, 4);

//            fares.clear();
//
//            List<FlightScheduleEntity> flights = new ArrayList<>();
//            FlightScheduleEntity m1 = new FlightScheduleEntity(inputDateFormat.parse("07/12/2020 17:00:00"), 3);
//            FlightScheduleEntity m2 = new FlightScheduleEntity(inputDateFormat.parse("08/12/2020 17:00:00"), 3);
//            FlightScheduleEntity m3 = new FlightScheduleEntity(inputDateFormat.parse("09/12/2020 17:00:00"), 3);
//            flights.add(m1);
//            flights.add(m2);
//            flights.add(m3);
//
//            fares.add(new FareEntity("F001", BigDecimal.valueOf(3100.0), CabinClassEnum.F));
//            fares.add(new FareEntity("F002", BigDecimal.valueOf(2850.0), CabinClassEnum.F));
//            fares.add(new FareEntity("J001", BigDecimal.valueOf(1600.0), CabinClassEnum.J));
//            fares.add(new FareEntity("J002", BigDecimal.valueOf(1350.0), CabinClassEnum.J));
//            fares.add(new FareEntity("Y001", BigDecimal.valueOf(600.0), CabinClassEnum.Y));
//            fares.add(new FareEntity("Y002", BigDecimal.valueOf(350.0), CabinClassEnum.Y));
//            flightSchedulePlanSessionBeanLocal.createNewNonRecurrentFlightSchedulePlan(flights, fares, "ML511", Boolean.TRUE, 2);
        } catch (CreateNewFlightSchedulePlanException | FlightNotFoundException | ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
