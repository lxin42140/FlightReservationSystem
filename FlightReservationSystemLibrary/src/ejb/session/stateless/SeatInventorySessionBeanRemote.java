/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.FlightScheduleEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import javax.ejb.Remote;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewSeatInventoryException;
import util.exception.ReserveSeatException;
import util.exception.SeatNotFoundException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface SeatInventorySessionBeanRemote {

    public void createSeatInventoryForFlightSchedule(FlightScheduleEntity flightScheduleEntity, AircraftConfigurationEntity aircraftConfigurationEntity) throws CreateNewSeatInventoryException;

    public void reserveSeatForPassenger(Long flightScheduleId, CabinClassEnum cabinClassEnum, Long rowNumber, Character rowLetter, PassengerEntity passengerEntity) throws SeatNotFoundException, ReserveSeatException;

    public SeatEntity retrieveSeatFromFlightSchedule(Long flightScheduleId, CabinClassEnum cabinClassEnum, Long rowNumber, Character rowLetter) throws SeatNotFoundException;

}
