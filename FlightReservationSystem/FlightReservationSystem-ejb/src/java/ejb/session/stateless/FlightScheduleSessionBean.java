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
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
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
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewFlightScheduleException;
import util.exception.CreateNewSeatInventoryException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.FlightSchedulePlanNotFoundException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightScheduleSessionBean implements FlightScheduleSessionBeanRemote, FlightScheduleSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @EJB
    private SeatInventorySessionBeanLocal seatInventorySessionBeanLocal;

    @Override
    public void createNewFlightSchedules(FlightSchedulePlanEntity flightSchedulePlanEntity, List<FlightScheduleEntity> flightSchedules) throws CreateNewFlightScheduleException {
        if (flightSchedules.isEmpty()) {
            throw new CreateNewFlightScheduleException("CreateNewFlightScheduleException: Please provide at least one flight schedule!");
        }

        // create new flight schedule for every flight schedule in list
        for (FlightScheduleEntity flightScheduleEntity : flightSchedules) {
            this.createNewFlightSchedule(flightScheduleEntity, flightSchedulePlanEntity);
        }
    }

    private void createNewFlightSchedule(FlightScheduleEntity flightScheduleEntity, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFlightScheduleException {

        // 1. associate flight schedule plan with flight schedule
        flightScheduleEntity.setFlightSchedulePlan(flightSchedulePlanEntity);

        // 2. check whether the new flight schedule conflict with any of existing flight schedules for flight
        checkFlightSchedules(flightScheduleEntity, flightSchedulePlanEntity.getFlight());

        // 3. add new flight schedule to flight schedule plan if no conflict
        flightSchedulePlanEntity.getFlightSchedules().add(flightScheduleEntity);

        // create seat inventory for the flight schedule
        try {
            seatInventorySessionBeanLocal.createSeatInventoryForFlightSchedule(flightScheduleEntity, flightSchedulePlanEntity.getFlight().getAircraftConfiguration());
        } catch (CreateNewSeatInventoryException ex) {
            throw new CreateNewFlightScheduleException(ex.toString());
        }

        validateFlightSchedule(flightScheduleEntity);
    }

    // helper method to create retrun flight schedule
    @Override
    public FlightScheduleEntity createReturnFlightSchedule(FlightScheduleEntity flightSchedule, Integer layoverPeriodForReturnFlight) {
        Date arrivalDateTime = flightSchedule.getArrivalDateTime(); // arrival time is already calculated based on time zone of destination airport
        GregorianCalendar returnDepartureDateTimeCalender = new GregorianCalendar();
        returnDepartureDateTimeCalender.setTime(arrivalDateTime);

        returnDepartureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, layoverPeriodForReturnFlight); // lay over period is determiend by user

        Date returnDepartureDateTime = returnDepartureDateTimeCalender.getTime(); // get return departure time

        FlightScheduleEntity returnFlightSchedule = new FlightScheduleEntity(returnDepartureDateTime, flightSchedule.getEstimatedFlightDurationHour(), flightSchedule.getEstimatedFlightDurationMinute());

        flightSchedule.setReturnFlightSchedule(returnFlightSchedule); // associate flight schedule and its return flight schedule

        return returnFlightSchedule;
    }

    @Override  // local interface only
    public void checkFlightSchedules(FlightScheduleEntity newFlightScheduleEntity, FlightEntity flightEntity) throws CreateNewFlightScheduleException {
        // retrieve all flight schedule plans associated with flight
        List<FlightSchedulePlanEntity> existingFlightSchedulePlans = flightEntity.getFlightSchedulePlans();

        for (FlightSchedulePlanEntity existingFlightSchedulePlan : existingFlightSchedulePlans) {
            // retrieve flight schedule for each flight schedule plan record
            List<FlightScheduleEntity> existingFlightSchedules = existingFlightSchedulePlan.getFlightSchedules();
            // check if any flight schedule conflict with the new flight schedule
            for (FlightScheduleEntity existingFlightSchedule : existingFlightSchedules) {
                if (existingFlightSchedule.equals(newFlightScheduleEntity)) {
                    // same flight schedule as existing, in the event when flight schedule details are changed
                    continue;
                }

                if (checkConflictBetweenTwoSchedule(existingFlightSchedule, newFlightScheduleEntity)) {
                    throw new CreateNewFlightScheduleException("CreateNewFlightScheduleException: New flight schedule conflicts with existing flight schedule!");
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

    @Override
    public FlightScheduleEntity retrieveFlightScheduleById(Long flightScheduleId) throws FlightScheduleNotFoundException {
        FlightScheduleEntity flightScheduleEntity = em.find(FlightScheduleEntity.class, flightScheduleId);

        if (flightScheduleEntity == null) {
            throw new FlightScheduleNotFoundException("FlightScheduleNotFoundException: Flight schedule with id " + flightScheduleId + " does not exsit!");
        }

        if (flightScheduleEntity.getFlightSchedulePlan().getIsDisabled()) {
            throw new FlightScheduleNotFoundException("FlightScheduleNotFoundException: Flight schedule with id " + flightScheduleId + " is disabled!");
        }

        return flightScheduleEntity;
    }

//    @Override
//    public BigDecimal getLowestFareForCabin(Long flightSchedulePlanId, String cabinClass) throws FlightSchedulePlanNotFoundException {
//        FlightSchedulePlanEntity flightSchedulePlan = this.retrieveFlightSchedulePlanById(flightSchedulePlanId);
//
//        if (cabinClass == null || cabinClass.isEmpty()) {
//            throw new FlightSchedulePlanNotFoundException("Please select cabin class!");
//        }
//
//        CabinClassEnum selectedCabinClass = CabinClassEnum.valueOf(cabinClass);
//
//        List<FareEntity> fares = flightSchedulePlan.getFares();
//        FareEntity cheapestFare = fares.get(0);
//
//        for (FareEntity fare : fares) {
//            if (fare.getCabinClass().equals(selectedCabinClass)) {
//                cheapestFare = fare;
//                break;
//            }
//        }
//
//        for (FareEntity fare : fares) {
//            if (fare.getCabinClass().equals(selectedCabinClass) && fare.getFareAmount().compareTo(cheapestFare.getFareAmount()) < 0) {
//                cheapestFare = fare;
//            }
//        }
//
//        return cheapestFare.getFareAmount();
//    }

    @Override
    public HashMap<CabinClassEnum, FareEntity> getHighestFaresForCabin(Long flightScheduleId) throws FlightScheduleNotFoundException {
        FlightScheduleEntity flightSchedule = this.retrieveFlightScheduleById(flightScheduleId);
        List<FareEntity> fares = flightSchedule.getFlightSchedulePlan().getFares();
        HashMap<CabinClassEnum, FareEntity> results = new HashMap<>();

        HashSet<CabinClassEnum> cabinClasses = new HashSet<>();

        for (FareEntity fare : fares) {
            cabinClasses.add(fare.getCabinClass());
        }

        for (CabinClassEnum cabinClass : cabinClasses) {
            results.put(cabinClass, findHighestFareForCabin(fares, cabinClass));
        }

        return results;
    }

    private FareEntity findHighestFareForCabin(List<FareEntity> fares, CabinClassEnum cabinClassEnum) {
        FareEntity highestFare = fares.get(0);

        for (FareEntity fare : fares) {
            if (fare.getCabinClass().equals(cabinClassEnum)) {
                highestFare = fare;
                break;
            }
        }

        for (FareEntity fare : fares) {
            if (fare.getCabinClass().equals(cabinClassEnum) && fare.getFareAmount().compareTo(highestFare.getFareAmount()) > 0) {
                highestFare = fare;
            }
        }

        return highestFare;
    }

    @Override
    public HashMap<CabinClassEnum, Double> getLowestFaresForCabin(Long flightScheduleId) throws FlightScheduleNotFoundException {
        FlightScheduleEntity flightSchedule = this.retrieveFlightScheduleById(flightScheduleId);
        List<FareEntity> fares = flightSchedule.getFlightSchedulePlan().getFares();
        HashMap<CabinClassEnum, Double> results = new HashMap<>();

        HashSet<CabinClassEnum> cabinClasses = new HashSet<>();

        for (FareEntity fare : fares) {
            cabinClasses.add(fare.getCabinClass());
        }

        for (CabinClassEnum cabinClass : cabinClasses) {
            results.put(cabinClass, findLowestFareForCabin(fares, cabinClass));
        }

        return results;
    }

    private double findLowestFareForCabin(List<FareEntity> fares, CabinClassEnum cabinClassEnum) {
        FareEntity lowestFare = fares.get(0);

        for (FareEntity fare : fares) {
            if (fare.getCabinClass().equals(cabinClassEnum)) {
                lowestFare = fare;
                break;
            }
        }

        for (FareEntity fare : fares) {
            if (fare.getCabinClass().equals(cabinClassEnum) && fare.getFareAmount().compareTo(lowestFare.getFareAmount()) < 0) {
                lowestFare = fare;
            }
        }

        return lowestFare.getFareAmount().doubleValue();
    }

}
