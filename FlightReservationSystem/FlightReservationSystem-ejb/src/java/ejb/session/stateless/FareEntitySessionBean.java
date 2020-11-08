/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CabinConfigurationEntity;
import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightSchedulePlanEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewFareException;
import util.exception.InvalidFareException;

/**
 *
 * @author kiyon
 */
@Stateless
public class FareEntitySessionBean implements FareEntitySessionBeanRemote, FareEntitySessionBeanLocal {

    @Override
    // local interface only
    public void createNewFare(FareEntity fare, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException {
        // bidirectional association
        fare.setFlightSchedulePlan(flightSchedulePlanEntity);
        flightSchedulePlanEntity.getFares().add(fare);

        validateFields(fare);
    }

    @Override
    public void createNewFares(List<FareEntity> fares, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException {
        if (fares.isEmpty()) {
            throw new CreateNewFareException("CreateNewFareException: Please provide at least one fare!");
        }

        try {
            validateFaresForFlight(fares, flightSchedulePlanEntity.getFlight());
        } catch (InvalidFareException ex) {
            throw new CreateNewFareException(ex.toString());
        }

        for (FareEntity fareEntity : fares) {
            this.createNewFare(fareEntity, flightSchedulePlanEntity);
        }
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

    private void validateFaresForFlight(List<FareEntity> fares, FlightEntity flightEntity) throws InvalidFareException {
        HashSet<CabinClassEnum> availableCabinClasses = new HashSet<>();
        HashSet<CabinClassEnum> allCabinClasses = new HashSet<>();
        HashMap<CabinClassEnum, HashSet<String>> classToBasisCode = new HashMap<>();

        for (CabinConfigurationEntity cabinConfigurationEntity : flightEntity.getAircraftConfiguration().getCabinConfigurations()) {
            availableCabinClasses.add(cabinConfigurationEntity.getCabinClass());
            allCabinClasses.add(cabinConfigurationEntity.getCabinClass());
            classToBasisCode.put(cabinConfigurationEntity.getCabinClass(), new HashSet<String>());
        }

        for (FareEntity fareEntity : fares) {
            if (!allCabinClasses.contains(fareEntity.getCabinClass())) {
                throw new InvalidFareException("InvalidFareException: Cabin class in fare does not exist in flight!");
            }

            if (!(fareEntity.getFareBasisCode().charAt(0) == 'F'
                    || fareEntity.getFareBasisCode().charAt(0) == 'J'
                    || fareEntity.getFareBasisCode().charAt(0) == 'W'
                    || fareEntity.getFareBasisCode().charAt(0) == 'Y')) {
                throw new InvalidFareException("InvalidFareException: Fare basis code needs to begin with cabin class!");
            }

            availableCabinClasses.remove(fareEntity.getCabinClass());

            if (classToBasisCode.get(fareEntity.getCabinClass()).contains(fareEntity.getFareBasisCode())) {
                throw new InvalidFareException("InvalidFareException: Fare basis code for a cabin should be different!");
            } else {
                classToBasisCode.get(fareEntity.getCabinClass()).add(fareEntity.getFareBasisCode());
            }
        }

        if (!availableCabinClasses.isEmpty()) {
            throw new InvalidFareException("InvalidFareException: Please provide at least one fare for all available cabins!");
        }
    }

}
