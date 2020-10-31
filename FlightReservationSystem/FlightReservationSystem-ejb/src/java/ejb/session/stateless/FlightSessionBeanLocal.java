/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightEntity;
import javax.ejb.Local;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightSessionBeanLocal {

    public String createNewFlight(FlightEntity newFlightEntity, Long flightRouteId, Long aircraftConfigurationId, Boolean doCreateReturnFlight, String returnFlightNumber) throws CreateNewFlightException, FlightRouteNotFoundException, AircraftConfigurationNotFoundException;
    
}
