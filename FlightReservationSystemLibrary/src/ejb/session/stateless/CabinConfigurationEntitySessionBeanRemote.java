/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CabinConfigurationEntity;
import java.util.List;
import javax.ejb.Remote;
import util.exception.CabinConfigurationNotFoundException;
import util.exception.CreateNewCabinConfigurationException;
import util.exception.InvalidInputException;

/**
 *
 * @author kiyon
 */
@Remote
public interface CabinConfigurationEntitySessionBeanRemote {
    
    public Long createNewCabinConfiguration(CabinConfigurationEntity cabinConfiguration) throws CreateNewCabinConfigurationException, InvalidInputException;

    public List<CabinConfigurationEntity> retrieveAllCabinConfiguration();

    public CabinConfigurationEntity retrieveCabinConfigurationById(Long cabinConfigurationId) throws CabinConfigurationNotFoundException;
    
}
