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
import util.exception.CreateNewFlightRouteException;
import util.exception.InvalidInputException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
@DependsOn({"AircraftTypeInitSessionBean", "AirportInitSessionBean", "EmployeeInitSessionBean", "PartnerInitSessionBean"})
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

    @PostConstruct
    public void postConstruct() {
        createFlightRoute();
        System.out.println("-----------------------TEST------------------------------\n");
        try {

        } catch (Exception ex) {
            System.out.println(ex);
        }
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

}
