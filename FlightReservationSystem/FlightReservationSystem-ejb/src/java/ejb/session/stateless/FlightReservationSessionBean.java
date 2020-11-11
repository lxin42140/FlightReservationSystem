/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CreditCardEntity;
import entity.CustomerEntity;
import entity.FlightReservationEntity;
import entity.FlightScheduleEntity;
import entity.PartnerEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import entity.UserEntity;
import java.util.ArrayList;
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
import util.exception.CreateNewCreditCardException;
import util.exception.CreateNewFlightReservationException;
import util.exception.CreateNewPassengerException;
import util.exception.FlightScheduleNotFoundException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightReservationSessionBean implements FlightReservationSessionBeanRemote, FlightReservationSessionBeanLocal {

    @EJB
    private PassengerSessionBeanLocal passengerSessionBeanLocal;

    @EJB
    private CreditCardSessionBeanLocal creditCardSessionBeanLocal;

    @EJB
    private FlightScheduleSessionBeanLocal flightScheduleSessionBeanLocal;

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public FlightReservationEntity viewFlightReservationsByFlightScheduleId(Long flightReservationId) {
        
        Query query = em.createQuery("SELECT r FROM FlightReservationEntity r WHERE r.flightReservationId = :inFlightReservationId");
        query.setParameter("inFlightReservationId", flightReservationId);
        
        return (FlightReservationEntity) query.getSingleResult();

//        FlightScheduleEntity flightSchedule = flightScheduleSessionBeanLocal.retrieveFlightScheduleById(flightScheduleId);

//        // get a list of seats that are booked
//        List<SeatEntity> reservedSeats = new ArrayList<>();
//
//        for (SeatEntity seat : flightSchedule.getSeatInventory()) {
//            if (seat.getPassenger() != null) {
//                reservedSeats.add(seat);
//            }
//        }
//
//        // sort base on cabin class
//        reservedSeats.sort(((SeatEntity a, SeatEntity b) -> {
//            return a.getCabinClassEnum().compareTo(b.getCabinClassEnum());
//        }));
//
//        // sort base on seat number
//        reservedSeats.sort(((SeatEntity a, SeatEntity b) -> {
//            return a.getSeatNumber().compareTo(b.getSeatNumber());
//        }));
//
//        return reservedSeats;
    }

    @Override
    public List<FlightReservationEntity> viewFlightReservationByCustomer(Long customerId) {

        Query query = em.createQuery("SELECT r FROM FlightReservationEntity r WHERE r.user.userId = :inUserId");
        query.setParameter("inUserId", customerId);

        List<FlightReservationEntity> flightReservationEntity = (List<FlightReservationEntity>) query.getResultList();
        
        return flightReservationEntity;
    }

    /*
    1. prompt user to enter serial number to retrieve List<FlightScheduleEntity> itinery
    2. create a list of passenger entities - passengers
    3.   for each FlightScheduleEntity in itinery
            for each passenger in passengers
                1. prompt user to select cabin
                2. display available seats in the cabin -- retrieveAllAvailableSeatsFromFlightScheduleAndCabin 
                3. prompt user to select seat by entering seat number -- retrieveAvailableSeatFromFlightScheduleAndCabin    
                4. add retrieved seat to list of seats in passenger -- ONLY THIS ASSOCIATION
    4. create credit card entity
    
    5. If there is a return flight schedule, follow repeat above process and add the return flight schedules to the same list
    aka List<FlightScheduleEntity> itinery.addAll(List<FlightScheduleEntity> returnItinery)
    then call this method
     */
    @Override
    public Long createNewFlightReservation(List<FlightScheduleEntity> itinery, List<PassengerEntity> passengers, CreditCardEntity creditCardEntity, UserEntity user) throws CreateNewFlightReservationException {
        if (creditCardEntity == null || itinery.isEmpty() || passengers.isEmpty() || user == null) {
            throw new CreateNewFlightReservationException("CreateNewFlightReservationException: One or more reservation requirements are not provided!");
        }

        FlightReservationEntity newFlightReservation = new FlightReservationEntity();

        try {
            // associate user and flight reservation
            newFlightReservation.setUser(user);
            user.getFlightReservations().add(newFlightReservation);

            // associate flight schedule with reservation
            newFlightReservation.getFlightSchedules().addAll(itinery);
            for (FlightScheduleEntity flightSchedule : itinery) {
                flightSchedule.getFlightReservations().add(newFlightReservation);
            }

            // associate credit card and flight reservation
            creditCardSessionBeanLocal.createNewCreditCard(creditCardEntity, newFlightReservation);

            // associate selected seats of each passenger with the passenger and the passenger with flight reservation
            passengerSessionBeanLocal.addPassengersToReservation(passengers, newFlightReservation, user instanceof CustomerEntity);
            
            validate(newFlightReservation);

            em.persist(newFlightReservation);
            em.flush();
            return newFlightReservation.getFlightReservationId();
        } catch (CreateNewPassengerException | CreateNewCreditCardException ex) {
            throw new CreateNewFlightReservationException(ex.getMessage());
        }
    }

    private void validate(FlightReservationEntity flightReservationEntity) throws CreateNewFlightReservationException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<FlightReservationEntity>> errors = validator.validate(flightReservationEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFlightReservationException("CreateNewFlightReservationException: Invalid inputs!\n" + errorMessage);
        }
    }

}
