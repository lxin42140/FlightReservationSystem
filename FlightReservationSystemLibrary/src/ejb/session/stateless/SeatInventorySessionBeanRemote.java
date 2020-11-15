/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.FlightScheduleEntity;
import entity.SeatEntity;
import java.util.List;
import javax.ejb.Remote;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewSeatInventoryException;
import util.exception.FlightScheduleNotFoundException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface SeatInventorySessionBeanRemote {

    public void createSeatInventoryForFlightSchedule(FlightScheduleEntity flightScheduleEntity, AircraftConfigurationEntity aircraftConfigurationEntity) throws CreateNewSeatInventoryException;

    public SeatInventory viewSeatsInventoryByFlightScheduleId(Long flightScheduleId) throws FlightScheduleNotFoundException;

//    public void reserveSeatForPassenger(Long flightScheduleId, CabinClassEnum cabinClassEnum, Long rowNumber, Character rowLetter, PassengerEntity passengerEntity) throws SeatNotFoundException, ReserveSeatException;
//
//    public SeatEntity retrieveSeatFromFlightSchedule(Long flightScheduleId, CabinClassEnum cabinClassEnum, Long rowNumber, Character rowLetter) throws SeatNotFoundException;
//    public SeatEntity retrieveAvailableSeatFromFlightScheduleAndCabin(Long flightScheduleId, CabinClassEnum cabinClassEnum, String seatNumber) throws SeatNotFoundException, ReserveSeatException;
    public List<SeatEntity> retrieveAllAvailableSeatsFromFlightScheduleAndCabin(Long flightScheduleId, CabinClassEnum cabinClassEnum);

    public List<SeatEntity> retrieveReservedSeatsByFlightScheduleId(Long flightScheduleId);

}
