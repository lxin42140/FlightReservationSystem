/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import entity.AircraftTypeEntity;
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
@Startup
public class AircraftTypeInitSessionBean {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void postConstruct() {
        if (em.find(AircraftTypeEntity.class, 1l) != null) {
            return;
        }

        em.persist(new AircraftTypeEntity(Long.valueOf(200), "Boeing 737"));
        em.flush();
        em.persist(new AircraftTypeEntity(Long.valueOf(200), "Boeing 747"));
        em.flush();
        em.persist(new AircraftTypeEntity(Long.valueOf(200), "Airbus A380"));
        em.flush();
    }

}
