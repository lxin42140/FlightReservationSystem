/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.FlightEntity;
import entity.FlightNumberEntityKey;
import entity.FlightRouteEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
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
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFlightException("CreateNewFlightException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public List<FlightEntity> retrieveAllFlights() {
        Query query = em.createQuery("SELECT f from FlightEntity f ORDER BY f.iataAirlineCode, f.flightNumber ASC");
        List<FlightEntity> flightEntities = query.getResultList();
        return flightEntities;
    }

    @Override
    public FlightEntity retrieveFlightById(String iataCode, String flightNumber) throws FlightNotFoundException {
        FlightEntity flightEntity = em.find(FlightEntity.class, new FlightNumberEntityKey(iataCode, flightNumber));
        if (flightEntity == null) {
            throw new FlightNotFoundException("FlightNotFoundException: Flight with numer " + iataCode + flightNumber + " does not exist!");
        }
        return flightEntity;
    }

    @Override
    public String updateFlight(FlightEntity updateFlightEntity) throws UpdateFlightFailedException {

        try {
            validate(updateFlightEntity);
        } catch (CreateNewFlightException ex) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: " + ex);
        }

        FlightEntity flightEntityToUpdate = em.find(FlightEntity.class, new FlightNumberEntityKey(updateFlightEntity.getIataAirlineCode(), updateFlightEntity.getFlightNumber()));
        em.merge(updateFlightEntity);

        em.flush();

        return updateFlightEntity.getIataAirlineCode() + updateFlightEntity.getFlightNumber();
    }

    @Override
    public void deleteFlightById(String iataCode, String flightNumber) throws FlightNotFoundException, FlightInUseException {
        FlightEntity flightEntity = em.find(FlightEntity.class, new FlightNumberEntityKey(iataCode, flightNumber));

        if (flightEntity == null) {
            throw new FlightNotFoundException("FlightNotFoundException: Flight with numer " + iataCode + flightNumber + " does not exist!");
        }

        if (flightEntity.getIsReturnFlight()) {
            deleteReturnFlight(flightEntity);
            return;
        }

        if (!flightEntity.getFlightSchedulePlans().isEmpty()) { // flight is in use
            flightEntity.setIsDisabled(true); // set flight as disabled
            if (flightEntity.getReturnFlight() != null) { // flight has return flight, disable return flight
                flightEntity.getReturnFlight().setIsDisabled(true);
            }
            em.flush();
            throw new FlightInUseException("FlightInUseException: Flight with numer " + iataCode + flightNumber + " is in use!" + "\nFlight is now disabled!");
        }
        em.remove(flightEntity);
    }

    private void deleteReturnFlight(FlightEntity returnFlightEntity) throws FlightInUseException {
        Query query = em.createQuery("SELECT f FROM FlightEntity f WHERE f.returnFlight = :inReturnFlight"); // find parent flight
        query.setParameter("inReturnFlight", returnFlightEntity);
        FlightEntity parentFlightEntity = (FlightEntity) query.getSingleResult();

        parentFlightEntity.setReturnFlight(null); // remove association from parent to return flight 
        em.merge(parentFlightEntity); // update parent

        if (!returnFlightEntity.getFlightSchedulePlans().isEmpty()) {
            // flights associated with the return flight
            returnFlightEntity.setIsDisabled(true); // set disabled
            throw new FlightInUseException("FlightInUseException: Flight with numer " + returnFlightEntity.getIataAirlineCode() + returnFlightEntity.getFlightNumber() + " is in use!" + "\nFlight is now disabled!");
        }

        em.remove(returnFlightEntity);
    }

}
