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
import java.util.HashMap;
import java.util.HashSet;
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
import util.enumeration.CabinClassEnum;
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

        // cannot create return flight schedule plan if flight has no return flight
        if (doCreateReturnFlightSchedule && flightEntity.getReturnFlight() == null) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight does not has complementary flight!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();

        // bi directional schedule plan and flight assoication
        newFlightSchedulePlanEntity.setFlight(flightEntity);
        flightEntity.getFlightSchedulePlans().add(newFlightSchedulePlanEntity);

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedules(newFlightSchedulePlanEntity, flightSchedules);
            fareEntitySessionBeanLocal.createNewFares(fares, newFlightSchedulePlanEntity);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        validateFlightSchedulePlan(newFlightSchedulePlanEntity);

        if (doCreateReturnFlightSchedule) {
            newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(createReturnFlightSchedulePlan(newFlightSchedulePlanEntity));
        }

        em.persist(newFlightSchedulePlanEntity);
        em.flush();

        return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

    @Override
    public Long createRecurrentFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {

        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        if (flightEntity.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight is disabled!");
        }

        // cannot create return flight schedule plan if flight has no return flight
        if (doCreateReturnFlightSchedule && flightEntity.getReturnFlight() == null) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight does not has complementary flight!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();
        newFlightSchedulePlanEntity.setRecurrentEndDate(endDate);
        newFlightSchedulePlanEntity.setRecurrentFrequency(recurrentDaysFrequency);

        newFlightSchedulePlanEntity.setFlight(flightEntity); //associate flight schedule with flight

        List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules
        autoGenerateFlightSchedules.add(baseFlightSchedule); // add base flight schedule

        Date startDate = baseFlightSchedule.getDepartureDate(); // base start date

        // while start date is smaller to end date
        while (startDate.compareTo(endDate) < 0) {
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
            FlightSchedulePlanEntity returnFlightSchedulePlan = createReturnFlightSchedulePlan(newFlightSchedulePlanEntity);
            returnFlightSchedulePlan.setRecurrentEndDate(endDate);
            returnFlightSchedulePlan.setRecurrentFrequency(recurrentDaysFrequency);

            newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(returnFlightSchedulePlan);
        }

        em.persist(newFlightSchedulePlanEntity);
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
            FlightScheduleEntity returnFlightSchedule = createReturnFlightSchedule(flightSchedule);
            returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlanEntity); // associate return flight schedule with flight schedule plan
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

    // helper method to create retrun flight schedule
    private FlightScheduleEntity createReturnFlightSchedule(FlightScheduleEntity flightSchedule) {
        Date arrivalDateTime = flightSchedule.getArrivalDateTime(); // arrival time is already calculated based on time zone of destination airport
        GregorianCalendar returnDepartureDateTimeCalender = new GregorianCalendar();
        returnDepartureDateTimeCalender.setTime(arrivalDateTime);
        returnDepartureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, 8); // lay over of 8 hours

        Date returnDepartureDateTime = returnDepartureDateTimeCalender.getTime();

        FlightScheduleEntity returnFlightSchedule = new FlightScheduleEntity(returnDepartureDateTime, flightSchedule.getEstimatedFlightDuration());

        flightSchedule.setReturnFlightSchedule(returnFlightSchedule); // associate flight schedule and its return flight schedule

        return returnFlightSchedule;
    }

    @Override
    public List<FlightSchedulePlanEntity> retrieveAllFlightSchedulePlans() {
        Query query = em.createQuery("SELECT f from FlightSchedulePlanEntity f WHERE f.isReturnFlightSchedulePlan = FALSE AND f.isDisabled = FALSE ORDER BY f.flight.flightNumber ASC");

        List<FlightSchedulePlanEntity> flightSchedulePlans = (List<FlightSchedulePlanEntity>) query.getResultList();

        // descending order by first departure date time
        flightSchedulePlans.sort((FlightSchedulePlanEntity a, FlightSchedulePlanEntity b) -> {
            if (!a.getFlightSchedules().isEmpty() && !b.getFlightSchedules().isEmpty()) {
                return b.getFlightSchedules().get(0).getDepartureDate().compareTo(a.getFlightSchedules().get(0).getDepartureDate());
            }
            return 0;
        });

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
    public Long updateRemoveFlightScheduleFromFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException {
        try {
            if (flightScheduleIdsToRemove.isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Please provide at least one flight schedule ID to remove!");
            }

            // retrieve managed instance of flight schedule plan
            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            // cannot update a disabled flight schedule plan
            if (existingFlightSchedulePlanEntity.getIsDisabled()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
            }

            // check that flight schedules to delete exsit in flight schedule plan
            for (Long id : flightScheduleIdsToRemove) {
                boolean validId = false;
                for (FlightScheduleEntity flightSchedule : existingFlightSchedulePlanEntity.getFlightSchedules()) {
                    if (flightSchedule.getFlightScheduleId().equals(id)) {
                        validId = true;
                    }
                }
                if (!validId) {
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: One or more  flight schedules to remove does not belong to selected flight schedule plan!");
                }
            }

            // removing flight schedules from a return flight schedule plan
            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                return updateRemoveFlightScheduleFromReturnFlightSchedulePlan(existingFlightSchedulePlanEntity, flightScheduleIdsToRemove);
            }

            List<FlightScheduleEntity> existingFlightSchedules = existingFlightSchedulePlanEntity.getFlightSchedules(); // retrieve existing flight schedules

            Iterator<FlightScheduleEntity> iter = existingFlightSchedules.iterator();

            while (iter.hasNext()) {
                FlightScheduleEntity existingFlightScheduleEntity = iter.next();

                if (flightScheduleIdsToRemove.contains(existingFlightScheduleEntity.getFlightScheduleId())) {
                    checkFlightScheduleUsage(existingFlightScheduleEntity);

                    // remove complementary return flight schedule if any
                    if (existingFlightScheduleEntity.getReturnFlightSchedule() != null) {
                        FlightScheduleEntity returnFlightSchedule = existingFlightScheduleEntity.getReturnFlightSchedule();
                        // remove return flight schedule from return flight schedule plan
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().getFlightSchedules().remove(returnFlightSchedule);
                    }

                    iter.remove(); // remove flight schedule from flight schedule plan
                    em.remove(existingFlightScheduleEntity); // remove flight schedule record
                }
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();

        } catch (FlightSchedulePlanNotFoundException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    private Long updateRemoveFlightScheduleFromReturnFlightSchedulePlan(FlightSchedulePlanEntity returnFlightSchedulePlan, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException {
        // get parent flight schedule plan
        Query query = em.createQuery("SELECT f FROM FlightSchedulePlanEntity f WHERE f.returnFlightSchedulePlan =:inReturnFlightSchedulePlan");
        query.setParameter("inReturnFlightSchedulePlan", returnFlightSchedulePlan);
        FlightSchedulePlanEntity parentFlightSchedulePlan = (FlightSchedulePlanEntity) query.getSingleResult();

        List<FlightScheduleEntity> returnFlightSchedules = returnFlightSchedulePlan.getFlightSchedules(); // retrieve existing flight schedules
        Iterator<FlightScheduleEntity> iter = returnFlightSchedules.iterator();

        while (iter.hasNext()) {
            FlightScheduleEntity returnFlightSchedule = iter.next();

            if (flightScheduleIdsToRemove.contains(returnFlightSchedule.getFlightScheduleId())) {
                checkFlightScheduleUsage(returnFlightSchedule);

                // remove return flight schedule from parent flight schedule 
                for (FlightScheduleEntity parentFlightSchedule : parentFlightSchedulePlan.getFlightSchedules()) {
                    if (parentFlightSchedule.getReturnFlightSchedule().equals(returnFlightSchedule)) {
                        parentFlightSchedule.setReturnFlightSchedule(null);
                        break;
                    }
                }

                iter.remove(); // remove flight schedule from return flight schedule plan
                em.remove(returnFlightSchedule); // remove flight schedule record
            }
        }

        em.flush();

        return returnFlightSchedulePlan.getFlightSchedulePlanId();
    }

    @Override
    public Long updateAddFlightScheduleToFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> newFlightSchedules, Boolean doCreateReturnFlightSchedule) throws UpdateFlightSchedulePlanFailedException {
        try {

            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (newFlightSchedules.isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Please provide at least one new flight schedule!");
            }

            if (existingFlightSchedulePlanEntity.getIsDisabled()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
            }

            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot add new flight schedules to a return flight schedule plan!");
            }

            if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() == null && doCreateReturnFlightSchedule) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan has no complementary return flight schedule plan for adding of return flight schedules!");
            }

            // add new flight schedules to flight schedule plan
            flightScheduleSessionBeanLocal.createNewFlightSchedules(existingFlightSchedulePlanEntity, newFlightSchedules);

            if (doCreateReturnFlightSchedule) {
                FlightSchedulePlanEntity returnFlightSchedulePlan = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan();
                List<FlightScheduleEntity> returnFlightSchedules = new ArrayList<>();

                for (FlightScheduleEntity newFlightScheduleEntity : newFlightSchedules) {
                    FlightScheduleEntity returnFlightSchedule = this.createReturnFlightSchedule(newFlightScheduleEntity);
                    returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlan); // associate return flight schedule with flight schedule plan
                    returnFlightSchedules.add(returnFlightSchedule);
                }

                flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlan, returnFlightSchedules);
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (CreateNewFlightScheduleException | FlightSchedulePlanNotFoundException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    // for arguments not in use, pass null 
    @Override
    public Long updateRecurrentFlightSchedulePlanParameters(Long flightSchedulePlanId, Date newEndDate, Integer newRecurrentFrequency) throws UpdateFlightSchedulePlanFailedException {
        try {
            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (existingFlightSchedulePlanEntity.getIsDisabled()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
            }

            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update parameters for a return recurrent flight schedule plan!");
            }

            if (existingFlightSchedulePlanEntity.getRecurrentEndDate() == null || existingFlightSchedulePlanEntity.getRecurrentFrequency() == null) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update parameters for a non-recurrent flight schedule plan!");
            }

            // same recurrent frequency, update end date
            if (newRecurrentFrequency == null) {
                if (newEndDate == null || newEndDate.equals(existingFlightSchedulePlanEntity.getRecurrentEndDate())) {
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Invalid new recurrent parameters!");
                }

                if (newEndDate.compareTo(existingFlightSchedulePlanEntity.getRecurrentEndDate()) < 0) {
                    // new end date is earlier than previous end date
                    // remove flight schedules with dates later than new end date
                    HashSet<Long> flightScheduleIds = new HashSet<>();

                    for (FlightScheduleEntity flightSchedule : existingFlightSchedulePlanEntity.getFlightSchedules()) {
                        if (flightSchedule.getDepartureDate().compareTo(newEndDate) > 0) {
                            flightScheduleIds.add(flightSchedule.getFlightScheduleId());
                        }
                    }

                    this.updateRemoveFlightScheduleFromFlightSchedulePlan(flightSchedulePlanId, flightScheduleIds);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();

                } else {
                    // new end date is later than previous end date
                    // create new flight schedules and add to plan

                    // get last departure date
                    Date startDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(existingFlightSchedulePlanEntity.getFlightSchedules().size() - 1).getDepartureDate();
                    Integer flightDuration = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDuration();

                    List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules

                    // while start date is smaller than new end date
                    while (startDate.compareTo(newEndDate) < 0) {
                        GregorianCalendar autoCalender = new GregorianCalendar();
                        autoCalender.setTime(startDate);
                        autoCalender.add(GregorianCalendar.HOUR_OF_DAY, existingFlightSchedulePlanEntity.getRecurrentFrequency() * 24);
                        startDate = autoCalender.getTime();

                        // generate new flight schedule depending on the date
                        FlightScheduleEntity flightSchedule = new FlightScheduleEntity(startDate, flightDuration);
                        autoGenerateFlightSchedules.add(flightSchedule);
                    }

                    this.updateAddFlightScheduleToFlightSchedulePlan(flightSchedulePlanId, autoGenerateFlightSchedules, existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
                }
            } else { // different frequency, can be same end date or different end date

                checkFlightSchedulePlanUsage(existingFlightSchedulePlanEntity);

                // get base details
                Date startDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getDepartureDate();
                Integer flightDuration = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDuration();

                existingFlightSchedulePlanEntity.getFlightSchedules().clear(); // remove all flight schedules and associated return flight schedules

                FlightSchedulePlanEntity returnFlightSchedulePlan = null;
                if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                    returnFlightSchedulePlan = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan();
                    // remove all associated return flight schedules from return flight schedule plan
                    returnFlightSchedulePlan.getFlightSchedules().clear();
                }

                FlightScheduleEntity basFlightSchedule = new FlightScheduleEntity(startDate, flightDuration);

                List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules
                autoGenerateFlightSchedules.add(basFlightSchedule); // add base flight schedule

                // if the end date is null, use existing end date, else use new end date
                if (newEndDate == null) {
                    newEndDate = existingFlightSchedulePlanEntity.getRecurrentEndDate();
                }

                // while start date is smaller to end date
                while (startDate.compareTo(newEndDate) < 0) {
                    GregorianCalendar autoCalender = new GregorianCalendar();
                    autoCalender.setTime(startDate);
                    autoCalender.add(GregorianCalendar.HOUR_OF_DAY, newRecurrentFrequency * 24);
                    startDate = autoCalender.getTime();
                    // generate new flight schedule depending on the date
                    FlightScheduleEntity flightSchedule = new FlightScheduleEntity(startDate, flightDuration);
                    autoGenerateFlightSchedules.add(flightSchedule);
                }

                flightScheduleSessionBeanLocal.createNewFlightSchedules(existingFlightSchedulePlanEntity, autoGenerateFlightSchedules);

                // if return flight schedule plan exists, generate the return flight schedules
                if (returnFlightSchedulePlan != null) {
                    List<FlightScheduleEntity> autoGenerateReturnFlightSchedules = new ArrayList<>(); // store generated flight schedules
                    for (FlightScheduleEntity flightSchedule : autoGenerateFlightSchedules) {
                        FlightScheduleEntity returnFlightSchedule = createReturnFlightSchedule(flightSchedule);
                        returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlan); // associate return flight schedule with flight schedule plan
                        autoGenerateReturnFlightSchedules.add(returnFlightSchedule);
                    }

                    flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlan, autoGenerateReturnFlightSchedules);
                }

                em.flush();

                return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
            }
        } catch (CreateNewFlightScheduleException | FlightSchedulePlanNotFoundException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    private void checkFlightSchedulePlanUsage(FlightSchedulePlanEntity existingFlightSchedulePlanEntity) throws UpdateFlightSchedulePlanFailedException {
        List<FlightScheduleEntity> flightSchedules = existingFlightSchedulePlanEntity.getFlightSchedules();
        for (FlightScheduleEntity flightSchedule : flightSchedules) {
            checkFlightScheduleUsage(flightSchedule);
        }

        if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
            List<FlightScheduleEntity> returnFlightSchedules = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().getFlightSchedules();
            for (FlightScheduleEntity flightSchedule : returnFlightSchedules) {
                checkFlightScheduleUsage(flightSchedule);
            }
        }
    }

    private void checkFlightScheduleUsage(FlightScheduleEntity flightScheduleEntity) throws UpdateFlightSchedulePlanFailedException {
        Query query = em.createQuery("SELECT f FROM FlightReservationEntity f, IN (f.flightSchedules) fs WHERE fs.flightScheduleId =:inFlightSCheduleId");
        query.setParameter("inFlightSCheduleId", flightScheduleEntity.getFlightScheduleId());

        if (!query.getResultList().isEmpty()) { // flight schedule to be deleted is in use
            throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule is in use!");
        }
    }

    // update departure date and flight duration
    @Override
    public Long updateFlightScheduleDetailForNonRecurrentFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> updatedFlightSchedules) throws UpdateFlightSchedulePlanFailedException {
        try {
            if (updatedFlightSchedules.isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Please provide at least one updated flight schedule!");
            }

            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (existingFlightSchedulePlanEntity.getRecurrentEndDate() != null || existingFlightSchedulePlanEntity.getRecurrentFrequency() != null) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Selected flight schedule plan is a recurrent flight schedule plan!");
            }

            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update flight schedule details of a return flight schedule plan!");
            }

            List<FlightScheduleEntity> existingFlightSchedules = existingFlightSchedulePlanEntity.getFlightSchedules();

            for (FlightScheduleEntity updatedFlightSchedule : updatedFlightSchedules) {

                for (FlightScheduleEntity existingFlightSchedule : existingFlightSchedules) {
                    if (existingFlightSchedule.equals(updatedFlightSchedule)) {
                        // check that the flight schedule to be updated is not in use
                        checkFlightScheduleUsage(existingFlightSchedule);

                        // update departure date and estimated flight duration
                        existingFlightSchedule.setDepartureDate(updatedFlightSchedule.getDepartureDate());
                        existingFlightSchedule.setEstimatedFlightDuration(updatedFlightSchedule.getEstimatedFlightDuration());

                        // check conflict for updated flight schedule and flight
                        flightScheduleSessionBeanLocal.checkFlightSchedules(existingFlightSchedule, existingFlightSchedulePlanEntity.getFlight());

                        if (existingFlightSchedule.getReturnFlightSchedule() != null) {
                            // create new return flight schedule using updated flight schedule
                            FlightScheduleEntity newReturnFlightSchedule = this.createReturnFlightSchedule(existingFlightSchedule);

                            // retrieve existing return flight schedule
                            FlightScheduleEntity existingReturnFlightSchedule = existingFlightSchedule.getReturnFlightSchedule();

                            // update flight schedule details of complementary return flight
                            existingReturnFlightSchedule.setDepartureDate(newReturnFlightSchedule.getDepartureDate());
                            existingReturnFlightSchedule.setEstimatedFlightDuration(newReturnFlightSchedule.getEstimatedFlightDuration());

                            // check conflict for updated return schedule and return flight
                            flightScheduleSessionBeanLocal.checkFlightSchedules(existingReturnFlightSchedule, existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().getFlight());
                        }
                    }
                }

            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (FlightSchedulePlanNotFoundException | CreateNewFlightScheduleException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    @Override
    public Long updateFareAmountInFlightSchedulePlan(Long flightSchedulePlanId, List<FareEntity> updateFares) throws UpdateFlightSchedulePlanFailedException {
        try {
            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (updateFares.isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Please provide at least one updated fare!");
            }

            for (FareEntity updatedFare : updateFares) {
                for (FareEntity existingFare : existingFlightSchedulePlanEntity.getFares()) {
                    // fare amount in updated fare is different
                    if (existingFare.equals(updatedFare) && !existingFare.getFareAmount().equals(updatedFare.getFareAmount())) {
                        existingFare.setFareAmount(updatedFare.getFareAmount()); // update new fare amount
                    }
                }
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (FlightSchedulePlanNotFoundException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    @Override
    public Long updateAddFareToFlightSchedulePlan(Long flightSchedulePlanId, List<FareEntity> newFares) throws UpdateFlightSchedulePlanFailedException {
        try {
            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (newFares.isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Please provide at least one new fare!");
            }

            fareEntitySessionBeanLocal.createNewFares(newFares, existingFlightSchedulePlanEntity);

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (FlightSchedulePlanNotFoundException | CreateNewFareException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    @Override
    public Long updateRemoveFareFromFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> fareIds) throws UpdateFlightSchedulePlanFailedException {
        try {
            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (fareIds.isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Please provide at least fare id for removal!");
            }

            HashMap<CabinClassEnum, Integer> fareCheck = new HashMap<>();
            fareCheck.put(CabinClassEnum.Y, 0);
            fareCheck.put(CabinClassEnum.F, 0);
            fareCheck.put(CabinClassEnum.J, 0);
            fareCheck.put(CabinClassEnum.W, 0);

            // count number of fare for each avail cabin class
            existingFlightSchedulePlanEntity
                    .getFares()
                    .forEach(x
                            -> fareCheck.put(x.getCabinClass(), (fareCheck.get(x.getCabinClass()) + 1))
                    );

            Iterator<FareEntity> iterator = existingFlightSchedulePlanEntity.getFares().iterator();

            while (iterator.hasNext()) {
                FareEntity fareEntity = iterator.next();

                if (fareIds.contains(fareEntity.getFareId())) {
                    // decre the number of fare for the avail cabin class
                    fareCheck.put(fareEntity.getCabinClass(), (fareCheck.get(fareEntity.getCabinClass()) - 1));

                    // if cabin class has no more fare after removal
                    if (fareCheck.get(fareEntity.getCabinClass()) <= 0) {
                        throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Each cabin class must have at least one fare!");
                    }

                    iterator.remove();
                    em.remove(fareEntity);
                }

            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (FlightSchedulePlanNotFoundException ex) {
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    @Override
    public void deleteFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException, FlightSchedulePlanInUseException {
        FlightSchedulePlanEntity flightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

        if (flightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
            deleteReturnFlightSchedulePlan(flightSchedulePlanEntity);
            return;
        }

        for (FlightScheduleEntity flightScheduleEntity : flightSchedulePlanEntity.getFlightSchedules()) {
            Query query = em.createQuery("SELECT f from FlightReservationEntity f, IN (f.flightSchedules) fl WHERE fl.flightScheduleId =:inFlightScheduleId");
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
            Query query = em.createQuery("SELECT f from FlightReservationEntity f, IN (f.flightSchedules) fl WHERE fl.flightScheduleId =:inFlightScheduleId");
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
