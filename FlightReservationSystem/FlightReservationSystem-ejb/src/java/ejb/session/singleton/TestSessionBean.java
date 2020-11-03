/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftConfigurationSessionBeanLocal;
import ejb.session.stateless.AircraftTypeSessionBeanLocal;
import ejb.session.stateless.AirportEntitySessionBeanLocal;
import ejb.session.stateless.CabinConfigurationEntitySessionBeanLocal;
import ejb.session.stateless.FlightRouteSessionBeanLocal;
import ejb.session.stateless.FlightSessionBeanLocal;
import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import java.util.ArrayList;
import entity.AirportEntity;
import entity.CabinConfigurationEntity;
import entity.FlightEntity;
import entity.FlightNumberEntityKey;
import entity.FlightRouteEntity;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.enumeration.CabinClassEnum;
import util.exception.AircraftTypeNotFoundException;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.CreateNewCabinConfigurationException;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CabinConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.CreateNewFlightRouteException;
import util.exception.FlightNotFoundException;
import util.exception.FlightRouteNotFoundException;
import util.exception.InvalidInputException;
import util.exception.UpdateFlightFailedException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
@DependsOn({"AircraftTypeInitSessionBean", "AirportInitSessionBean", "EmployeeInitSessionBean", "PartnerInitSessionBean", "AircraftConfigurationInitSessionBean"})
public class TestSessionBean {

    @EJB
    private FlightSessionBeanLocal flightSessionBean;

    @EJB
    private CabinConfigurationEntitySessionBeanLocal cabinConfigurationEntitySessionBean;

    @EJB
    private AircraftTypeSessionBeanLocal aircraftTypeSessionBean;

    @EJB
    private AircraftConfigurationSessionBeanLocal aircraftConfigurationSessionBean;

    @EJB
    private AirportEntitySessionBeanLocal airportEntitySessionBean;

    @EJB
    private FlightRouteSessionBeanLocal flightRouteSessionBean;

    @EJB
    private AircraftConfigurationSessionBeanLocal aircraftConfigurationSessionBeanLocal;

    @EJB
    private CabinConfigurationEntitySessionBeanLocal cabinConfigurationEntitySessionBeanLocal;

    @PostConstruct
    public void postConstruct() {

        System.out.println("-----------------------TEST------------------------------\n");
        try {
            //createFlightRoute();
            //createFlight();
            //flightSessionBean.deleteFlightByFlightNumber("ML001");
            //updateFlight();
            //retrieveAllFlight();
        } catch (Exception ex) {
            System.out.println(ex);
        }

        //createAircraftConfig();
    }

    private void createFlightRoute() {
        try {
            flightRouteSessionBean.createNewFlightRoute(1l, 2l, true);
        } catch (CreateNewFlightRouteException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AirportNotFoundException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateFlight() {
        FlightEntity flightEntity;
        try {
            flightEntity = flightSessionBean.retrieveFlightByFlightNumber("ML001");
            //FlightRouteEntity flightRouteEntity = flightRouteSessionBean.retrieveFlightRouteById(2l);
            //flightEntity.setFlightRoute(flightRouteEntity);
            AircraftConfigurationEntity aircraftConfigurationEntity = aircraftConfigurationSessionBean.retrieveAircraftConfigurationById(2L);
            flightEntity.setAircraftConfiguration(aircraftConfigurationEntity);
            flightSessionBean.updateFlight(flightEntity);
        } catch (FlightNotFoundException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UpdateFlightFailedException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AircraftConfigurationNotFoundException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void retrieveAllFlight() {
        List<FlightEntity> list = flightSessionBean.retrieveAllFlights();
        for (FlightEntity flightEntity : list) {
            System.out.println(flightEntity.getFlightNumber());
        }
    }

    private void createFlight() {
        FlightEntity flightEntity = new FlightEntity("ML001");
        FlightEntity flightEntity1 = new FlightEntity("ML003");
        FlightEntity flightEntity2 = new FlightEntity("ML005");

        try {
           // flightSessionBean.createNewFlight(flightEntity, 2l, 1l, Boolean.TRUE, "ML002");
            flightSessionBean.createNewFlight(flightEntity1, 2l, 1l, Boolean.TRUE, "ML004");
            flightSessionBean.createNewFlight(flightEntity2, 2l, 1l, Boolean.FALSE, "ML006");
            
        } catch (CreateNewFlightException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FlightRouteNotFoundException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AircraftConfigurationNotFoundException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //TESTED AND SUCCEEDED
//    private void createAircraftConfig() {
//        List<AircraftConfigurationEntity> configs = aircraftConfigurationSessionBeanLocal.retrieveAllAircraftConfiguration();
//        //should print 2
//        System.out.println("aircraft config list size = " + configs.size());
//        try {
//            AircraftConfigurationEntity result = aircraftConfigurationSessionBeanLocal.retrieveAircraftConfigurationById(12L);
//            //should print 20
//            System.out.println("aircraft config seating capacity = " + result.getMaximumConfigurationSeatCapacity());
//        } catch (AircraftConfigurationNotFoundException ex) {
//            System.out.println(ex.getMessage());
//        }
//
//        List<CabinConfigurationEntity> cabinConfigs = cabinConfigurationEntitySessionBeanLocal.retrieveAllCabinConfiguration();
//        //should print 3
//        System.out.println("cabin config list size = " + cabinConfigs.size());
//        try {
//            CabinConfigurationEntity result = cabinConfigurationEntitySessionBeanLocal.retrieveCabinConfigurationById(11L);
//            //should print 80
//            System.out.println("cabin config seating capacity = " + result.getMaximumCabinSeatCapacity());
//        } catch (CabinConfigurationNotFoundException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }
}
