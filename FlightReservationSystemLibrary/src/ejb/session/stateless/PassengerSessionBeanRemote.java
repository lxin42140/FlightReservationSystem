/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightReservationEntity;
import entity.PassengerEntity;
import javax.ejb.Remote;
import util.exception.CreateNewPassengerException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface PassengerSessionBeanRemote {

    public void createNewPassenger(PassengerEntity passenger, FlightReservationEntity flightReservation) throws CreateNewPassengerException;

}
