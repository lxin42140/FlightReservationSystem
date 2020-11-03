/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewCabinConfigurationException;

/**
 *
 * @author kiyon
 */
@Stateless
public class CabinConfigurationEntitySessionBean implements CabinConfigurationEntitySessionBeanRemote, CabinConfigurationEntitySessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public void createNewCabinConfigurationForAircraftConfiguration(CabinConfigurationEntity cabinConfiguration, AircraftConfigurationEntity aircraftConfiguration) throws CreateNewCabinConfigurationException {

        //calculate total number of seats for cabin
        Long seatCapacity = cabinConfiguration.getNumberOfSeatsAbreast() * cabinConfiguration.getNumberOfRows();
        cabinConfiguration.setMaximumCabinSeatCapacity(seatCapacity);

        //check if adding the cabin will exceed the maximum AIRCRAFT seat capacity
        Long aircraftTypeMaxCapacity = aircraftConfiguration.getAircraftType().getMaximumAircraftSeatCapacity();

        if (aircraftConfiguration.getMaximumConfigurationSeatCapacity() + seatCapacity > aircraftTypeMaxCapacity) {
            throw new CreateNewCabinConfigurationException("Exceed maximum seat capacity for aircraft configuration!");
        }

        // associate cabin config with aircraft config
        cabinConfiguration.setAircraftConfiguration(aircraftConfiguration);

        validateFields(cabinConfiguration);

        //associate aircraftconfig with cabin
        aircraftConfiguration.getCabinConfigurations().add(cabinConfiguration);

        //increase number of cabins by 1
        aircraftConfiguration.setNumberOfCabins(aircraftConfiguration.getNumberOfCabins() + 1);
        aircraftConfiguration.setMaximumConfigurationSeatCapacity(aircraftConfiguration.getMaximumConfigurationSeatCapacity() + seatCapacity);
    }

    private void validateFields(CabinConfigurationEntity aircraftConfiguration) throws CreateNewCabinConfigurationException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<CabinConfigurationEntity>> errors = validator.validate(aircraftConfiguration);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewCabinConfigurationException("CreateNewCabinConfigurationException: Invalid inputs!\n" + errorMessage);
        }
    }
}
