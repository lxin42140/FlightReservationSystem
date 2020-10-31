/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import entity.CabinConfigurationEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
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
import util.exception.CreateNewCabinConfigurationException;
import util.exception.InvalidInputException;

/**
 *
 * @author kiyon
 */
@Stateless
public class AircraftConfigurationSessionBean implements AircraftConfigurationSessionBeanRemote, AircraftConfigurationSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;
    @EJB
    private CabinConfigurationEntitySessionBeanLocal cabinConfigurationEntitySessionBeanLocal;

    public Long createNewAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration, List<CabinConfigurationEntity> cabinConfigurations, Long aircraftTypeId) throws CreateNewAircraftConfigurationException {
        
        AircraftTypeEntity aircraftType = em.find(AircraftTypeEntity.class, aircraftTypeId);
        aircraftConfiguration.setAircraftType(aircraftType);
        
//        cabinConfigurationEntitySessionBeanLocal.createNewCabinConfiguration(cabinConfiguration, aircraftConfiguration);
        
        Long totalSeatingCapacity = 0L;
        for (CabinConfigurationEntity cabinConfiguration : cabinConfigurations) {
//            totalSeatingCapacity += cabinConfiguration.getNumberOfSeatsAbreast() * cabinConfiguration.getNumberOfRows();
//            if (totalSeatingCapacity > aircraftType.getMaximumAircraftSeatCapacity()) {
//                throw new CreateNewAircraftConfigurationException("Exceed maximum seat capacity for aircraft configuration!");
//            }
            try {
                cabinConfigurationEntitySessionBeanLocal.createNewCabinConfiguration(cabinConfiguration, aircraftConfiguration);
            } catch(CreateNewCabinConfigurationException ex) {
                throw new CreateNewAircraftConfigurationException(ex.getMessage());
            }
        }
        
        validateFields(aircraftConfiguration);

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

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validateFields(AircraftConfigurationEntity aircraftConfiguration) throws CreateNewAircraftConfigurationException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<AircraftConfigurationEntity>> errors = validator.validate(aircraftConfiguration);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += error.getPropertyPath() + ": " + error.getInvalidValue() + " - " + error.getMessage() + "\n";
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: " + errorMessage);
        }
    }

    public List<AircraftConfigurationEntity> retrieveAllAircraftConfiguration() {
        Query query = em.createQuery("SELECT a FROM AircraftConfigurationEntity a");
        List<AircraftConfigurationEntity> aircraftConfigurations = (List<AircraftConfigurationEntity>) query.getResultList();
        return aircraftConfigurations;
    }

    public AircraftConfigurationEntity retrieveAircraftConfigurationById(Long aircraftConfigurationId) throws AircraftConfigurationNotFoundException {
        if (aircraftConfigurationId == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: Invalid aircraft configuration ID!");
        }

        AircraftConfigurationEntity aircraftConfigurationEntity = em.find(AircraftConfigurationEntity.class, aircraftConfigurationId);
        if (aircraftConfigurationEntity == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: aircraft configuration with ID " + aircraftConfigurationId + " does not exist!");
        }
        aircraftConfigurationEntity.getCabinConfigurations().size();
        return aircraftConfigurationEntity;
    }
}
