/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftTypeEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.AircraftTypeNotFoundException;
import util.exception.CreateNewAircraftTypeException;

/**
 *
 * @author Li Xin
 */
@Local
public interface AircraftTypeSessionBeanLocal {

    public AircraftTypeEntity retrieveAircraftTypeById(Long aircraftTypeId) throws AircraftTypeNotFoundException;

    public List<AircraftTypeEntity> retrieveAllAircraftTypes();

    public Long createNewAircraftType(AircraftTypeEntity newAircraftTypeEntity) throws CreateNewAircraftTypeException;
    
}
