/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.FlightInUseException;
import util.exception.FlightNotFoundException;
import util.exception.FlightRouteNotFoundException;
import util.exception.UpdateFlightFailedException;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightSessionBeanLocal {

    public String createNewFlight(FlightEntity newFlightEntity, Long flightRouteId, Long aircraftConfigurationId, Boolean doCreateReturnFlight, String returnFlightNumber) throws CreateNewFlightException, FlightRouteNotFoundException, AircraftConfigurationNotFoundException;

    public List<FlightEntity> retrieveAllFlights();

    public String updateFlight(FlightEntity updateFlightEntity) throws UpdateFlightFailedException;

    public FlightEntity retrieveFlightById(String iataCode, String flightNumber) throws FlightNotFoundException;

    public void deleteFlightById(String iataCode, String flightNumber) throws FlightNotFoundException, FlightInUseException;
    
}
