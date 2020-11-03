/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewFlightScheduleException;
import util.exception.CreateNewSeatInventoryException;

/*
recurrent flight schedule plan will auto generate the flight schedule

single and multiple flight schedules are generatd manually by user

 */
/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightScheduleSessionBean implements FlightScheduleSessionBeanRemote, FlightScheduleSessionBeanLocal {

    @EJB
    private SeatInventorySessionBeanLocal seatInventorySessionBeanLocal;

    @PersistenceContext
    private EntityManager em;

    @Override
    public void createNewFlightSchedule(FlightScheduleEntity flightScheduleEntity, FlightSchedulePlanEntity flightSchedulePlanEntity, FlightEntity flightEntity) throws CreateNewFlightScheduleException {
        if (flightSchedulePlanEntity == null) {
            throw new CreateNewFlightScheduleException("CreateNewFlightScheduleException: Invalid flight schedule plan!");
        }

        if (flightEntity == null) {
            throw new CreateNewFlightScheduleException("CreateNewFlightScheduleException: Invalid flight!");
        }

        // check whether the new flight schedule conflict with any of existing flight schedules for flight
        checkFlightSchedules(flightScheduleEntity, flightEntity);

        // associate flight schedule with flight schedule plan
        flightScheduleEntity.setFlightSchedulePlan(flightSchedulePlanEntity);

        // create seat inventory for the flight schedule
        try {
            seatInventorySessionBeanLocal.createSeatInventoryForFlightSchedule(flightScheduleEntity, flightEntity.getAircraftConfiguration());
        } catch (CreateNewSeatInventoryException ex) {
            throw new CreateNewFlightScheduleException(ex.toString());
        }

        validateFlightSchedule(flightScheduleEntity);

        // associate flight schedule plan with flight schedule
        flightSchedulePlanEntity.getFlightSchedules().add(flightScheduleEntity);
    }

    private void checkFlightSchedules(FlightScheduleEntity newFlightScheduleEntity, FlightEntity flightEntity) throws CreateNewFlightScheduleException {
        // retrieve all flight schedule plans associated with flight
        List<FlightSchedulePlanEntity> flightSchedulePlans = flightEntity.getFlightSchedulePlans();

        for (FlightSchedulePlanEntity flightSchedulePlanEntity : flightSchedulePlans) {
            // retrieve flight schedule for each flight schedule plan record
            List<FlightScheduleEntity> flightSchedules = flightSchedulePlanEntity.getFlightSchedules();
            // check if any flight schedule conflict with the new flight schedule
            for (FlightScheduleEntity flightScheduleEntity : flightSchedules) {
                if (checkConflictBetweenTwoSchedule(flightScheduleEntity, newFlightScheduleEntity)) {
                    throw new CreateNewFlightScheduleException("CreateNewFlightScheduleException: New flight schedule conflicts with existing flight schedule " + flightScheduleEntity.getFlightScheduleId() + " !");
                }
            }
        }
    }

    private boolean checkConflictBetweenTwoSchedule(FlightScheduleEntity flightScheduleEntity, FlightScheduleEntity newFlightScheduleEntity) {
        /*  conflict if (StartA <= EndB) and(EndA >= StartB)
            valid case 1: start new > end existing
                                        |---- new ------| 
            |--- existing -----|          
            
            valid case 2: end new < start existing
            |---- new -----|                       
                                        |--- existing ----|
         */
        return flightScheduleEntity.getDepartureDate().compareTo(newFlightScheduleEntity.getArrivalDateTime()) <= 0
                && flightScheduleEntity.getArrivalDateTime().compareTo(newFlightScheduleEntity.getDepartureDate()) >= 0;
    }

    private void validateFlightSchedule(FlightScheduleEntity flightScheduleEntity) throws CreateNewFlightScheduleException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<FlightScheduleEntity>> errors = validator.validate(flightScheduleEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFlightScheduleException("CreateNewFlightScheduleException: Invalid inputs!\n" + errorMessage);
        }
    }

}
