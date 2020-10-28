/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.InvalidInputException;

/**
 *
 * @author kiyon
 */
@Stateless
public class AircraftConfigurationSessionBean implements AircraftConfigurationSessionBeanRemote, AircraftConfigurationSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    public Long createNewAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration, List<Long> cabinConfigurations) throws CreateNewAircraftConfigurationException, InvalidInputException {

        String errorMessage = validateFields(aircraftConfiguration);
        if (errorMessage != null) {
            throw new InvalidInputException(errorMessage);
        } 
        
        Long totalCapacity = 0L;
        for (Long cabinConfigurationId : cabinConfigurations) {
            //set bidirectional relationship
            CabinConfigurationEntity cabinConfiguration = em.find(CabinConfigurationEntity.class, cabinConfigurationId);
            aircraftConfiguration.getCabinConfigurations().add(cabinConfiguration);
            cabinConfiguration.setAircraftConfiguration(aircraftConfiguration);
            
            totalCapacity += cabinConfiguration.getMaximumCabinSeatCapacity();
        }
        if (totalCapacity > aircraftConfiguration.getMaximumConfigurationSeatCapacity()) {
            throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Total number of seats for cabins exceed maximum seat capacity for aircraft configuration!");
        }
        
        try {
            em.persist(aircraftConfiguration);
            em.flush();
            return aircraftConfiguration.getAircraftConfigurationId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Aircraft configuration with same name already exists!");
            } else {
                throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: " + ex.getMessage());
            }
        }
    }

    public List<AircraftConfigurationEntity> retrieveAllAircraftConfiguration() {
        Query query = em.createQuery("SELECT a FROM AircraftConfigurationEntity a");
        List<AircraftConfigurationEntity> aircraftConfigurations = (List<AircraftConfigurationEntity>) query.getResultList();
        return aircraftConfigurations;
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private String validateFields(AircraftConfigurationEntity aircraftConfiguration) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<AircraftConfigurationEntity>> errors = validator.validate(aircraftConfiguration);

        String errorMessage = "";

        if (errors.isEmpty()) {
            return null;
        }
        for (ConstraintViolation error : errors) {
            errorMessage += error.getPropertyPath() + ": " + error.getInvalidValue() + " - " + error.getMessage() + "\n";
        }
        return errorMessage;
    }

    public AircraftConfigurationEntity retrieveAircraftTypeById(Long aircraftConfigurationId) throws AircraftConfigurationNotFoundException {
        if (aircraftConfigurationId == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: Invalid aircraft configuration ID!");
        }

        AircraftConfigurationEntity aircraftConfigurationEntity = em.find(AircraftConfigurationEntity.class, aircraftConfigurationId);
        if (aircraftConfigurationEntity == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: aircraft configuration with ID " + aircraftConfigurationId + " does not exist!");
        }
        return aircraftConfigurationEntity;
    }
}
