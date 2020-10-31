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
import javax.ejb.EJB;
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

    @EJB
    private FlightRouteSessionBeanLocal flightRouteSessionBeanLocal;

    @EJB
    private AircraftConfigurationSessionBeanLocal aircraftConfigurationSessionBeanLocal;

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public String createNewFlight(FlightEntity newFlightEntity, Long flightRouteId, Long aircraftConfigurationId, Boolean doCreateReturnFlight, String returnFlightNumber) throws CreateNewFlightException, FlightRouteNotFoundException, AircraftConfigurationNotFoundException {
        FlightRouteEntity flightRouteEntity = flightRouteSessionBeanLocal.retrieveFlightRouteById(flightRouteId);
        AircraftConfigurationEntity aircraftConfigurationEntity = aircraftConfigurationSessionBeanLocal.retrieveAircraftTypeById(aircraftConfigurationId);

        newFlightEntity.setAircraftConfiguration(aircraftConfigurationEntity); // associate flight with aircraft config
        newFlightEntity.setFlightRoute(flightRouteEntity); // associate flight with flight route
        flightRouteEntity.getFlights().add(newFlightEntity); // associate flight route with flight

        validate(newFlightEntity);

        if (doCreateReturnFlight) {
            return createNewFlightWithReturnFlight(newFlightEntity, returnFlightNumber);
        } else {
            return createNewFlightWithoutReturnFlight(newFlightEntity);
        }
    }

    private String createNewFlightWithoutReturnFlight(FlightEntity newFlightEntity) {

        em.persist(newFlightEntity);
        em.flush();

        return newFlightEntity.getIataAirlineCode() + newFlightEntity.getFlightNumber();
    }

    private String createNewFlightWithReturnFlight(FlightEntity newFlightEntity, String returnFlightNumber) throws CreateNewFlightException {
        // create return flight with same IATA code, new return flight number, and the return flight route
        FlightEntity returnFlightEntity = new FlightEntity(newFlightEntity.getIataAirlineCode(), returnFlightNumber, newFlightEntity.getAircraftConfiguration(), newFlightEntity.getFlightRoute().getReturnFlightRoute());
        returnFlightEntity.setIsReturnFlight(true);

        // associate the return flight route with the return flight 
        returnFlightEntity.getFlightRoute().getFlights().add(returnFlightEntity);

        validate(returnFlightEntity);

        newFlightEntity.setReturnFlight(returnFlightEntity);
        em.persist(newFlightEntity);
        em.flush();

        return newFlightEntity.getIataAirlineCode() + newFlightEntity.getFlightNumber();
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

}
