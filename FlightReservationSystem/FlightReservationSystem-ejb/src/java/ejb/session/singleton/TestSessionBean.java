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
import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import java.util.ArrayList;
import entity.AirportEntity;
import entity.CabinConfigurationEntity;
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
import util.exception.CreateNewFlightRouteException;
import util.exception.InvalidInputException;

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

        createFlightRoute();
        System.out.println("-----------------------TEST------------------------------\n");
        try {

        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        createAircraftConfig();
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


        //TESTED AND SUCCEEDED
    private void createAircraftConfig() {
        List<AircraftConfigurationEntity> configs = aircraftConfigurationSessionBeanLocal.retrieveAllAircraftConfiguration();
        //should print 2
        System.out.println("aircraft config list size = " + configs.size());
        try {
            AircraftConfigurationEntity result = aircraftConfigurationSessionBeanLocal.retrieveAircraftConfigurationById(12L);
            //should print 20
            System.out.println("aircraft config seating capacity = " + result.getMaximumConfigurationSeatCapacity());
        } catch (AircraftConfigurationNotFoundException ex) {
            System.out.println(ex.getMessage());
        }

        List<CabinConfigurationEntity> cabinConfigs = cabinConfigurationEntitySessionBeanLocal.retrieveAllCabinConfiguration();
        //should print 3
        System.out.println("cabin config list size = " + cabinConfigs.size());
        try {
            CabinConfigurationEntity result = cabinConfigurationEntitySessionBeanLocal.retrieveCabinConfigurationById(11L);
            //should print 80
            System.out.println("cabin config seating capacity = " + result.getMaximumCabinSeatCapacity());
        } catch (CabinConfigurationNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
