/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CabinConfigurationEntity;
import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.enumeration.CabinClassEnum;
import util.enumeration.FlightScheduleTypeEnum;
import util.exception.CreateNewFareException;
import util.exception.CreateNewFlightScheduleException;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightNotFoundException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightSchedulePlanSessionBean implements FlightSchedulePlanSessionBeanRemote, FlightSchedulePlanSessionBeanLocal {

    @EJB
    private FareEntitySessionBeanLocal fareEntitySessionBeanLocal;

    @EJB
    private FlightScheduleSessionBeanLocal flightScheduleSessionBeanLocal;

    @EJB
    private FlightSessionBeanLocal flightSessionBeanLocal;

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

 
    public FlightScheduleEntity createFlightSchedulePlanForSingleSchedule(FlightScheduleEntity flightScheduleEntity, List<FareEntity> fares, String flightNumber) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        // retrieve flight entity
        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        // ensure at least one fare for each cabin class
        validateFares(fares, flightEntity);

        // create new flight schedule plan record
        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();
        // set schedule type to single
        newFlightSchedulePlanEntity.setFlightSchedulePlanType(FlightScheduleTypeEnum.SINGLE);
        //associate flight schedule with flight
        newFlightSchedulePlanEntity.setFlight(flightEntity);

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedule(flightScheduleEntity, newFlightSchedulePlanEntity, flightEntity);
        } catch (CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        createFares(newFlightSchedulePlanEntity, fares);

        em.persist(newFlightSchedulePlanEntity);
        em.flush();

        return flightScheduleEntity;
    }

    public FlightSchedulePlanEntity createFlightSchedulePlanForMultipleSchedules(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        // retrieve flight
        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        validateFares(fares, flightEntity);

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();
        // set schedule type to multiple
        newFlightSchedulePlanEntity.setFlightSchedulePlanType(FlightScheduleTypeEnum.MULTIPLE);
        //associate flight schedule with flight
        newFlightSchedulePlanEntity.setFlight(flightEntity);

        // create multiple flight schedules
        createFlightSchedules(flightEntity, newFlightSchedulePlanEntity, flightSchedules);

        // add fares
        createFares(newFlightSchedulePlanEntity, fares);

        validateFlightSchedulePlan(newFlightSchedulePlanEntity);

        em.persist(newFlightSchedulePlanEntity);
        em.flush();

        return newFlightSchedulePlanEntity;
    }
    
    private void createFlightSchedules(FlightEntity flightEntity, FlightSchedulePlanEntity newFlightSchedulePlanEntity, List<FlightScheduleEntity> flightSchedules) throws CreateNewFlightSchedulePlanException {
        if (flightSchedules.isEmpty()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Please provide at least one flight schedule!");
        }

        // create flight schedule
        for (FlightScheduleEntity flightScheduleEntity : flightSchedules) {
            try {
                flightScheduleSessionBeanLocal.createNewFlightSchedule(flightScheduleEntity, newFlightSchedulePlanEntity, flightEntity);
            } catch (CreateNewFlightScheduleException ex) {
                throw new CreateNewFlightSchedulePlanException(ex.toString());
            }
        }
    }

    private void createFares(FlightSchedulePlanEntity newFlightSchedulePlanEntity, List<FareEntity> fares) throws CreateNewFlightSchedulePlanException {
        if (fares.isEmpty()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Please provide at least one fare!");
        }

        for (FareEntity fareEntity : fares) {
            try {
                fareEntitySessionBeanLocal.createFareForFlightSchedulePlan(fareEntity, newFlightSchedulePlanEntity);
            } catch (CreateNewFareException ex) {
                throw new CreateNewFlightSchedulePlanException(ex.toString());
            }
        }
    }

    private void validateFares(List<FareEntity> fares, FlightEntity flightEntity) throws CreateNewFlightSchedulePlanException {
        HashSet<CabinClassEnum> availableCabins = new HashSet<>();

        for (CabinConfigurationEntity cabinConfigurationEntity : flightEntity.getAircraftConfiguration().getCabinConfigurations()) {
            availableCabins.add(cabinConfigurationEntity.getCabinClass());
        }

        for (FareEntity fareEntity : fares) {
            availableCabins.remove(fareEntity.getCabinClass());
        }

        if (!availableCabins.isEmpty()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Please provide at least one fare for all available cabins!");
        }
    }

    private void validateFlightSchedulePlan(FlightSchedulePlanEntity newFlightSchedulePlanEntity) throws CreateNewFlightSchedulePlanException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<FlightSchedulePlanEntity>> errors = validator.validate(newFlightSchedulePlanEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Invalid inputs!\n" + errorMessage);
        }
    }

}
