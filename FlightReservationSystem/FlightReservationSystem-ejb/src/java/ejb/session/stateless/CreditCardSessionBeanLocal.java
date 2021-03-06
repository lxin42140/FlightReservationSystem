/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CreditCardEntity;
import entity.FlightReservationEntity;
import javax.ejb.Local;
import util.exception.CreateNewCreditCardException;

/**
 *
 * @author Li Xin
 */
@Local
public interface CreditCardSessionBeanLocal {

    public void createNewCreditCard(CreditCardEntity creditCard, FlightReservationEntity flightReservation) throws CreateNewCreditCardException;
    
}
