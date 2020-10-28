/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.FlightEntity;
import entity.FlightRouteEntity;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.CreateNewFlightException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightSessionBean implements FlightSessionBeanRemote, FlightSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public String createNewFlight(FlightEntity newFlightEntity, Long flightRouteId, Long aircraftConfigurationId, Boolean doCreateReturnFlight) throws CreateNewFlightException, FlightRouteNotFoundException, AircraftConfigurationNotFoundException {
        FlightRouteEntity flightRouteEntity = em.find(FlightRouteEntity.class, flightRouteId);
        if (flightRouteEntity == null) {
            throw new FlightRouteNotFoundException("FlightRouteNotFoundException: Flight route with ID " + flightRouteId + " does not exist!");
        }

        AircraftConfigurationEntity aircraftConfigurationEntity = em.find(AircraftConfigurationEntity.class, aircraftConfigurationId);
        if (aircraftConfigurationEntity == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: Aircraft configuration with ID " + aircraftConfigurationId + " does not exist!");
        }

        newFlightEntity.setAircraftConfiguration(aircraftConfigurationEntity); // associate flight with aircraft config
        newFlightEntity.setFlightRoute(flightRouteEntity); // associate flight with flight route
        flightRouteEntity.getFlights().add(newFlightEntity); // associate flight route with flight

        validate(newFlightEntity);

        if (doCreateReturnFlight) {

        } else {
            createNewFlightWithoutReturnFlight(newFlightEntity);
        }
        return"";
        
    }

    private String createNewFlightWithoutReturnFlight(FlightEntity newFlightEntity) {

        em.persist(newFlightEntity);
        em.flush();

        return newFlightEntity.getIataAirlineCode() + newFlightEntity.getFlightNumber();

    }

    private String createNewFlightWithReturnFlight(FlightEntity newFlightEntity) {
        return "";
    }

    private void validate(FlightEntity flightEntity) throws CreateNewFlightException {
        javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<FlightEntity>> errors = validator.validate(flightEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += error.getPropertyPath() + ": " + error.getInvalidValue() + " - " + error.getMessage() + "\n";
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFlightException("CreateNewFlightException: Invalid inputs!\n" + errorMessage);
        }
    }

    private void validateFlightNumber(FlightEntity flightEntity) throws CreateNewFlightException {

    }
}
