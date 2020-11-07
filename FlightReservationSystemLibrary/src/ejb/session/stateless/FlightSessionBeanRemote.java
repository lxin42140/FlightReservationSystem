/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightEntity;
import java.util.List;
import javax.ejb.Remote;
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
@Remote
public interface FlightSessionBeanRemote {

    public String createNewFlight(FlightEntity newFlightEntity, Long flightRouteId, Long aircraftConfigurationId, Boolean doCreateReturnFlight, String returnFlightNumber) throws CreateNewFlightException, FlightRouteNotFoundException, AircraftConfigurationNotFoundException;

    public List<FlightEntity> retrieveAllFlights();

    public FlightEntity retrieveFlightByFlightNumber(String flightNumber) throws FlightNotFoundException;

    public String updateFlightNumberForFlight(FlightEntity flightEntity, String newFlightNumber, String returnFlightNumber) throws UpdateFlightFailedException;

    public String updateAircraftConfigurationForFlight(FlightEntity flightEntity, Long newAircraftConfigurationId) throws UpdateFlightFailedException;

    public String updateFlightRouteForFlight(FlightEntity flightEntity, Long newFlightRouteId) throws UpdateFlightFailedException;

    public void deleteFlightByFlightNumber(String flightNumber) throws FlightNotFoundException, FlightInUseException;

}
