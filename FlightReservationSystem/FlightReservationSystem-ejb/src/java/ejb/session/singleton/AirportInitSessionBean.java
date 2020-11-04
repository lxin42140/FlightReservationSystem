/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AirportEntitySessionBeanLocal;
import entity.AirportEntity;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.exception.CreateNewAirportException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class AirportInitSessionBean {

    @EJB
    private AirportEntitySessionBeanLocal airportEntitySessionBean;

    @PostConstruct
    public void postConstruct() {
        if (!airportEntitySessionBean.retrieveAllAirports().isEmpty()) {
            return;
        }

        try {
            airportEntitySessionBean.createNewAirport(new AirportEntity("SIN", "Singapore Changi Airport", "Singapore", "Singapore", "Singapore", "Asia/Singapore"));

            airportEntitySessionBean.createNewAirport(new AirportEntity("TPE", "Taoyuan International Airport", "Taipei", "Taipei", "Taiwan", "Asia/Taipei"));

            airportEntitySessionBean.createNewAirport(new AirportEntity("HND", "Tokyo International Airport", "Tokyo", "Tokyo", "Japan", "Asia/Tokyo"));

            airportEntitySessionBean.createNewAirport(new AirportEntity("ICN", "Incheon International Airport", "Incheon", "Seoul", "Korea", "Asia/Seoul"));
            airportEntitySessionBean.createNewAirport(new AirportEntity("GMP", "Gimpo International Airport", "Gimpo", "Seoul", "Korea", "Asia/Seoul"));

            airportEntitySessionBean.createNewAirport(new AirportEntity("SYD", "Sydney International Airport", "Sidney", "New South Wales", "Australia", "Australia/NSW"));
            airportEntitySessionBean.createNewAirport(new AirportEntity("NSO", "Scone International Airport", "Scone", "New South Wales", "Australia", "Australia/NSW"));
        } catch (CreateNewAirportException ex) {
            System.out.println(ex);
        }
    }
}
