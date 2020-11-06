/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightReservationEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewPassengerException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class PassengerSessionBean implements PassengerSessionBeanRemote, PassengerSessionBeanLocal {
    
//    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
//    private EntityManager em;
    
    @Override
    public void createNewPassenger(PassengerEntity passenger, FlightReservationEntity flightReservation) throws CreateNewPassengerException {
        passenger.setFlightReservation(flightReservation);
        flightReservation.getPassengers().add(passenger);
        
        for (SeatEntity seatEntity : passenger.getSeats()) {
            if (seatEntity.getPassenger() != null) {
                throw new CreateNewPassengerException("CreateNewPassengerException: Selected seat for passenger " + passenger.getFirstName() + " " + passenger.getLastName() + " has already been reserved!");
            }
            seatEntity.setPassenger(passenger);
        }
        
        validate(passenger);
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
    
}
