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
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewFlightReservationException;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightReservationSessionBeanLocal {

    public FlightReservationEntity viewFlightReservationByFlightReservationId(Long flightReservationId);
    
    public List<FlightReservationEntity> viewFlightReservationByCustomer(Long customerId);

    public Long createNewFlightReservation(List<FlightScheduleEntity> itinery, List<PassengerEntity> passengers, CreditCardEntity creditCardEntity, UserEntity user) throws CreateNewFlightReservationException;


}
