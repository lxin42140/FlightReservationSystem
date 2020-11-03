/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import javax.ejb.Local;
import util.exception.CreateNewCabinConfigurationException;

/**
 *
 * @author kiyon
 */
@Local
public interface CabinConfigurationEntitySessionBeanLocal {

    public void createNewCabinConfigurationForAircraftConfiguration(CabinConfigurationEntity cabinConfiguration, AircraftConfigurationEntity aircraftConfiguration) throws CreateNewCabinConfigurationException;

}
