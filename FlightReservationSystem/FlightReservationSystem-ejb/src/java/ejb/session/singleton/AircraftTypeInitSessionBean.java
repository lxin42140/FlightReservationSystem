/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftTypeSessionBeanLocal;
import entity.AircraftTypeEntity;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.exception.CreateNewAircraftTypeException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class AircraftTypeInitSessionBean {

    @EJB
    private AircraftTypeSessionBeanLocal aircraftTypeSessionBean;

    @PostConstruct
    public void postConstruct() {
        if (!aircraftTypeSessionBean.retrieveAllAircraftTypes().isEmpty()) {
            return;
        }

        try {
            aircraftTypeSessionBean.createNewAircraftType(new AircraftTypeEntity(Long.valueOf(120), "Boeing 737"));
            aircraftTypeSessionBean.createNewAircraftType(new AircraftTypeEntity(Long.valueOf(660), "Boeing 747"));
            aircraftTypeSessionBean.createNewAircraftType(new AircraftTypeEntity(Long.valueOf(519), "Airbus A380"));
        } catch (CreateNewAircraftTypeException ex) {
            System.out.println(ex);
        }
    }
}
