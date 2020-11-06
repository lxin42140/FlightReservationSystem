/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightScheduleEntity;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Local;
import util.enumeration.CabinClassEnum;
import util.exception.NoMatchingFlightsException;
import util.exception.SearchFlightFailedException;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightSearchSessionBeanLocal {

    public HashMap<Integer, List<List<FlightScheduleEntity>>> searchTwoWaysFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Date returnDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException, SearchFlightFailedException;

    public List<List<FlightScheduleEntity>> searchOneWayFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException, SearchFlightFailedException;
    
}
