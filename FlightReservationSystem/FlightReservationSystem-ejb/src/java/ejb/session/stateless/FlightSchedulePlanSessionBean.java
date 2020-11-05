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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewFareException;
import util.exception.CreateNewFlightScheduleException;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightNotFoundException;
import util.exception.FlightSchedulePlanInUseException;
import util.exception.FlightSchedulePlanNotFoundException;
import util.exception.UpdateFlightSchedulePlanFailedException;

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

    @Override
    public Long createNewNonRecurrentFlightSchedulePlan(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {

        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        if (flightEntity.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight is disabled!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();

        em.persist(newFlightSchedulePlanEntity);

        // bi directional schedule plan and flight assoication
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
    }

    @Override
    public Long createRecurrentFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        if (flightEntity.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight is disabled!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();

        em.persist(newFlightSchedulePlanEntity);

        newFlightSchedulePlanEntity.setFlight(flightEntity); //associate flight schedule with flight

        List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules
        autoGenerateFlightSchedules.add(baseFlightSchedule); // add base flight schedule

        Date startDate = baseFlightSchedule.getDepartureDate(); // base start date

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

        em.flush();

        return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

    private FlightSchedulePlanEntity createReturnFlightSchedulePlan(FlightSchedulePlanEntity newFlightSchedulePlanEntity) throws CreateNewFlightSchedulePlanException {
        FlightEntity returnFlight = newFlightSchedulePlanEntity.getFlight().getReturnFlight(); // add return flight schedule plan for return flight

        if (returnFlight.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Return flight is disabled!");
        }

        FlightSchedulePlanEntity returnFlightSchedulePlanEntity = new FlightSchedulePlanEntity(); // create return flight schedule plan
        returnFlightSchedulePlanEntity.setIsReturnFlightSchedulePlan(true); // set return flight schedule plan as true

        //bi directional between return flight and return flight schedule plan
        returnFlightSchedulePlanEntity.setFlight(returnFlight);
        returnFlight.getFlightSchedulePlans().add(returnFlightSchedulePlanEntity);

        List<FlightScheduleEntity> returnFlightSchedules = new ArrayList<>(); // store new flight schedules for return flight

        for (FlightScheduleEntity flightSchedule : newFlightSchedulePlanEntity.getFlightSchedules()) { // create return flight schedule for each flight schedule
            Date arrivalDateTime = flightSchedule.getArrivalDateTime(); // arrival time is already calculated based on time zone of destination airport
            GregorianCalendar returnDepartureDateTimeCalender = new GregorianCalendar();
            returnDepartureDateTimeCalender.setTime(arrivalDateTime);
            returnDepartureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, 8); // lay over of 8 hours

            Date returnDepartureDateTime = returnDepartureDateTimeCalender.getTime();

            FlightScheduleEntity returnFlightSchedule = new FlightScheduleEntity(returnDepartureDateTime, flightSchedule.getEstimatedFlightDuration(), returnFlightSchedulePlanEntity);
            returnFlightSchedules.add(returnFlightSchedule);
        }

        List<FareEntity> returnFares = new ArrayList<>(); // store new instances of fare for return flight

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

    public Long updateFlightSchedulePlan(FlightSchedulePlanEntity updatedFlightSchedulePlanEntity) throws FlightSchedulePlanNotFoundException, UpdateFlightSchedulePlanFailedException {

        FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(updatedFlightSchedulePlanEntity.getFlightSchedulePlanId());

        if (existingFlightSchedulePlanEntity.getIsDisabled()) {
            throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
        }

        // deleting one or more flight schedules
        if (updatedFlightSchedulePlanEntity.getFlightSchedules().size() < existingFlightSchedulePlanEntity.getFlightSchedules().size()) {
            updateRemoveFlightSchedule(updatedFlightSchedulePlanEntity.getFlightSchedules(), existingFlightSchedulePlanEntity.getFlightSchedules());
        }

        // update fare amount
        for (FareEntity updatedFare : updatedFlightSchedulePlanEntity.getFares()) {
            for (FareEntity existingFare : existingFlightSchedulePlanEntity.getFares()) {
                // fare amount in updated fare is different
                if (existingFare.equals(updatedFare) && !existingFare.getFareAmount().equals(updatedFare.getFareAmount())) {
                    existingFare.setFareAmount(updatedFare.getFareAmount()); // update new fare
                }
            }
        }

        em.flush();

        return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

    private void updateRemoveFlightSchedule(List<FlightScheduleEntity> updatedFlightSchedules, List<FlightScheduleEntity> existingFlightSchedules) throws UpdateFlightSchedulePlanFailedException {
        Iterator<FlightScheduleEntity> iter = existingFlightSchedules.iterator();

        while (iter.hasNext()) {
            FlightScheduleEntity existingFlightScheduleEntity = iter.next();

            if (!updatedFlightSchedules.contains(existingFlightScheduleEntity)) {
                Query query = em.createQuery("SELECT f from FlightReservationEntity f, IN (f.flightSchedules) fs WHERE fs.flightSchedulePlan =:inFlightSchedulePlan");
                query.setParameter("inFlightSchedulePlan", existingFlightScheduleEntity);

                if (!query.getResultList().isEmpty()) { // flight schedule to be deleted is in use
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule to be deleted has been reserved!");
                }

                iter.remove(); // remove flight schedule to be deleted
                em.remove(existingFlightScheduleEntity); // remove flight schedule record
            }
        }
    }

    @Override
    public List<FlightSchedulePlanEntity> retrieveAllFlightSchedulePlans() {
        String jpql = "SELECT DISTINCT f from FlightSchedulePlanEntity f, in (f.flightSchedules) t"
                + " WHERE f.isReturnFlightSchedulePlan = FALSE AND f.isDisabled = FALSE"
                + " ORDER BY f.flight.flightNumber ASC, t.departureDate DESC";
        Query query = em.createQuery(jpql);

        List<FlightSchedulePlanEntity> flightSchedulePlans = (List<FlightSchedulePlanEntity>) query.getResultList();
        return flightSchedulePlans;
    }

    @Override
    public FlightSchedulePlanEntity retrieveFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException {
        FlightSchedulePlanEntity flightSchedulePlanEntity = em.find(FlightSchedulePlanEntity.class, flightSchedulePlanId);

        if (flightSchedulePlanEntity == null) {
            throw new FlightSchedulePlanNotFoundException("FlightSchedulePlanNotFoundException: Flight schedule plan with id " + flightSchedulePlanId + " does not exist!");
        }

        if (flightSchedulePlanEntity.getIsDisabled()) {
            throw new FlightSchedulePlanNotFoundException("FlightSchedulePlanNotFoundException: Flight schedule plan with id " + flightSchedulePlanId + " is disabled!");
        }

        return flightSchedulePlanEntity;
    }

    @Override
    public void deleteFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException, FlightSchedulePlanInUseException {
        FlightSchedulePlanEntity flightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

        if (flightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
            deleteReturnFlightSchedulePlan(flightSchedulePlanEntity);
            return;
        }

        for (FlightScheduleEntity flightScheduleEntity : flightSchedulePlanEntity.getFlightSchedules()) {
            Query query = em.createQuery("SELECT f from FlightReservationEntity f IN (f.flightSchedules) fl WHERE fl.flightScheduleId =:inFlightScheduleId");
            query.setParameter("inFlightScheduleId", flightScheduleEntity.getFlightScheduleId());

            if (!query.getResultList().isEmpty()) {
                flightSchedulePlanEntity.setIsDisabled(true); // set main to disabled
                if (flightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) { // set return to disabled
                    flightSchedulePlanEntity.getReturnFlightSchedulePlan().setIsDisabled(true);
                }
                em.flush();
                throw new FlightSchedulePlanInUseException("FlightSchedulePlanInUseException: Flight schedule plan has flight schedules that are being reserved!\nFlight schedule plan is marked as disabled!");
            }

        }

        // remove main flight schedule plan from associated flight
        flightSchedulePlanEntity.getFlight().getFlightSchedulePlans().remove(flightSchedulePlanEntity);

        if (flightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
            // remove return flight schedule plan from associated return flight
            flightSchedulePlanEntity.getReturnFlightSchedulePlan().getFlight().getFlightSchedulePlans().remove(flightSchedulePlanEntity.getReturnFlightSchedulePlan());
        }

        em.remove(flightSchedulePlanEntity);
    }

    private void deleteReturnFlightSchedulePlan(FlightSchedulePlanEntity returnFlightSchedulePlanEntity) throws FlightSchedulePlanInUseException {
        for (FlightScheduleEntity flightScheduleEntity : returnFlightSchedulePlanEntity.getFlightSchedules()) {
            Query query = em.createQuery("SELECT f from FlightReservationEntity f IN (f.flightSchedules) fl WHERE fl.flightScheduleId =:inFlightScheduleId");
            query.setParameter("inFlightScheduleId", flightScheduleEntity.getFlightScheduleId());

            if (!query.getResultList().isEmpty()) {
                returnFlightSchedulePlanEntity.setIsDisabled(true);
                em.flush();
                throw new FlightSchedulePlanInUseException("FlightSchedulePlanInUseException: Flight schedule plan has flight schedules that are being reserved!\nFlight schedule plan is marked as disabled!");
            }
        }

        Query query = em.createQuery("SELECT f from FlightSchedulePlanEntity f WHERE f.returnFlightSchedulePlan =:inReturnFlightSchedulePlan");
        query.setParameter("inReturnFlightSchedulePlan", returnFlightSchedulePlanEntity);

        FlightSchedulePlanEntity mainFlightSchedulePlanEntity = (FlightSchedulePlanEntity) query.getSingleResult();
        mainFlightSchedulePlanEntity.setReturnFlightSchedulePlan(null); // diassociate main and return
        // remove flight schedule plan from associated flight
        returnFlightSchedulePlanEntity.getFlight().getFlightSchedulePlans().remove(returnFlightSchedulePlanEntity);

        em.remove(returnFlightSchedulePlanEntity); // remove return
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
