/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CreditCardEntity;
import entity.FlightReservationEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import entity.UserEntity;
import java.util.List;
import javax.ejb.Remote;
import util.exception.CreateNewFlightReservationException;
import util.exception.FlightScheduleNotFoundException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface FlightReservationSessionBeanRemote {

    public List<SeatEntity> viewFlightReservationsByFlightScheduleId(Long flightScheduleId) throws FlightScheduleNotFoundException;

//    public Long createNewFlightReservationForNoReturnFlight(List<Long> flightScheduleIds, List<PassengerEntity> passengers, CreditCardEntity creditCardEntity, UserEntity user) throws CreateNewFlightReservationException;
//
//    public Long createNewFlightReservationForReturnFlight(List<Long> toFlightScheduleIds, List<Long> returnFlightScheduleIds, List<PassengerEntity> passengers, CreditCardEntity creditCardEntity, UserEntity user) throws CreateNewFlightReservationException;

}
