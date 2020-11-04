/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public Long createNewNonRecurrentFlightSchedulePlan(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        if (flightSchedules != null && !flightSchedules.isEmpty()
                && fares != null && fares.isEmpty()
                && flightNumber != null && !flightNumber.isEmpty()
                && doCreateReturnFlightSchedule != null) {

            FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

            FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();

            em.persist(newFlightSchedulePlanEntity);

            newFlightSchedulePlanEntity.setFlight(flightEntity);
            flightEntity.getFlightSchedulePlans().add(newFlightSchedulePlanEntity);

            try {
                fareEntitySessionBeanLocal.createNewFares(fares, newFlightSchedulePlanEntity);
                flightScheduleSessionBeanLocal.createNewFlightSchedules(newFlightSchedulePlanEntity, flightSchedules);
            } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
                throw new CreateNewFlightSchedulePlanException(ex.toString());
            }

            validateFlightSchedulePlan(newFlightSchedulePlanEntity);

            if (doCreateReturnFlightSchedule) {
                newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(createReturnFlightSchedulePlan(newFlightSchedulePlanEntity));
            }

            em.flush();

            return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } else {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Missing flight schedules, fares, flight number or indicator for creating return flight schedule plan!");
        }
    }

    public Long createRecurrentFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();

        em.persist(newFlightSchedulePlanEntity);

        newFlightSchedulePlanEntity.setFlight(flightEntity); //associate flight schedule with flight

        List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>();
        autoGenerateFlightSchedules.add(baseFlightSchedule); // add base flight schedule

        Date startDate = baseFlightSchedule.getDepartureDate();

        // while start date is smaller or equals to end date
        while (startDate.compareTo(endDate) <= 0) {
            GregorianCalendar autoCalender = new GregorianCalendar();
            autoCalender.setTime(startDate);
            autoCalender.add(GregorianCalendar.HOUR_OF_DAY, recurrentDaysFrequency * 24);
            startDate = autoCalender.getTime();
            // generate new flight schedule depending on the date
            autoGenerateFlightSchedules.add(new FlightScheduleEntity(startDate, baseFlightSchedule.getEstimatedFlightDuration(), newFlightSchedulePlanEntity));
        }

        try {
            fareEntitySessionBeanLocal.createNewFares(fares, newFlightSchedulePlanEntity);
            flightScheduleSessionBeanLocal.createNewFlightSchedules(newFlightSchedulePlanEntity, autoGenerateFlightSchedules);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        if (doCreateReturnFlightSchedule) {
            newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(createReturnFlightSchedulePlan(newFlightSchedulePlanEntity));
        }

        return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

    private FlightSchedulePlanEntity createReturnFlightSchedulePlan(FlightSchedulePlanEntity newFlightSchedulePlanEntity) throws CreateNewFlightSchedulePlanException {
        FlightEntity returnFlight = newFlightSchedulePlanEntity.getFlight().getReturnFlight(); // add return flight schedule plan for return flight

        FlightSchedulePlanEntity returnFlightSchedulePlanEntity = new FlightSchedulePlanEntity(); // create return flight schedule plan
        returnFlightSchedulePlanEntity.setIsReturnFlightSchedulePlan(true); // set return flight schedule plan as true

        returnFlightSchedulePlanEntity.setFlight(returnFlight); //associate flight schedule with return flight
        returnFlight.getFlightSchedulePlans().add(returnFlightSchedulePlanEntity);

        List<FlightScheduleEntity> returnFlightSchedules = new ArrayList<>();

        for (FlightScheduleEntity flightSchedule : newFlightSchedulePlanEntity.getFlightSchedules()) { // create return flight schedule for each flight schedule
            Date arrivalDateTime = flightSchedule.getArrivalDateTime(); // arrival time is already calculated based on time zone of destination airport
            GregorianCalendar returnDepartureDateTimeCalender = new GregorianCalendar();
            returnDepartureDateTimeCalender.setTime(arrivalDateTime);
            returnDepartureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, 8); // lay over of 8 hours

            Date returnDepartureDateTime = returnDepartureDateTimeCalender.getTime();

            FlightScheduleEntity returnFlightSchedule = new FlightScheduleEntity(returnDepartureDateTime, flightSchedule.getEstimatedFlightDuration(), returnFlightSchedulePlanEntity);
            returnFlightSchedules.add(returnFlightSchedule);
        }

        List<FareEntity> returnFares = new ArrayList<>();
        for (FareEntity fare : newFlightSchedulePlanEntity.getFares()) { // create new fare for return flight schedule plan
            returnFares.add(new FareEntity(fare.getFareBasisCode(), fare.getFareAmount(), fare.getCabinClass(), returnFlightSchedulePlanEntity));
        }

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlanEntity, returnFlightSchedules);
            fareEntitySessionBeanLocal.createNewFares(returnFares, returnFlightSchedulePlanEntity);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        return returnFlightSchedulePlanEntity;
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
