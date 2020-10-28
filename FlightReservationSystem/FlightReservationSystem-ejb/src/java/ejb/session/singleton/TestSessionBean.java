/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AirportEntitySessionBeanLocal;
import ejb.session.stateless.FlightRouteSessionBeanLocal;
import entity.AirportEntity;
import entity.FlightRouteEntity;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.exception.CreateNewFlightRouteException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class TestSessionBean {

//    @EJB
//    private AirportEntitySessionBeanLocal airportEntitySessionBean;
//
//    @EJB
//    private FlightRouteSessionBeanLocal flightRouteSessionBean;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    @PostConstruct
    public void postConstruct() {
//        System.out.println("---------------TEST flightRouteSessionBean.createNewFlightRoute-----------------");
//        List<AirportEntity> airports = airportEntitySessionBean.retrieveAllAirports();
//        try {
//            flightRouteSessionBean.createNewFlightRouteWithReturnFlight(new FlightRouteEntity(airports.get(1), airports.get(2)));
//        } catch (CreateNewFlightRouteException ex) {
//            System.out.println(ex);
//        }
//        
//        List<FlightRouteEntity> flightRoutes = flightRouteSessionBean.retrieveAllFlightRoutes();
//        System.out.println(flightRoutes);
    }

}
