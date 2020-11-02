/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CabinConfigurationEntity;
import entity.FareEntity;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewCabinConfigurationException;
import util.exception.CreateNewFareException;

/**
 *
 * @author kiyon
 */
@Stateless
public class FareEntitySessionBean implements FareEntitySessionBeanRemote, FareEntitySessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    public Long createNewFare(FareEntity fare, Long flightSchedulePlanId) throws CreateNewFareException {

        //set bidirectional relationship between fare and flight schedule plan
        //uncomment when flightscheduleplan is created.
//        FlightSchedulePlan flightSchedulePlan = em.find(FlightSchedulePlan.class, flightSchedulePlanId);
//        flightSchedulePlan.getFares().add(fare);
//        fare.setFlightSchedulePlan(flightSchedulePlan);

        validateFields(fare);

        try {
            em.persist(fare);
            em.flush();
            return fare.getFareId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewFareException("CreateNewFareException: Fare with same fare basis code already exists!");
            } else {
                throw new CreateNewFareException("CreateNewFareException: " + ex.getMessage());
            }
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validateFields(FareEntity fare) throws CreateNewFareException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<FareEntity>> errors = validator.validate(fare);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFareException("CreateFareException: Invalid inputs!\n" + errorMessage);
        }
    }
    
    
}
