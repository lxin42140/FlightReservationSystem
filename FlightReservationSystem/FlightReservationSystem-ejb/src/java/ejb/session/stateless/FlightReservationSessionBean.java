/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CreditCardEntity;
import entity.FlightReservationEntity;
import entity.FlightScheduleEntity;
import entity.PassengerEntity;
import entity.UserEntity;
import java.math.BigDecimal;
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
    public List<FlightReservationEntity> viewFlightReservationsByFlightScheduleId(Long flightScheduleId) {
        Query query = em.createQuery("SELECT DISTINCT f from FlightReservationEntity f, IN (f.flightSchedules) fs, IN (f.passengers.seats) s WHERE fs.flightScheduleId =:inFlightScheduleId ORDER BY s.seatNumber");
        query.setParameter("inFlightScheduleId", flightScheduleId);
        List<FlightReservationEntity> flightReservations = query.getResultList();

        return flightReservations;
    }

    // every passenger needs to have a list of seats FOR EACH FLIGHT SCHEDULE, along with the required information in the attributes
    @Override
    public Long createNewFlightReservationForNoReturnFlight(List<Long> flightScheduleIds, List<PassengerEntity> passengers, CreditCardEntity creditCardEntity, UserEntity user) throws CreateNewFlightReservationException {
        FlightReservationEntity newFlightReservation = new FlightReservationEntity();

        try {
            // add flight schedules to flight reservation
            for (Long flightScheduleId : flightScheduleIds) {
                FlightScheduleEntity flightSchedule = flightScheduleSessionBeanLocal.retrieveFlightScheduleById(flightScheduleId);
                newFlightReservation.getFlightSchedules().add(flightSchedule);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PassengerEntity passenger : passengers) {
                // associate passenger with flight reservation and check seat
                passengerSessionBeanLocal.createNewPassenger(passenger, newFlightReservation);
                // calculate total amount
                totalAmount = totalAmount.add(passenger.getFareAmount());
            }
            newFlightReservation.setTotalAmount(totalAmount);

            // associate credit card and flight reservation
            creditCardSessionBeanLocal.createNewCreditCard(creditCardEntity, newFlightReservation);

            // associate user with new flight reservation
            newFlightReservation.setUser(user);
            user.getFlightReservations().add(newFlightReservation);

            validate(newFlightReservation);

            em.persist(newFlightReservation);
            em.flush();

            return newFlightReservation.getFlightReservationId();
        } catch (FlightScheduleNotFoundException | CreateNewPassengerException | CreateNewCreditCardException ex) {
            throw new CreateNewFlightReservationException(ex.getMessage());
        }

    }

    @Override
    public Long createNewFlightReservationForReturnFlight(List<Long> toFlightScheduleIds, List<Long> returnFlightScheduleIds, List<PassengerEntity> passengers, CreditCardEntity creditCardEntity, UserEntity user) throws CreateNewFlightReservationException {
        FlightReservationEntity newFlightReservation = new FlightReservationEntity();

        try {
            // add flight schedules to flight reservation
            for (Long toFlightScheduleId : toFlightScheduleIds) {
                FlightScheduleEntity flightSchedule = flightScheduleSessionBeanLocal.retrieveFlightScheduleById(toFlightScheduleId);
                newFlightReservation.getFlightSchedules().add(flightSchedule);
            }

            for (Long returnFlightScheduleId : returnFlightScheduleIds) {
                FlightScheduleEntity flightSchedule = flightScheduleSessionBeanLocal.retrieveFlightScheduleById(returnFlightScheduleId);
                newFlightReservation.getFlightSchedules().add(flightSchedule);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PassengerEntity passenger : passengers) {
                // associate passenger with flight reservation and check seat
                passengerSessionBeanLocal.createNewPassenger(passenger, newFlightReservation);
                // calculate total amount
                totalAmount = totalAmount.add(passenger.getFareAmount());
            }
            newFlightReservation.setTotalAmount(totalAmount);

            // associate credit card and flight reservation
            creditCardSessionBeanLocal.createNewCreditCard(creditCardEntity, newFlightReservation);

            // associate user with new flight reservation
            newFlightReservation.setUser(user);
            user.getFlightReservations().add(newFlightReservation);

            validate(newFlightReservation);

            em.persist(newFlightReservation);
            em.flush();

            return newFlightReservation.getFlightReservationId();
        } catch (FlightScheduleNotFoundException | CreateNewPassengerException | CreateNewCreditCardException ex) {
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
