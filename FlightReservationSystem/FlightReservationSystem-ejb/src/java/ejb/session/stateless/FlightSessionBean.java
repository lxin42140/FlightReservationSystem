/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.FlightEntity;
import entity.FlightRouteEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
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

        if (flightRouteEntity.getIsDisabled()) {
            throw new CreateNewFlightException("CreateNewFlightException: Selected flight route is disabled!");
        }

        if (doCreateReturnFlight && flightRouteEntity.getReturnFlightRoute() == null) {
            throw new CreateNewFlightException("CreateNewFlightException: Selected flight route does not has a return flight route!");
        }

        AircraftConfigurationEntity aircraftConfigurationEntity = aircraftConfigurationSessionBeanLocal.retrieveAircraftConfigurationById(aircraftConfigurationId);

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

    private String createNewFlightWithoutReturnFlight(FlightEntity newFlightEntity) throws CreateNewFlightException {
        try {
            em.persist(newFlightEntity);
            em.flush();
            return newFlightEntity.getFlightNumber();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewFlightException("CreateNewFlightException: Flight with same flight number already exists!");
            } else {
                throw new CreateNewFlightException("CreateNewFlightException: " + ex.getMessage());
            }
        }
    }

    private String createNewFlightWithReturnFlight(FlightEntity newFlightEntity, String returnFlightNumber) throws CreateNewFlightException {
        // get return flight route
        FlightRouteEntity returnFlightRoute = newFlightEntity.getFlightRoute().getReturnFlightRoute();

        if (returnFlightRoute.getIsDisabled()) {
            throw new CreateNewFlightException("CreateNewFlightException: Return flight route is disabled!");
        }

        // create new return flight entity
        FlightEntity returnFlightEntity = new FlightEntity(returnFlightNumber, newFlightEntity.getAircraftConfiguration(), returnFlightRoute);
        // set as return flight
        returnFlightEntity.setIsReturnFlight(true);
        // add return flight to return flight route
        returnFlightRoute.getFlights().add(returnFlightEntity);

        validate(returnFlightEntity);

        // assoicare return flight with main flight
        newFlightEntity.setReturnFlight(returnFlightEntity);

        try {
            em.persist(newFlightEntity);
            em.flush();
            return newFlightEntity.getFlightNumber();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewFlightException("CreateNewFlightException: Flight with same flight number already exists!");
            } else {
                throw new CreateNewFlightException("CreateNewFlightException: " + ex.getMessage());
            }
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
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
        Query query = em.createQuery("SELECT f from FlightEntity f WHERE f.isDisabled = FALSE AND f.isReturnFlight = FALSE ORDER BY f.flightNumber ASC");
        List<FlightEntity> flightEntities = query.getResultList();
        return flightEntities;
    }

    @Override
    public FlightEntity retrieveFlightByFlightNumber(String flightNumber) throws FlightNotFoundException {
        Query query = em.createQuery("SELECT f FROM FlightEntity f WHERE f.flightNumber =:inFlightNumber");
        query.setParameter("inFlightNumber", flightNumber);

        try {
            return (FlightEntity) query.getSingleResult();
        } catch (NoResultException ex) {
            throw new FlightNotFoundException("FlightNotFoundException: Flight number " + flightNumber + " does not exist!");
        }
    }

    // set return flight number to null if not used
    @Override
    public String updateFlightNumberForFlight(FlightEntity flightEntity, String newFlightNumber, String returnFlightNumber) throws UpdateFlightFailedException {

        if (!flightEntity.getFlightSchedulePlans().isEmpty()) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: Flight already in use and unable to update flight number!");
        }

        if (flightEntity.getReturnFlight() == null && returnFlightNumber != null) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: Flight has no complementary return flight for update of flight number!");
        }

        Query query = em.createQuery("SELECT f FROM FlightEntity f WHERE f.flightNumber =:inFlightNumber OR f.flightNumber =:inReturnFlightNumber");
        query.setParameter("inFlightNumber", newFlightNumber);
        query.setParameter("inReturnFlightNumber", returnFlightNumber);

        List<FlightEntity> conflicts = (List< FlightEntity>) query.getResultList();

        if (!conflicts.isEmpty()) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: New flight number already exists!");
        }

        if (flightEntity.getIsReturnFlight()) {
            flightEntity.setFlightNumber(newFlightNumber);
            em.merge(flightEntity);
            em.flush();

            return flightEntity.getFlightNumber();
        }

        flightEntity.setFlightNumber(newFlightNumber);

        if (flightEntity.getReturnFlight() != null && returnFlightNumber != null) {
            flightEntity.getReturnFlight().setFlightNumber(returnFlightNumber);
        }

        em.merge(flightEntity);
        em.flush();

        return flightEntity.getFlightNumber();
    }

    @Override
    public String updateAircraftConfigurationForFlight(FlightEntity flightEntity, Long newAircraftConfigurationId) throws UpdateFlightFailedException {
        if (!flightEntity.getFlightSchedulePlans().isEmpty()) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: Flight already in use and unable to update aircraft configuration!");
        }

        if (flightEntity.getIsReturnFlight()) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: Unable to update aircraft configuration for a return flight!");
        }

        try {
            AircraftConfigurationEntity newAircraftConfigurationEntity = aircraftConfigurationSessionBeanLocal.retrieveAircraftConfigurationById(newAircraftConfigurationId);
            flightEntity.setAircraftConfiguration(newAircraftConfigurationEntity);

            //update aircraft config for return flight if any
            if (flightEntity.getReturnFlight() != null) {
                FlightEntity returnFlight = flightEntity.getReturnFlight();
                returnFlight.setAircraftConfiguration(newAircraftConfigurationEntity);
            }

            em.merge(flightEntity);
            em.flush();

            return flightEntity.getFlightNumber();
        } catch (AircraftConfigurationNotFoundException ex) {
            throw new UpdateFlightFailedException(ex.getMessage());
        }
    }

    @Override
    public String updateFlightRouteForFlight(FlightEntity flightEntity, Long newFlightRouteId) throws UpdateFlightFailedException {
        if (!flightEntity.getFlightSchedulePlans().isEmpty()) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: Flight already in use and unable to update flight route!");
        }

        if (flightEntity.getIsReturnFlight()) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: Unable to update flight route for a return flight!");
        }

        try {
            // remove existing flight from old flight route
            flightEntity.getFlightRoute().getFlights().remove(flightEntity);

            FlightRouteEntity newFlightRouteEntity = flightRouteSessionBeanLocal.retrieveFlightRouteById(newFlightRouteId);
            flightEntity.setFlightRoute(newFlightRouteEntity);
            newFlightRouteEntity.getFlights().add(flightEntity);

            //update route for return flight if any
            if (flightEntity.getReturnFlight() != null) {

                if (newFlightRouteEntity.getReturnFlightRoute() == null) {
                    throw new UpdateFlightFailedException("UpdateFlightFailedException: New flight route does not has complementary return flight route!");
                }

                FlightEntity returnFlight = flightEntity.getReturnFlight();

                // remove return flight from associated flight route
                returnFlight.getFlightRoute().getFlights().remove(returnFlight);

                FlightRouteEntity newReturnFlightRouteEntity = newFlightRouteEntity.getReturnFlightRoute();
                returnFlight.setFlightRoute(newReturnFlightRouteEntity);
                newReturnFlightRouteEntity.getFlights().add(returnFlight);
            }

            em.merge(flightEntity);
            em.flush();

            return flightEntity.getFlightNumber();
        } catch (FlightRouteNotFoundException ex) {
            throw new UpdateFlightFailedException(ex.getMessage());
        }
    }

    @Override
    public void deleteFlightByFlightNumber(String flightNumber) throws FlightNotFoundException, FlightInUseException {
        Query query = em.createQuery("SELECT f FROM FlightEntity f WHERE f.flightNumber =:inFlightNumber");
        query.setParameter("inFlightNumber", flightNumber);
        FlightEntity flight = (FlightEntity)query.getSingleResult();
        FlightEntity flightEntity = em.find(FlightEntity.class, flight.getFlightId());

        if (flightEntity == null) {
            throw new FlightNotFoundException("FlightNotFoundException: Flight number " + flightNumber + " does not exist!");
        }

        if (flightEntity.getIsReturnFlight()) { // deleting a return flight
            deleteReturnFlight(flightEntity);
            return;
        }

        if (!flightEntity.getFlightSchedulePlans().isEmpty()) { // flight is in use
            flightEntity.setIsDisabled(true); // set flight as disabled
            if (flightEntity.getReturnFlight() != null) { // flight has return flight, disable return flight
                flightEntity.getReturnFlight().setIsDisabled(true);
            }
            em.flush();
            throw new FlightInUseException("FlightInUseException: Flight number " + flightNumber + " is in use!" + "\nFlight is now disabled!");
        }

        flightEntity.getFlightRoute().getFlights().remove(flightEntity);

        if (flightEntity.getReturnFlight() != null) {
            flightEntity.getReturnFlight().getFlightRoute().getFlights().remove(flightEntity.getReturnFlight());
        }

        em.remove(flightEntity);
    }

    private void deleteReturnFlight(FlightEntity returnFlightEntity) throws FlightInUseException {
        if (!returnFlightEntity.getFlightSchedulePlans().isEmpty()) {
            returnFlightEntity.setIsDisabled(true); // set disabled
            em.flush();
            throw new FlightInUseException("FlightInUseException: Flight number " + returnFlightEntity.getFlightNumber() + " is in use!" + "\nFlight is now disabled!");
        }

        Query query = em.createQuery("SELECT f FROM FlightEntity f WHERE f.returnFlight = :inReturnFlight"); // find parent flight
        query.setParameter("inReturnFlight", returnFlightEntity);
        FlightEntity parentFlightEntity = (FlightEntity) query.getSingleResult();

        parentFlightEntity.setReturnFlight(null); // remove association from parent to return flight 

        // remove return flight from flight route
        returnFlightEntity.getFlightRoute().getFlights().remove(returnFlightEntity);
        em.remove(returnFlightEntity);
    }

}
