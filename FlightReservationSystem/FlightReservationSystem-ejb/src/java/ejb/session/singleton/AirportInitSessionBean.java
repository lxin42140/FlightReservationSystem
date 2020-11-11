/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import entity.AirportEntity;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
//@Startup
public class AirportInitSessionBean {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void postConstruct() {
        if (em.find(AirportEntity.class, 1l) != null) {
            return;
        }

        em.persist(new AirportEntity("SIN", "Singapore Changi Airport", "Singapore", "Singapore", "Singapore", "Asia/Singapore"));
        em.flush();
        em.persist(new AirportEntity("TPE", "Taoyuan International Airport", "Taipei", "Taipei", "Taiwan", "Asia/Taipei"));
        em.flush();
        em.persist(new AirportEntity("HND", "Tokyo International Airport", "Tokyo", "Tokyo", "Japan", "Asia/Tokyo"));
        em.flush();
        em.persist(new AirportEntity("ICN", "Incheon International Airport", "Incheon", "Seoul", "Korea", "Asia/Seoul"));
        em.flush();
        em.persist(new AirportEntity("GMP", "Gimpo International Airport", "Gimpo", "Seoul", "Korea", "Asia/Seoul"));
        em.flush();
        em.persist(new AirportEntity("SYD", "Sydney International Airport", "Sidney", "New South Wales", "Australia", "Australia/NSW"));
        em.flush();
        em.persist(new AirportEntity("NSO", "Scone International Airport", "Scone", "New South Wales", "Australia", "Australia/NSW"));
        em.flush();
    }
}
