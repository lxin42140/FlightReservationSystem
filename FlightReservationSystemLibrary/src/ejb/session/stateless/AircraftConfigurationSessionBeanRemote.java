/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import java.util.List;
import javax.ejb.Remote;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.InvalidInputException;

/**
 *
 * @author kiyon
 */
@Remote
public interface AircraftConfigurationSessionBeanRemote {
    
    public Long createNewAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration, List<CabinConfigurationEntity> cabinConfigurations, Long airportTypeId) throws CreateNewAircraftConfigurationException;
    
    public List<AircraftConfigurationEntity> retrieveAllAircraftConfiguration();

    public AircraftConfigurationEntity retrieveAircraftConfigurationById(Long aircraftConfigurationId) throws AircraftConfigurationNotFoundException;

}
