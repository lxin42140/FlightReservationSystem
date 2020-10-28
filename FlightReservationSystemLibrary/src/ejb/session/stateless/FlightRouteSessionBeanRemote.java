/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightRouteEntity;
import java.util.List;
import javax.ejb.Remote;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewFlightRouteException;
import util.exception.FlightRouteInUseException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface FlightRouteSessionBeanRemote {

    public FlightRouteEntity retrieveFlightRouteById(Long flightRouteId) throws FlightRouteNotFoundException;

    public List<FlightRouteEntity> retrieveAllFlightRoutes();

    public void deleteFlightRouteById(Long flightRouteId) throws FlightRouteNotFoundException, FlightRouteInUseException;

    public Long createNewFlightRoute(FlightRouteEntity newFlightRouteEntity, Long originAirportId, Long destinationAirportId, Boolean doCreateReturnFlight) throws CreateNewFlightRouteException, AirportNotFoundException;

}
