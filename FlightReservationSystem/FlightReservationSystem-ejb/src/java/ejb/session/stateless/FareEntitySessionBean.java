/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightSchedulePlanEntity;
import java.util.Set;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewFareException;

/**
 *
 * @author kiyon
 */
@Stateless
public class FareEntitySessionBean implements FareEntitySessionBeanRemote, FareEntitySessionBeanLocal {

    @Override
    public void createFareForFlightSchedulePlan(FareEntity fare, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException {
        // associate fare with flight schedule plan
        fare.setFlightSchedulePlan(flightSchedulePlanEntity);

        validateFields(fare);

        // associate flight schedule plan with fare
        flightSchedulePlanEntity.getFares().add(fare);
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
