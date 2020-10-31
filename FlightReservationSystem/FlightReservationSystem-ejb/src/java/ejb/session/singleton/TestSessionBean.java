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
import util.exception.FlightRouteInUseException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class TestSessionBean {

    @EJB
    private AirportEntitySessionBeanLocal airportEntitySessionBean;

    @EJB
    private FlightRouteSessionBeanLocal flightRouteSessionBean;

    @PostConstruct
    public void postConstruct() {
        System.out.println("-----------------------TEST------------------------------\n");
        try {

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }

}
