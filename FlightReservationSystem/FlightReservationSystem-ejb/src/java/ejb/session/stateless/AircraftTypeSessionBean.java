/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftTypeEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import util.exception.AircraftTypeNotFoundException;
import util.exception.CreateNewAircraftTypeException;
import util.exception.InvalidInputException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class AircraftTypeSessionBean implements AircraftTypeSessionBeanRemote, AircraftTypeSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public Long createNewAircraftType(AircraftTypeEntity newAircraftTypeEntity) throws CreateNewAircraftTypeException {
        validate(newAircraftTypeEntity);

        try {
            em.persist(newAircraftTypeEntity);
            em.flush();
            return newAircraftTypeEntity.getAricraftId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewAircraftTypeException("CreateNewAircraftTypeException: Aircraft type with same name already exists!");
            } else {
                throw new CreateNewAircraftTypeException("CreateNewAircraftTypeException: " + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new CreateNewAircraftTypeException("CreateNewAircraftTypeException: " + ex.getMessage());
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validate(AircraftTypeEntity aircraftTypeEntity) throws CreateNewAircraftTypeException {
        javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<AircraftTypeEntity>> errors = validator.validate(aircraftTypeEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }
        if (errorMessage.length() > 0) {
            throw new CreateNewAircraftTypeException("CreateNewAircraftTypeException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public List<AircraftTypeEntity> retrieveAllAircraftTypes() {
        Query query = em.createQuery("SELECT a FROM AircraftTypeEntity a");
        List<AircraftTypeEntity> aircraftTypes = (List<AircraftTypeEntity>) query.getResultList();
        return aircraftTypes;
    }

    @Override
    public AircraftTypeEntity retrieveAircraftTypeById(Long aircraftTypeId) throws AircraftTypeNotFoundException {
        if (aircraftTypeId == null) {
            throw new AircraftTypeNotFoundException("AirportNotFoundException: Invalid aircraft type ID!");
        }

        AircraftTypeEntity aircraftTypeEntity = em.find(AircraftTypeEntity.class, aircraftTypeId);
        if (aircraftTypeEntity == null) {
            throw new AircraftTypeNotFoundException("AircraftTypeNotFoundException: aircraft type with ID " + aircraftTypeId + " does not exist!");
        }
        return aircraftTypeEntity;
    }

}
