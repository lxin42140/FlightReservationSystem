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
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Local;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewSeatInventoryException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.ReserveSeatException;
import util.exception.SeatNotFoundException;

/**
 *
 * @author Li Xin
 */
@Local
public interface SeatInventorySessionBeanLocal {

    public void createSeatInventoryForFlightSchedule(FlightScheduleEntity flightScheduleEntity, AircraftConfigurationEntity aircraftConfigurationEntity) throws CreateNewSeatInventoryException;

    public SeatInventory viewSeatsInventoryByFlightScheduleId(Long flightScheduleId) throws FlightScheduleNotFoundException;

    public SeatEntity retrieveAvailableSeatFromFlightScheduleAndCabin(Long flightScheduleId, CabinClassEnum cabinClassEnum, String seatNumber) throws SeatNotFoundException, ReserveSeatException;

    public List<SeatEntity> retrieveAllAvailableSeatsFromFlightScheduleAndCabin(Long flightScheduleId, CabinClassEnum cabinClassEnum);

    public void reserveSeatsForCustomer(PassengerEntity passenger) throws ReserveSeatException;

    public void reserveSeatsForPartner(PassengerEntity passenger) throws ReserveSeatException;

    public List<SeatEntity> retrieveReservedSeatsByFlightScheduleId(Long flightScheduleId);

    public SeatEntity retrieveSeatById(Long seatId) throws SeatNotFoundException;

}
