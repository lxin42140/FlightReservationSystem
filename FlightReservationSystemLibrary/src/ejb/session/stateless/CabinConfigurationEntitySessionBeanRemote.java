/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import javax.ejb.Remote;
import util.exception.CreateNewCabinConfigurationException;

/**
 *
 * @author kiyon
 */
@Remote
public interface CabinConfigurationEntitySessionBeanRemote {

    public void createNewCabinConfigurationForAircraftConfiguration(CabinConfigurationEntity cabinConfiguration, AircraftConfigurationEntity aircraftConfiguration) throws CreateNewCabinConfigurationException;

}
