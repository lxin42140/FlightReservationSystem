/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.AircraftConfigurationSessionBeanLocal;
import ejb.session.stateless.AircraftTypeSessionBeanLocal;
import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import entity.CabinConfigurationEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.enumeration.CabinClassEnum;
import util.exception.AircraftTypeNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;

/**
 *
 * @author kiyon
 */
@Singleton
@LocalBean
@Startup
public class AircraftConfigurationInitSessionBean {

    @EJB
    private AircraftConfigurationSessionBeanLocal aircraftConfigurationSessionBeanLocal;

    @EJB
    private AircraftTypeSessionBeanLocal aircraftTypeSessionBean;

    @PostConstruct
    public void postConstruct() {
        if (!aircraftConfigurationSessionBeanLocal.retrieveAllAircraftConfiguration().isEmpty()) {
            return;
        }

        List<AircraftTypeEntity> list = aircraftTypeSessionBean.retrieveAllAircraftTypes();
        Long aircraftTypeId = list.get(0).getAricraftId();

        AircraftConfigurationEntity firstAircraftConfig = new AircraftConfigurationEntity("SIAPremium");
        AircraftConfigurationEntity secondAircraftConfig = new AircraftConfigurationEntity("SIAEconomy");
        //AircraftConfigurationEntity thirdAircraftConfig = new AircraftConfigurationEntity("SIAPremium");
        //AircraftConfigurationEntity fourthAircraftConfig = new AircraftConfigurationEntity("SIABudget");

        CabinConfigurationEntity firstCabinConfig = new CabinConfigurationEntity(2L, 10L, 2L, 20L, "3-4-3", CabinClassEnum.F);
        CabinConfigurationEntity secondCabinConfig = new CabinConfigurationEntity(2L, 10L, 4L, 40L, "3-4-3", CabinClassEnum.J);
        CabinConfigurationEntity thirdCabinConfig = new CabinConfigurationEntity(2L, 10L, 8L, 80L, "3-4-3", CabinClassEnum.W);

        List<CabinConfigurationEntity> firstList = new ArrayList<>();
        firstList.add(firstCabinConfig);

        List<CabinConfigurationEntity> secondList = new ArrayList<>();
        secondList.add(secondCabinConfig);
        secondList.add(thirdCabinConfig);

//        List<CabinConfigurationEntity> thirdList = new ArrayList<>();
//        thirdList.add(firstCabinConfig);
//        thirdList.add(secondCabinConfig);
//        thirdList.add(thirdCabinConfig);
        try {
            aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(firstAircraftConfig, firstList, aircraftTypeId);
            aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(secondAircraftConfig, secondList, aircraftTypeId);

            //should print an error because aircraft configuration have same name (SUCCESS)
            //aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(thirdAircraftConfig, firstList, aircraftTypeId);
            //should print an error because exceed seat limit (SUCCESS)
            //aircraftConfigurationSessionBeanLocal.createNewAircraftConfiguration(fourthAircraftConfig, thirdList, aircraftTypeId);
        } catch (CreateNewAircraftConfigurationException | AircraftTypeNotFoundException ex) {
            System.out.println(ex);
        }

    }

}
