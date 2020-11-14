/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightReservationEntity;
import entity.PassengerEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewPassengerException;
import util.exception.ReserveSeatException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class PassengerSessionBean implements PassengerSessionBeanRemote, PassengerSessionBeanLocal {

    @EJB
    private SeatInventorySessionBeanLocal seatInventorySessionBeanLocal;

    @Override // local oonly
    public void addPassengersToReservation(List<PassengerEntity> passengers, FlightReservationEntity flightReservation, boolean isCustomerReservation) throws CreateNewPassengerException {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PassengerEntity passenger : passengers) {

            // passenger must have seats chosen
            if (passenger.getSeats().isEmpty()) {
                throw new CreateNewPassengerException("CreateNewPassengerException: No seats chosen for passenger " + passenger.getFirstName() + " " + passenger.getLastName());
            }

            // associate passenger with reservation
            passenger.setFlightReservation(flightReservation);
            flightReservation.getPassengers().add(passenger);

            try {
                if (isCustomerReservation) {
                    seatInventorySessionBeanLocal.reserveSeatsForCustomer(passenger);
                } else {
                    seatInventorySessionBeanLocal.reserveSeatsForPartner(passenger);
                }
            } catch (ReserveSeatException ex) {
                throw new CreateNewPassengerException(ex.getMessage());
            }

            totalAmount = totalAmount.add(passenger.getFareAmount());
            validate(passenger);
        }

        flightReservation.setTotalAmount(totalAmount);
    }

    private void validate(PassengerEntity passenger) throws CreateNewPassengerException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<PassengerEntity>> errors = validator.validate(passenger);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewPassengerException("CreateNewPassengerException: Invalid inputs!\n" + errorMessage);
        }
    }

    public PassengerEntity createNewPassenger(String firstName, String lastName, String passportNumber) {
        PassengerEntity passenger = new PassengerEntity();

        passenger.setFirstName(firstName);
        passenger.setLastName(lastName);
        passenger.setPassportNumber(passportNumber);

        return passenger;
    }

}
