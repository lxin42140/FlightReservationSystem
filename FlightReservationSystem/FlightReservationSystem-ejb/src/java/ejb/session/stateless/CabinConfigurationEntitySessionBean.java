/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

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
import util.exception.CabinConfigurationNotFoundException;
import util.exception.CreateNewCabinConfigurationException;
import util.exception.InvalidInputException;

/**
 *
 * @author kiyon
 */
@Stateless
public class CabinConfigurationEntitySessionBean implements CabinConfigurationEntitySessionBeanRemote, CabinConfigurationEntitySessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    public Long createNewCabinConfiguration(CabinConfigurationEntity cabinConfiguration) throws CreateNewCabinConfigurationException, InvalidInputException {

        String errorMessage = validateFields(cabinConfiguration);
        if (errorMessage != null) {
            throw new InvalidInputException(errorMessage);
        }
        
        //calculate total number of seats for cabin
        cabinConfiguration.setMaximumCabinSeatCapacity(
                cabinConfiguration.getNumberOfSeatsAbreast() * cabinConfiguration.getNumberOfRows());
        
        try {
            em.persist(cabinConfiguration);
            em.flush();
            return cabinConfiguration.getCabinConfigurationId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewCabinConfigurationException("CreateNewCabinConfigurationException: Aircraft configuration with same name already exists!");
            } else {
                throw new CreateNewCabinConfigurationException("CreateNewCabinConfigurationException: " + ex.getMessage());
            }
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private String validateFields(CabinConfigurationEntity aircraftConfiguration) {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<CabinConfigurationEntity>> errors = validator.validate(aircraftConfiguration);

        String errorMessage = "";

        if (errors.isEmpty()) {
            return null;
        }
        for (ConstraintViolation error : errors) {
            errorMessage += error.getPropertyPath() + ": " + error.getInvalidValue() + " - " + error.getMessage() + "\n";
        }
        return errorMessage;
    }

    public List<CabinConfigurationEntity> retrieveAllCabinConfiguration() {
        Query query = em.createQuery("SELECT c FROM CabinConfigurationEntity c");
        List<CabinConfigurationEntity> cabinConfigurations = (List<CabinConfigurationEntity>) query.getResultList();
        return cabinConfigurations;
    }

    public CabinConfigurationEntity retrieveCabinConfigurationById(Long cabinConfigurationId) throws CabinConfigurationNotFoundException {
        if (cabinConfigurationId == null) {
            throw new CabinConfigurationNotFoundException("CabinConfigurationNotFoundException: Invalid cabin type ID!");
        }

        CabinConfigurationEntity cabinConfigurationEntity = em.find(CabinConfigurationEntity.class, cabinConfigurationId);
        if (cabinConfigurationEntity == null) {
            throw new CabinConfigurationNotFoundException("CabinConfigurationNotFoundException: cabin type with ID " + cabinConfigurationId + " does not exist!");
        }
        return cabinConfigurationEntity;
    }
}
