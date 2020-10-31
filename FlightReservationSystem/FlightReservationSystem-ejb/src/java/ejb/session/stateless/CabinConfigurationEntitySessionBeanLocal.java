/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.CabinConfigurationNotFoundException;
import util.exception.CreateNewCabinConfigurationException;

/**
 *
 * @author kiyon
 */
@Local
public interface CabinConfigurationEntitySessionBeanLocal {

    public Long createNewCabinConfiguration(CabinConfigurationEntity cabinConfiguration, AircraftConfigurationEntity aircraftConfiguration) throws CreateNewCabinConfigurationException;

    public List<CabinConfigurationEntity> retrieveAllCabinConfiguration();

    public CabinConfigurationEntity retrieveCabinConfigurationById(Long cabinConfigurationId) throws CabinConfigurationNotFoundException;
}
