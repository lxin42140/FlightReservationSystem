/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CreditCardEntity;
import entity.FlightReservationEntity;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.exception.CreateNewCreditCardException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class CreditCardSessionBean implements CreditCardSessionBeanRemote, CreditCardSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public void createNewCreditCard(CreditCardEntity creditCard, FlightReservationEntity flightReservation) throws CreateNewCreditCardException {
        creditCard.setFlightReservation(flightReservation);
        flightReservation.setCreditCard(creditCard);
        
        validate(creditCard);
    }

    private void validate(CreditCardEntity creditCard) throws CreateNewCreditCardException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<CreditCardEntity>> errors = validator.validate(creditCard);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewCreditCardException("CreateNewCreditCardException: Invalid inputs!\n" + errorMessage);
        }
    }
}
