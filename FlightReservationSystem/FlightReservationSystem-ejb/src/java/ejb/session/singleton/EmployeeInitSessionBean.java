/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import entity.AirportEntity;
import entity.EmployeeEntity;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import util.enumeration.EmployeeAccessRightEnum;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class EmployeeInitSessionBean {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void postConstruct() {
        if (em.find(AirportEntity.class, 1l) != null) {
            return;
        }

        em.persist(new EmployeeEntity("Li", "Xin", "user1", "password", EmployeeAccessRightEnum.ADMIN));
        em.flush();
        em.persist(new EmployeeEntity("Li", "Xin", "user2", "password", EmployeeAccessRightEnum.FLEETMANAGER));
        em.flush();
        em.persist(new EmployeeEntity("Li", "Xin", "user3", "password", EmployeeAccessRightEnum.ROUTEMANAGER));
        em.flush();
        em.persist(new EmployeeEntity("Li", "Xin", "user4", "password", EmployeeAccessRightEnum.SCHEDULEMANAGER));
        em.flush();
        em.persist(new EmployeeEntity("Li", "Xin", "user5", "password", EmployeeAccessRightEnum.SALESMANAGER));
        em.flush();

    }

}
