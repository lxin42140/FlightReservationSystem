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
import java.util.Calendar;
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
import util.enumeration.FlightSchedulePlanTypeEnum;
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
    public Long createNewNonRecurrentFlightSchedulePlan(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {

        if (doCreateReturnFlightSchedule && (layoverPeriodForReturnFlight == null || layoverPeriodForReturnFlight <= 0)) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Invalid layover duration for return flight schedules!");
        }

        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        if (flightEntity.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight is disabled!");
        }

        // cannot create return flight schedule plan if flight has no return flight
        if (doCreateReturnFlightSchedule && flightEntity.getReturnFlight() == null) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight does not has complementary flight!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();
        newFlightSchedulePlanEntity.setFlightSchedulePlanType(FlightSchedulePlanTypeEnum.MANUAL);

        // bi directional schedule plan and flight assoication
        newFlightSchedulePlanEntity.setFlight(flightEntity);
        flightEntity.getFlightSchedulePlans().add(newFlightSchedulePlanEntity);

        // save layover period in flight schedule plan for future update of flight schedule plan
        newFlightSchedulePlanEntity.setLayoverPeriod(layoverPeriodForReturnFlight);

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedules(newFlightSchedulePlanEntity, flightSchedules);
            fareEntitySessionBeanLocal.createNewFares(fares, newFlightSchedulePlanEntity);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        validateFlightSchedulePlan(newFlightSchedulePlanEntity);

        if (doCreateReturnFlightSchedule) {
            FlightSchedulePlanEntity returnFlightSchedulePlan = createReturnFlightSchedulePlan(newFlightSchedulePlanEntity, layoverPeriodForReturnFlight);

            newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(returnFlightSchedulePlan);
        }

        em.persist(newFlightSchedulePlanEntity);
        em.flush();

        return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

    @Override
    public Long createRecurrentNDaysFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        if (doCreateReturnFlightSchedule && (layoverPeriodForReturnFlight == null || layoverPeriodForReturnFlight <= 0)) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Invalid layover duration for return flight schedules!");
        }

        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        if (flightEntity.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight is disabled!");
        }

        // cannot create return flight schedule plan if flight has no return flight
        if (doCreateReturnFlightSchedule && flightEntity.getReturnFlight() == null) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight does not has complementary flight!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();
        newFlightSchedulePlanEntity.setRecurrentEndDate(endDate); // set recurrent end date
        newFlightSchedulePlanEntity.setRecurrentFrequency(recurrentDaysFrequency); // set recurrent frequency
        newFlightSchedulePlanEntity.setFlightSchedulePlanType(FlightSchedulePlanTypeEnum.RECURRENTNDAYS);
        newFlightSchedulePlanEntity.setLayoverPeriod(layoverPeriodForReturnFlight);

        //associate flight schedule with flight
        newFlightSchedulePlanEntity.setFlight(flightEntity);
        flightEntity.getFlightSchedulePlans().add(newFlightSchedulePlanEntity);

        List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules
        autoGenerateFlightSchedules.add(baseFlightSchedule); // add base flight schedule

        Date startDate = baseFlightSchedule.getDepartureDate(); // base start date

        // while start date is smaller to end date
        while (startDate.compareTo(endDate) < 0) {
            GregorianCalendar autoCalender = new GregorianCalendar();
            autoCalender.setTime(startDate);
            autoCalender.add(GregorianCalendar.HOUR_OF_DAY, recurrentDaysFrequency * 24);
            startDate = autoCalender.getTime();

            if (startDate.after(endDate)) {
                break;
            }

            // generate new flight schedule depending on the date
            autoGenerateFlightSchedules.add(new FlightScheduleEntity(startDate, baseFlightSchedule.getEstimatedFlightDurationHour(), baseFlightSchedule.getEstimatedFlightDurationMinute()));
        }

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedules(newFlightSchedulePlanEntity, autoGenerateFlightSchedules);
            fareEntitySessionBeanLocal.createNewFares(fares, newFlightSchedulePlanEntity);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        if (doCreateReturnFlightSchedule) {
            FlightSchedulePlanEntity returnFlightSchedulePlan = createReturnFlightSchedulePlan(newFlightSchedulePlanEntity, layoverPeriodForReturnFlight);
            returnFlightSchedulePlan.setRecurrentEndDate(endDate); // set recurrent end date
            returnFlightSchedulePlan.setRecurrentFrequency(recurrentDaysFrequency); // set recurrent frequency

            newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(returnFlightSchedulePlan);
        }

        em.persist(newFlightSchedulePlanEntity);
        em.flush();

        return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

//    dayOfWeek: 1 - 7 for Sunday to Saturday
//    departureHourOfDay: 0 - 23
    @Override
    public Long createRecurrentWeeklyFlightSchedulePlan(Integer dayOfWeek, Integer departureHourOfDay, Integer departureMinuteOfHour, Date recurrentStartDate, Date recurrentEndDate, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException, FlightNotFoundException {
        if (doCreateReturnFlightSchedule && (layoverPeriodForReturnFlight == null || layoverPeriodForReturnFlight <= 0)) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Invalid layover duration for return flight schedules!");
        }

        FlightEntity flightEntity = flightSessionBeanLocal.retrieveFlightByFlightNumber(flightNumber);

        if (flightEntity.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight is disabled!");
        }

        // cannot create return flight schedule plan if flight has no return flight
        if (doCreateReturnFlightSchedule && flightEntity.getReturnFlight() == null) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Selected flight does not has complementary flight!");
        }

        FlightSchedulePlanEntity newFlightSchedulePlanEntity = new FlightSchedulePlanEntity();
        newFlightSchedulePlanEntity.setRecurrentStartDate(recurrentStartDate); // start recurrent start date
        newFlightSchedulePlanEntity.setRecurrentEndDate(recurrentEndDate); // set recurrent end date
        newFlightSchedulePlanEntity.setFlightSchedulePlanType(FlightSchedulePlanTypeEnum.RECURRENTWEEKLY); // set recurrent weekly
        newFlightSchedulePlanEntity.setLayoverPeriod(layoverPeriodForReturnFlight);

        //associate flight schedule with flight
        newFlightSchedulePlanEntity.setFlight(flightEntity);
        flightEntity.getFlightSchedulePlans().add(newFlightSchedulePlanEntity);

        List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules

        Date startDate = recurrentStartDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // while start date does not match the given day of the week
        while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            GregorianCalendar autoCalender = new GregorianCalendar();
            autoCalender.setTime(startDate);
            autoCalender.add(GregorianCalendar.HOUR_OF_DAY, 24); // go to next day
            startDate = autoCalender.getTime(); // get the next day

            calendar.setTime(startDate); // set day in calender to check day of week
        }

        // set departure hour and minute
        calendar.set(Calendar.HOUR_OF_DAY, departureHourOfDay);
        calendar.set(Calendar.MINUTE, departureMinuteOfHour);

        startDate = calendar.getTime();
        baseFlightSchedule.setDepartureDate(startDate); // set departure date for base flight
        autoGenerateFlightSchedules.add(baseFlightSchedule); // add base flight schedule

        while (startDate.compareTo(recurrentEndDate) < 0) {
            GregorianCalendar autoCalender = new GregorianCalendar();
            autoCalender.setTime(startDate);
            autoCalender.add(GregorianCalendar.HOUR_OF_DAY, 7 * 24); // get the next week
            startDate = autoCalender.getTime();

            if (startDate.after(recurrentEndDate)) {
                break;
            }
            // generate new flight schedule depending on the date
            autoGenerateFlightSchedules.add(new FlightScheduleEntity(startDate, baseFlightSchedule.getEstimatedFlightDurationHour(), baseFlightSchedule.getEstimatedFlightDurationMinute()));
        }

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedules(newFlightSchedulePlanEntity, autoGenerateFlightSchedules);
            fareEntitySessionBeanLocal.createNewFares(fares, newFlightSchedulePlanEntity);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        if (doCreateReturnFlightSchedule) {
            FlightSchedulePlanEntity returnFlightSchedulePlan = createReturnFlightSchedulePlan(newFlightSchedulePlanEntity, layoverPeriodForReturnFlight);
            returnFlightSchedulePlan.setRecurrentStartDate(recurrentStartDate); // start recurrent start date
            returnFlightSchedulePlan.setRecurrentEndDate(recurrentEndDate); // set recurrent end date

            newFlightSchedulePlanEntity.setReturnFlightSchedulePlan(returnFlightSchedulePlan);
        }

        em.persist(newFlightSchedulePlanEntity);
        em.flush();

        return newFlightSchedulePlanEntity.getFlightSchedulePlanId();
    }

    // used to create return flight schedule plan
    // generate corresponding return flight schedules, and fare
    private FlightSchedulePlanEntity createReturnFlightSchedulePlan(FlightSchedulePlanEntity newFlightSchedulePlanEntity, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException {
        FlightEntity returnFlight = newFlightSchedulePlanEntity.getFlight().getReturnFlight(); // add return flight schedule plan for return flight

        if (returnFlight.getIsDisabled()) {
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Return flight is disabled!");
        }

        FlightSchedulePlanEntity returnFlightSchedulePlanEntity = new FlightSchedulePlanEntity(); // create return flight schedule plan
        returnFlightSchedulePlanEntity.setIsReturnFlightSchedulePlan(true); // set return flight schedule plan as true
        returnFlightSchedulePlanEntity.setFlightSchedulePlanType(newFlightSchedulePlanEntity.getFlightSchedulePlanType());
        returnFlightSchedulePlanEntity.setLayoverPeriod(layoverPeriodForReturnFlight); // set lay over

        //bi directional between return flight and return flight schedule plan
        returnFlightSchedulePlanEntity.setFlight(returnFlight);
        returnFlight.getFlightSchedulePlans().add(returnFlightSchedulePlanEntity);

        returnFlightSchedulePlanEntity.setLayoverPeriod(layoverPeriodForReturnFlight);

        List<FlightScheduleEntity> returnFlightSchedules = new ArrayList<>(); // store new flight schedules for return flight

        for (FlightScheduleEntity flightSchedule : newFlightSchedulePlanEntity.getFlightSchedules()) { // create return flight schedule for each flight schedule
            returnFlightSchedules.add(flightScheduleSessionBeanLocal.createReturnFlightSchedule(flightSchedule, layoverPeriodForReturnFlight));
        }

        List<FareEntity> returnFares = new ArrayList<>(); // store new instances of fare for return flight

        for (FareEntity fare : newFlightSchedulePlanEntity.getFares()) { // create new fare for return flight schedule plan
            returnFares.add(new FareEntity(fare.getFareBasisCode(), fare.getFareAmount(), fare.getCabinClass()));
        }

        try {
            flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlanEntity, returnFlightSchedules);
            fareEntitySessionBeanLocal.createNewFares(returnFares, returnFlightSchedulePlanEntity);
        } catch (CreateNewFareException | CreateNewFlightScheduleException ex) {
            throw new CreateNewFlightSchedulePlanException(ex.toString());
        }

        validateFlightSchedulePlan(returnFlightSchedulePlanEntity);

        return returnFlightSchedulePlanEntity;
    }

    @Override
    public List<FlightSchedulePlanEntity> retrieveAllFlightSchedulePlans() {
        Query query = em.createQuery("SELECT f from FlightSchedulePlanEntity f WHERE f.isReturnFlightSchedulePlan = FALSE AND f.isDisabled = FALSE");

        List<FlightSchedulePlanEntity> flightSchedulePlans = (List<FlightSchedulePlanEntity>) query.getResultList();

        // sort by flight number in ascending order
        flightSchedulePlans.sort((FlightSchedulePlanEntity a, FlightSchedulePlanEntity b) -> a.getFlight().getFlightNumber().compareTo(b.getFlight().getFlightNumber()));

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

    // update departure date and flight duration
    @Override
    public Long updateFlightScheduleDetailForManualFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> updatedFlightSchedules) throws UpdateFlightSchedulePlanFailedException {
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
                        if (!existingFlightSchedule.getFlightReservations().isEmpty()) {
                            throw new FlightSchedulePlanInUseException("FlightSchedulePlanInUseException: Flight schedule has already been reserved!");
                        }

                        // updated flight duration
                        List<Integer> updatedEstimatedFlightDuration = new ArrayList<>();
                        updatedEstimatedFlightDuration.add(updatedFlightSchedule.getEstimatedFlightDurationHour());
                        updatedEstimatedFlightDuration.add(updatedFlightSchedule.getEstimatedFlightDurationMinute());

                        // existing flight duration
                        List<Integer> existingEstimatedFlightDuration = new ArrayList<>();
                        existingEstimatedFlightDuration.add(existingFlightSchedule.getEstimatedFlightDurationHour());
                        existingEstimatedFlightDuration.add(existingFlightSchedule.getEstimatedFlightDurationMinute());

                        // both departure time and flight duration are updated
                        if (!existingFlightSchedule.getDepartureDate().equals(updatedFlightSchedule.getDepartureDate())
                                && !updatedEstimatedFlightDuration.equals(existingEstimatedFlightDuration)) {

                            // update departure date and estimated flight duration
                            existingFlightSchedule.setDepartureDate(updatedFlightSchedule.getDepartureDate());
                            existingFlightSchedule.setEstimatedFlightDurationHour(updatedFlightSchedule.getEstimatedFlightDurationHour());
                            existingFlightSchedule.setEstimatedFlightDurationMinute(updatedFlightSchedule.getEstimatedFlightDurationMinute());

                            // check conflict for updated flight schedule and flight
                            flightScheduleSessionBeanLocal.checkFlightSchedules(existingFlightSchedule, existingFlightSchedulePlanEntity.getFlight());

                            if (existingFlightSchedule.getReturnFlightSchedule() != null) {
                                Date arrivalDateTime = existingFlightSchedule.getArrivalDateTime();
                                GregorianCalendar returnDepartureDateTimeCalender = new GregorianCalendar();
                                returnDepartureDateTimeCalender.setTime(arrivalDateTime);
                                returnDepartureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, existingFlightSchedulePlanEntity.getLayoverPeriod());

                                Date returnDepartureDateTime = returnDepartureDateTimeCalender.getTime();

                                // update details of return flight schedule
                                existingFlightSchedule.getReturnFlightSchedule().setDepartureDate(returnDepartureDateTime);
                                existingFlightSchedule.getReturnFlightSchedule().setEstimatedFlightDurationHour(existingFlightSchedule.getEstimatedFlightDurationHour());
                                existingFlightSchedule.getReturnFlightSchedule().setEstimatedFlightDurationMinute(existingFlightSchedule.getEstimatedFlightDurationMinute());

                            }
                        } else if (!updatedEstimatedFlightDuration.equals(existingEstimatedFlightDuration)) {
                            // only flight duration is updated
                            existingFlightSchedule.setEstimatedFlightDurationHour(updatedFlightSchedule.getEstimatedFlightDurationHour());
                            existingFlightSchedule.setEstimatedFlightDurationMinute(updatedFlightSchedule.getEstimatedFlightDurationMinute());

                            // check conflict for updated flight schedule and flight
                            flightScheduleSessionBeanLocal.checkFlightSchedules(existingFlightSchedule, existingFlightSchedulePlanEntity.getFlight());

                            if (existingFlightSchedule.getReturnFlightSchedule() != null) {
                                existingFlightSchedule.getReturnFlightSchedule().setEstimatedFlightDurationHour(existingFlightSchedule.getEstimatedFlightDurationHour());
                                existingFlightSchedule.getReturnFlightSchedule().setEstimatedFlightDurationMinute(existingFlightSchedule.getEstimatedFlightDurationMinute());
                            }
                        } else if (!existingFlightSchedule.getDepartureDate().equals(updatedFlightSchedule.getDepartureDate())) {

                            // update flight departure date
                            existingFlightSchedule.setDepartureDate(updatedFlightSchedule.getDepartureDate());

                            // check conflict for updated flight schedule and flight
                            flightScheduleSessionBeanLocal.checkFlightSchedules(existingFlightSchedule, existingFlightSchedulePlanEntity.getFlight());

                            if (existingFlightSchedule.getReturnFlightSchedule() != null) {
                                Date arrivalDateTime = existingFlightSchedule.getArrivalDateTime();
                                GregorianCalendar returnDepartureDateTimeCalender = new GregorianCalendar();
                                returnDepartureDateTimeCalender.setTime(arrivalDateTime);
                                returnDepartureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, existingFlightSchedulePlanEntity.getLayoverPeriod());

                                Date returnDepartureDateTime = returnDepartureDateTimeCalender.getTime();

                                existingFlightSchedule.getReturnFlightSchedule().setDepartureDate(returnDepartureDateTime);
                            }
                        }
                        break;
                    }
                }
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (CreateNewFlightScheduleException | FlightSchedulePlanInUseException | FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    @Override
    public Long updateRemoveFlightScheduleFromManualFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException {
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

            // can only update manual flight schedule plan
            if (!existingFlightSchedulePlanEntity.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.MANUAL)) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot remove flight schedules from a recurrent flight schedule plan!");
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

                    if (!existingFlightScheduleEntity.getFlightReservations().isEmpty()) {
                        throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule has already been reserved!");
                    }

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
            em.getTransaction().rollback();
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

                if (!returnFlightSchedule.getFlightReservations().isEmpty()) {
                    em.getTransaction().rollback();
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule has already been reserved!");
                }

                // remove return flight schedule from parent flight schedule 
                for (FlightScheduleEntity parentFlightSchedule : parentFlightSchedulePlan.getFlightSchedules()) {

                    if (parentFlightSchedule.getReturnFlightSchedule() == null) {
                        continue;
                    }

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

    // can only add flight schedule to manual flight schedule plans
    @Override
    public Long updateAddFlightScheduleToManualFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> newFlightSchedules, Boolean doCreateReturnFlightSchedule) throws UpdateFlightSchedulePlanFailedException {
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

            // can only update manual flight schedule plan
            if (!existingFlightSchedulePlanEntity.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.MANUAL)) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot add flight schedules to a recurrent flight schedule plan!");
            }

            // add new flight schedules to flight schedule plan
            flightScheduleSessionBeanLocal.createNewFlightSchedules(existingFlightSchedulePlanEntity, newFlightSchedules);

            if (doCreateReturnFlightSchedule) {
                FlightSchedulePlanEntity returnFlightSchedulePlan = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan();

                List<FlightScheduleEntity> returnFlightSchedules = new ArrayList<>();

                for (FlightScheduleEntity newFlightScheduleEntity : newFlightSchedules) {
                    FlightScheduleEntity returnFlightSchedule = flightScheduleSessionBeanLocal.createReturnFlightSchedule(newFlightScheduleEntity, existingFlightSchedulePlanEntity.getLayoverPeriod());

                    returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlan); // associate return flight schedule with flight schedule plan
                    returnFlightSchedules.add(returnFlightSchedule);
                }
                flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlan, returnFlightSchedules);
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (CreateNewFlightScheduleException | FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    // for arguments not in use, pass null 
    @Override
    public Long updateRecurrentNDaysFlightSchedulePlanParameters(Long flightSchedulePlanId, Date newEndDate, Integer newRecurrentFrequency) throws UpdateFlightSchedulePlanFailedException {
        try {
            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (existingFlightSchedulePlanEntity.getIsDisabled()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
            }

            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update parameters for a return recurrent flight schedule plan!");
            }

            if (!existingFlightSchedulePlanEntity.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.RECURRENTNDAYS)) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update selected parameters for a non-recurrent N days flight schedule plan!");
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

                    existingFlightSchedulePlanEntity.setRecurrentEndDate(newEndDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentEndDate(newEndDate);
                    }

                    this.removeFlightScheduleFromRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, flightScheduleIds);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
                } else {
                    // new end date is later than previous end date
                    // create new flight schedules and add to plan

                    // get last departure date
                    Date startDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getDepartureDate();
                    for (FlightScheduleEntity flightSchedule : existingFlightSchedulePlanEntity.getFlightSchedules()) {
                        if (flightSchedule.getDepartureDate().compareTo(startDate) > 0) {
                            startDate = flightSchedule.getDepartureDate();
                        }
                    }

                    // retrieve the flight duration
                    Integer estimatedFlightHour = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationHour();
                    Integer estimatedFlightMin = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationMinute();

                    List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules

                    // while start date is smaller than new end date
                    while (startDate.compareTo(newEndDate) < 0) {
                        GregorianCalendar autoCalender = new GregorianCalendar();
                        autoCalender.setTime(startDate);
                        autoCalender.add(GregorianCalendar.HOUR_OF_DAY, existingFlightSchedulePlanEntity.getRecurrentFrequency() * 24);
                        startDate = autoCalender.getTime();

                        if (startDate.compareTo(newEndDate) > 0) {
                            break;
                        }

                        // generate new flight schedule depending on the date
                        FlightScheduleEntity flightSchedule = new FlightScheduleEntity(startDate, estimatedFlightHour, estimatedFlightMin);
                        autoGenerateFlightSchedules.add(flightSchedule);
                    }

                    existingFlightSchedulePlanEntity.setRecurrentEndDate(newEndDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentEndDate(newEndDate);
                    }

                    this.updateAddFlightScheduleToRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, autoGenerateFlightSchedules, existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
                }
            } else { // different frequency, can be same end date or different end date

                checkFlightSchedulePlanUsage(existingFlightSchedulePlanEntity);

                // get base details
                Date startDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getDepartureDate();
                for (FlightScheduleEntity flightSchedule : existingFlightSchedulePlanEntity.getFlightSchedules()) {
                    if (flightSchedule.getDepartureDate().compareTo(startDate) < 0) {
                        startDate = flightSchedule.getDepartureDate();
                    }
                }

                // retrieve the flight duration
                Integer estimatedFlightHour = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationHour();
                Integer estimatedFlightMin = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationMinute();

                // remove all flight schedules and associated return flight schedules
                existingFlightSchedulePlanEntity.getFlightSchedules().clear();

                FlightSchedulePlanEntity returnFlightSchedulePlan = null;
                if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                    returnFlightSchedulePlan = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan();
                    // remove all associated return flight schedules from return flight schedule plan
                    returnFlightSchedulePlan.getFlightSchedules().clear();
                }

                FlightScheduleEntity basFlightSchedule = new FlightScheduleEntity(startDate, estimatedFlightHour, estimatedFlightMin);

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
                    autoGenerateFlightSchedules.add(new FlightScheduleEntity(startDate, estimatedFlightHour, estimatedFlightMin));
                }

                flightScheduleSessionBeanLocal.createNewFlightSchedules(existingFlightSchedulePlanEntity, autoGenerateFlightSchedules);

                // if return flight schedule plan exists, generate the return flight schedules
                if (returnFlightSchedulePlan != null) {
                    List<FlightScheduleEntity> autoGenerateReturnFlightSchedules = new ArrayList<>(); // store generated flight schedules
                    for (FlightScheduleEntity flightSchedule : autoGenerateFlightSchedules) {
                        FlightScheduleEntity returnFlightSchedule = flightScheduleSessionBeanLocal.createReturnFlightSchedule(flightSchedule, existingFlightSchedulePlanEntity.getLayoverPeriod());
                        returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlan); // associate return flight schedule with flight schedule plan
                        autoGenerateReturnFlightSchedules.add(returnFlightSchedule);
                    }

                    flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlan, autoGenerateReturnFlightSchedules);
                }

                // update new frequency
                existingFlightSchedulePlanEntity.setRecurrentFrequency(newRecurrentFrequency);
                if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                    existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentFrequency(newRecurrentFrequency);
                }

                // update new date
                if (newEndDate != null && !newEndDate.equals(existingFlightSchedulePlanEntity.getRecurrentEndDate())) {
                    existingFlightSchedulePlanEntity.setRecurrentEndDate(newEndDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentEndDate(newEndDate);
                    }
                }

                em.flush();

                return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
            }
        } catch (FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        } catch (CreateNewFlightScheduleException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Unable to create new recurrent flight schedules! Please double check parameters!");
        }
    }

    @Override
    public Long updateRecurrentWeeklyFlightSchedulePlanDayOfWeek(Long flightSchedulePlanId, Integer newDayOfWeek) throws UpdateFlightSchedulePlanFailedException {
        try {
            if (newDayOfWeek == null || newDayOfWeek <= 0 || newDayOfWeek >= 8) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Invalid day of week!");
            }

            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (existingFlightSchedulePlanEntity.getIsDisabled()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
            }

            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot edit parameters to a return recurrent flight schedule plan!");
            }

            if (!existingFlightSchedulePlanEntity.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.RECURRENTWEEKLY)) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update selected parameters for a non-recurrent weekly flight schedule plan!");
            }

            checkFlightSchedulePlanUsage(existingFlightSchedulePlanEntity);

            List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules
            // get estimated flight duration
            Integer estimatedFlightHour = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationHour();
            Integer estimatedFlightMinute = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationMinute();

            // get departure hour and min
            Date departureDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getDepartureDate();
            Integer departureHourOfDay = departureDate.getHours();
            Integer departureMinuteOfHour = departureDate.getMinutes();

            // create new base flight schedule
            FlightScheduleEntity baseFlightSchedule = new FlightScheduleEntity();
            baseFlightSchedule.setEstimatedFlightDurationHour(estimatedFlightHour);
            baseFlightSchedule.setEstimatedFlightDurationMinute(estimatedFlightMinute);

            // get recurrent start date
            Date startDate = existingFlightSchedulePlanEntity.getRecurrentStartDate(); // get start date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            // while start date does not match the given day of the week
            while (calendar.get(Calendar.DAY_OF_WEEK) != newDayOfWeek) {
                GregorianCalendar autoCalender = new GregorianCalendar();
                autoCalender.setTime(startDate);
                autoCalender.add(GregorianCalendar.HOUR_OF_DAY, 24); // go to next day
                startDate = autoCalender.getTime(); // get the next day

                calendar.setTime(startDate); // set day in calender to check day of week
            }

            // set departure hour and minute
            calendar.set(Calendar.HOUR_OF_DAY, departureHourOfDay);
            calendar.set(Calendar.MINUTE, departureMinuteOfHour);

            startDate = calendar.getTime();
            baseFlightSchedule.setDepartureDate(startDate); // set departure date for base flight
            autoGenerateFlightSchedules.add(baseFlightSchedule); // add base flight schedule

            while (startDate.compareTo(existingFlightSchedulePlanEntity.getRecurrentEndDate()) < 0) {
                GregorianCalendar autoCalender = new GregorianCalendar();
                autoCalender.setTime(startDate);
                autoCalender.add(GregorianCalendar.HOUR_OF_DAY, 7 * 24); // get the next week
                startDate = autoCalender.getTime();
                // generate new flight schedule depending on the date
                autoGenerateFlightSchedules.add(new FlightScheduleEntity(startDate, baseFlightSchedule.getEstimatedFlightDurationHour(), baseFlightSchedule.getEstimatedFlightDurationMinute()));
            }

            // remove existing flight schedules
            existingFlightSchedulePlanEntity.getFlightSchedules().clear();
            flightScheduleSessionBeanLocal.createNewFlightSchedules(existingFlightSchedulePlanEntity, autoGenerateFlightSchedules);

            // create return flight schedules
            if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                FlightSchedulePlanEntity returnFlightSchedulePlan = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan();

                // remove existing flight schedule plan
                returnFlightSchedulePlan.getFlightSchedules().clear();

                List<FlightScheduleEntity> autoGenerateReturnFlightSchedules = new ArrayList<>(); // store generated flight schedules
                for (FlightScheduleEntity flightSchedule : autoGenerateFlightSchedules) {
                    FlightScheduleEntity returnFlightSchedule = flightScheduleSessionBeanLocal.createReturnFlightSchedule(flightSchedule, existingFlightSchedulePlanEntity.getLayoverPeriod());
                    returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlan); // associate return flight schedule with flight schedule plan
                    autoGenerateReturnFlightSchedules.add(returnFlightSchedule);
                }
                flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlan, autoGenerateReturnFlightSchedules);
            }

            em.persist(existingFlightSchedulePlanEntity);
            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();

        } catch (FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        } catch (CreateNewFlightScheduleException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Unable to create new recurrent flight schedules! Please double check parameters!");
        }
    }

    @Override
    public Long updateRecurrentWeeklyFlightSchedulePlanRange(Long flightSchedulePlanId, Date newStartDate, Date newEndDate) throws UpdateFlightSchedulePlanFailedException {
        try {
            if (newStartDate == null && newEndDate == null) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Invalid date range!");
            }

            FlightSchedulePlanEntity existingFlightSchedulePlanEntity = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);

            if (existingFlightSchedulePlanEntity.getIsDisabled()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule plan to be updated is disabled!");
            }

            if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot edit parameters to a return recurrent flight schedule plan!");
            }

            if (!existingFlightSchedulePlanEntity.getFlightSchedulePlanType().equals(FlightSchedulePlanTypeEnum.RECURRENTWEEKLY)) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update selected parameters for a non-recurrent weekly flight schedule plan!");
            }

            checkFlightSchedulePlanUsage(existingFlightSchedulePlanEntity);

            // update start date
            if (newStartDate != null && newEndDate == null) {
                // new start date is before current start date, add flight schedules
                if (newStartDate.before(existingFlightSchedulePlanEntity.getRecurrentStartDate())) {

                    // get earliest start date
                    Date startDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getDepartureDate();

                    // retrieve the flight duration
                    Integer estimatedFlightHour = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationHour();
                    Integer estimatedFlightMin = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationMinute();

                    List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules

                    // while existing start date is later than new start date
                    while (startDate.after(newStartDate)) {
                        GregorianCalendar autoCalender = new GregorianCalendar();
                        autoCalender.setTime(startDate);
                        autoCalender.add(GregorianCalendar.HOUR_OF_DAY, -7 * 24); // go previous week
                        startDate = autoCalender.getTime();

                        if (startDate.before(newStartDate)) {
                            break;
                        }

                        // generate new flight schedule depending on the date
                        FlightScheduleEntity flightSchedule = new FlightScheduleEntity(startDate, estimatedFlightHour, estimatedFlightMin);
                        autoGenerateFlightSchedules.add(flightSchedule);
                    }

                    existingFlightSchedulePlanEntity.setRecurrentStartDate(newStartDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentStartDate(newStartDate);
                    }

                    this.updateAddFlightScheduleToRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, autoGenerateFlightSchedules, existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();

                } else if (newStartDate.after(existingFlightSchedulePlanEntity.getRecurrentStartDate())) {
                    // new start date is after current start date, remvoe flight schedules

                    HashSet<Long> flightScheduleIds = new HashSet<>();

                    for (FlightScheduleEntity flightSchedule : existingFlightSchedulePlanEntity.getFlightSchedules()) {
                        if (flightSchedule.getDepartureDate().before(newStartDate)) {
                            flightScheduleIds.add(flightSchedule.getFlightScheduleId());
                        }
                    }

                    existingFlightSchedulePlanEntity.setRecurrentStartDate(newStartDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentStartDate(newStartDate);
                    }

                    this.removeFlightScheduleFromRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, flightScheduleIds);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
                } else {
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Same start date as existing start date!");

                }
            } // update end date
            else if (newEndDate != null && newStartDate == null) {
                // new end date is before current end date, remove flight schedules
                if (newEndDate.before(existingFlightSchedulePlanEntity.getRecurrentEndDate())) {

                    HashSet<Long> flightScheduleIds = new HashSet<>();

                    for (FlightScheduleEntity flightSchedule : existingFlightSchedulePlanEntity.getFlightSchedules()) {
                        if (flightSchedule.getDepartureDate().compareTo(newEndDate) > 0) {
                            flightScheduleIds.add(flightSchedule.getFlightScheduleId());
                        }
                    }

                    existingFlightSchedulePlanEntity.setRecurrentEndDate(newEndDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentEndDate(newEndDate);
                    }

                    this.removeFlightScheduleFromRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, flightScheduleIds);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
                } else if (newEndDate.after(existingFlightSchedulePlanEntity.getRecurrentEndDate())) {

                    // get latest start date
                    Date startDate = existingFlightSchedulePlanEntity.getFlightSchedules().get(existingFlightSchedulePlanEntity.getFlightSchedules().size() - 1).getDepartureDate();

                    // retrieve the flight duration
                    Integer estimatedFlightHour = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationHour();
                    Integer estimatedFlightMin = existingFlightSchedulePlanEntity.getFlightSchedules().get(0).getEstimatedFlightDurationMinute();

                    List<FlightScheduleEntity> autoGenerateFlightSchedules = new ArrayList<>(); // store generated flight schedules

                    // while existing start date is later than new start date
                    while (startDate.before(newEndDate)) {
                        GregorianCalendar autoCalender = new GregorianCalendar();
                        autoCalender.setTime(startDate);
                        autoCalender.add(GregorianCalendar.HOUR_OF_DAY, 7 * 24); // go next week
                        startDate = autoCalender.getTime();

                        if (startDate.after(newEndDate)) {
                            break;
                        }

                        // generate new flight schedule depending on the date
                        FlightScheduleEntity flightSchedule = new FlightScheduleEntity(startDate, estimatedFlightHour, estimatedFlightMin);
                        autoGenerateFlightSchedules.add(flightSchedule);
                    }

                    existingFlightSchedulePlanEntity.setRecurrentEndDate(newEndDate);
                    if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
                        existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().setRecurrentEndDate(newEndDate);
                    }

                    this.updateAddFlightScheduleToRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, autoGenerateFlightSchedules, existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null);

                    return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();

                } else {
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Same end date as existing end date!");

                }
            } else {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Cannot update selected parameters concurrently!");
            }
        } catch (FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    private Long removeFlightScheduleFromRecurrentFlightSchedulePlan(FlightSchedulePlanEntity existingFlightSchedulePlanEntity, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException {
        // removing flight schedules from a return flight schedule plan
        if (existingFlightSchedulePlanEntity.getIsReturnFlightSchedulePlan()) {
            return removeFlightScheduleFromReturnRecurrentFlightSchedulePlan(existingFlightSchedulePlanEntity, flightScheduleIdsToRemove);
        }

        List<FlightScheduleEntity> existingFlightSchedules = existingFlightSchedulePlanEntity.getFlightSchedules(); // retrieve existing flight schedules

        Iterator<FlightScheduleEntity> iter = existingFlightSchedules.iterator();

        while (iter.hasNext()) {
            FlightScheduleEntity existingFlightScheduleEntity = iter.next();

            if (flightScheduleIdsToRemove.contains(existingFlightScheduleEntity.getFlightScheduleId())) {

                if (!existingFlightScheduleEntity.getFlightReservations().isEmpty()) {
                    em.getTransaction().rollback();
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule has already been reserved");
                }

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
    }

    private Long removeFlightScheduleFromReturnRecurrentFlightSchedulePlan(FlightSchedulePlanEntity returnFlightSchedulePlan, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException {
        // get parent flight schedule plan
        Query query = em.createQuery("SELECT f FROM FlightSchedulePlanEntity f WHERE f.returnFlightSchedulePlan =:inReturnFlightSchedulePlan");
        query.setParameter("inReturnFlightSchedulePlan", returnFlightSchedulePlan);
        FlightSchedulePlanEntity parentFlightSchedulePlan = (FlightSchedulePlanEntity) query.getSingleResult();

        List<FlightScheduleEntity> returnFlightSchedules = returnFlightSchedulePlan.getFlightSchedules(); // retrieve existing flight schedules
        Iterator<FlightScheduleEntity> iter = returnFlightSchedules.iterator();

        while (iter.hasNext()) {
            FlightScheduleEntity returnFlightSchedule = iter.next();

            if (flightScheduleIdsToRemove.contains(returnFlightSchedule.getFlightScheduleId())) {

                if (!returnFlightSchedule.getFlightReservations().isEmpty()) {
                    em.getTransaction().rollback();
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule has already been reserved");
                }

                // remove return flight schedule from parent flight schedule 
                for (FlightScheduleEntity parentFlightSchedule : parentFlightSchedulePlan.getFlightSchedules()) {

                    if (parentFlightSchedule.getReturnFlightSchedule() == null) {
                        continue;
                    }

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

    private Long updateAddFlightScheduleToRecurrentFlightSchedulePlan(FlightSchedulePlanEntity existingFlightSchedulePlanEntity, List<FlightScheduleEntity> newFlightSchedules, Boolean doCreateReturnFlightSchedule) throws UpdateFlightSchedulePlanFailedException {
        try {
            // add new flight schedules to flight schedule plan
            flightScheduleSessionBeanLocal.createNewFlightSchedules(existingFlightSchedulePlanEntity, newFlightSchedules);

            if (doCreateReturnFlightSchedule) {
                FlightSchedulePlanEntity returnFlightSchedulePlan = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan();
                List<FlightScheduleEntity> returnFlightSchedules = new ArrayList<>();

                for (FlightScheduleEntity newFlightScheduleEntity : newFlightSchedules) {
                    FlightScheduleEntity returnFlightSchedule = flightScheduleSessionBeanLocal.createReturnFlightSchedule(newFlightScheduleEntity, existingFlightSchedulePlanEntity.getLayoverPeriod());
                    returnFlightSchedule.setFlightSchedulePlan(returnFlightSchedulePlan); // associate return flight schedule with flight schedule plan
                    returnFlightSchedules.add(returnFlightSchedule);
                }

                flightScheduleSessionBeanLocal.createNewFlightSchedules(returnFlightSchedulePlan, returnFlightSchedules);
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (CreateNewFlightScheduleException ex) {
            em.getTransaction().rollback();
            throw new UpdateFlightSchedulePlanFailedException(ex.getMessage());
        }
    }

    private void checkFlightSchedulePlanUsage(FlightSchedulePlanEntity existingFlightSchedulePlanEntity) throws UpdateFlightSchedulePlanFailedException {
        List<FlightScheduleEntity> flightSchedules = existingFlightSchedulePlanEntity.getFlightSchedules();

        for (FlightScheduleEntity flightSchedule : flightSchedules) {
            if (!flightSchedule.getFlightReservations().isEmpty()) {
                throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule has already been reserved!");
            }
        }

        if (existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) {
            List<FlightScheduleEntity> returnFlightSchedules = existingFlightSchedulePlanEntity.getReturnFlightSchedulePlan().getFlightSchedules();
            for (FlightScheduleEntity flightSchedule : returnFlightSchedules) {
                if (!flightSchedule.getFlightReservations().isEmpty()) {
                    throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Flight schedule has already been reserved!");
                }
            }
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
            em.getTransaction().rollback();
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

            for (FareEntity newFare : newFares) {
                fareEntitySessionBeanLocal.createNewFare(newFare, existingFlightSchedulePlanEntity);
            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (CreateNewFareException | FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
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

            // count number of fare for each avail cabi
            existingFlightSchedulePlanEntity
                    .getFares()
                    .forEach(x
                            -> fareCheck.put(x.getCabinClass(), (fareCheck.get(x.getCabinClass()) + 1))
                    );

            Iterator<FareEntity> iterator = existingFlightSchedulePlanEntity.getFares().iterator();

            while (iterator.hasNext()) {
                FareEntity fareEntity = iterator.next();

                if (fareIds.contains(fareEntity.getFareId())) {

                    // check whether the fare is being used
                    String fareBasisCode = fareEntity.getFareBasisCode();
                    Query query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.fareBasisCode := inFareBasisCode");
                    query.setParameter("inFareBasisCode", fareBasisCode);
                    if (!query.getResultList().isEmpty()) {
                        em.getTransaction().rollback();
                        throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Fare basis code " + fareBasisCode + " is in use!");
                    }

                    // decre the number of fare for the avail cabin class
                    fareCheck.put(fareEntity.getCabinClass(), (fareCheck.get(fareEntity.getCabinClass()) - 1));

                    // if cabin class has no more fare after removal
                    if (fareCheck.get(fareEntity.getCabinClass()) <= 0) {
                        em.getTransaction().rollback();
                        throw new UpdateFlightSchedulePlanFailedException("UpdateFlightSchedulePlanFailedException: Each cabin class must have at least one fare!");
                    }

                    iterator.remove();
                    em.remove(fareEntity);
                }

            }

            em.flush();

            return existingFlightSchedulePlanEntity.getFlightSchedulePlanId();
        } catch (FlightSchedulePlanNotFoundException ex) {
            em.getTransaction().rollback();
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

            if (!flightScheduleEntity.getFlightReservations().isEmpty()) {
                flightSchedulePlanEntity.setIsDisabled(true); // set main to disabled
                if (flightSchedulePlanEntity.getReturnFlightSchedulePlan() != null) { // set return to disabled
                    flightSchedulePlanEntity.getReturnFlightSchedulePlan().setIsDisabled(true);
                }
                em.flush();
                throw new FlightSchedulePlanInUseException("FlightSchedulePlanInUseException: Flight schedule has already been reserved!");
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
            if (!flightScheduleEntity.getFlightReservations().isEmpty()) {
                throw new FlightSchedulePlanInUseException("FlightSchedulePlanInUseException: Flight schedule has already been reserved!");
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
            System.out.println(errorMessage);
            throw new CreateNewFlightSchedulePlanException("CreateNewFlightSchedulePlanException: Invalid inputs!\n" + errorMessage);
        }
    }

}
