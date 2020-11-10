/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightReservationEntity;
import entity.PassengerEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewPassengerException;

/**
 *
 * @author Li Xin
 */
@Local
public interface PassengerSessionBeanLocal {

    public void addPassengersToReservation(List<PassengerEntity> passengers, FlightReservationEntity flightReservation, boolean isCustomerReservation) throws CreateNewPassengerException;

}
