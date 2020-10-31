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
        // create return flight with new return flight number, and the return flight route
        FlightEntity returnFlightEntity = new FlightEntity(returnFlightNumber, newFlightEntity.getAircraftConfiguration(), newFlightEntity.getFlightRoute().getReturnFlightRoute());
        returnFlightEntity.setIsReturnFlight(true);

        // associate the return flight route with the return flight 
        returnFlightEntity.getFlightRoute().getFlights().add(returnFlightEntity);

        validate(returnFlightEntity);

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
        Query query = em.createQuery("SELECT f from FlightEntity f WHERE f.isReturnFlight = FALSE ORDER BY f.flightNumber ASC");
        List<FlightEntity> flightEntities = query.getResultList();
        return flightEntities;
    }

    @Override
    public FlightEntity retrieveFlightByFlightNumber(String flightNumber) throws FlightNotFoundException {
        FlightEntity flightEntity = em.find(FlightEntity.class, flightNumber);

        if (flightEntity == null) {
            throw new FlightNotFoundException("FlightNotFoundException: Flight with numer " + flightNumber + " does not exist!");
        }
        return flightEntity;
    }

    @Override
    public String updateFlight(FlightEntity updateFlightEntity) throws UpdateFlightFailedException, FlightNotFoundException {

        try {
            validate(updateFlightEntity);
        } catch (CreateNewFlightException ex) {
            throw new UpdateFlightFailedException("UpdateFlightFailedException: " + ex);
        }

        FlightEntity flightEntity = this.retrieveFlightByFlightNumber(updateFlightEntity.getFlightNumber());

        flightEntity.setFlightNumber(updateFlightEntity.getFlightNumber()); // update flight number
        flightEntity.setReturnFlight(updateFlightEntity.getReturnFlight()); // update return flight
        flightEntity.setFlightRoute(updateFlightEntity.getFlightRoute()); // update flight route
        flightEntity.setAircraftConfiguration(updateFlightEntity.getAircraftConfiguration()); // update aircraft configuration
        flightEntity.setFlightSchedulePlans(updateFlightEntity.getFlightSchedulePlans()); // update flightscheduleplan
        //flightEntity.setIsDisabled(updateFlightEntity.isIsDisabled()); // update disable status ?

        em.merge(updateFlightEntity);
        em.flush();

        return updateFlightEntity.getFlightNumber();
    }

    @Override
    public void deleteFlightByFlightNumber(String flightNumber) throws FlightNotFoundException, FlightInUseException {
        FlightEntity flightEntity = em.find(FlightEntity.class, flightNumber);

        if (flightEntity == null) {
            throw new FlightNotFoundException("FlightNotFoundException: Flight with numer " + flightNumber + " does not exist!");
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
            throw new FlightInUseException("FlightInUseException: Flight with numer " + flightNumber + " is in use!" + "\nFlight is now disabled!");
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
            throw new FlightInUseException("FlightInUseException: Flight with numer " + returnFlightEntity.getFlightNumber() + " is in use!" + "\nFlight is now disabled!");
        }

        em.remove(returnFlightEntity);
    }

}
