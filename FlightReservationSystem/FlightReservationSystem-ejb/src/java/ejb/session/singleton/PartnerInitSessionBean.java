/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import entity.AirportEntity;
import entity.PartnerEntity;
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
public class PartnerInitSessionBean {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void postConstruct() {
        if (em.find(AirportEntity.class, 1l) != null) {
            return;
        }

        em.persist(new PartnerEntity("Holiday Reservation.com", "partner1", "password"));
        em.flush();
    }
}
