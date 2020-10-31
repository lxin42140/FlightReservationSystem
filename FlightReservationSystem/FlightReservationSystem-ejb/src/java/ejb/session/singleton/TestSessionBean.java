/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftConfigurationSessionBeanLocal;
import ejb.session.stateless.AirportEntitySessionBeanLocal;
import ejb.session.stateless.FlightRouteSessionBeanLocal;
import entity.AirportEntity;
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
import util.exception.AirportNotFoundException;
import util.exception.CreateNewFlightRouteException;
import util.exception.FlightRouteInUseException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
@DependsOn({ "AircraftTypeInitSessionBean", "AirportInitSessionBean", "EmployeeInitSessionBean", "PartnerInitSessionBean" })
public class TestSessionBean {

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
            flightRouteSessionBean.createNewFlightRoute(1l, 2l, Boolean.TRUE);
        } catch (CreateNewFlightRouteException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AirportNotFoundException ex) {
            Logger.getLogger(TestSessionBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createFlight() {
        
    }
   

}
