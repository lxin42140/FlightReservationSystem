/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AirportEntity;
import entity.FlightRouteEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewFlightRouteException;
import util.exception.DeleteFlightRouteException;
import util.exception.FlightRouteInUseException;
import util.exception.FlightRouteNotFoundException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightRouteSessionBean implements FlightRouteSessionBeanRemote, FlightRouteSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public Long createNewFlightRoute(Long originAirportId, Long destinationAirportId, Boolean doCreateReturnFlight) throws CreateNewFlightRouteException, AirportNotFoundException {
        AirportEntity originAirport = em.find(AirportEntity.class, originAirportId);

        if (originAirport == null) {
            throw new AirportNotFoundException("AirportNotFoundException: Origin airport with ID " + originAirportId + " does not exist!");
        }

        AirportEntity destinationAirport = em.find(AirportEntity.class, destinationAirportId);

        if (destinationAirport == null) {
            throw new AirportNotFoundException("AirportNotFoundException: Destination airport with ID " + destinationAirportId + " does not exist!");
        }

        FlightRouteEntity newFlightRouteEntity = new FlightRouteEntity();

        newFlightRouteEntity.setOriginAirport(originAirport);
        newFlightRouteEntity.setDestinationAirport(destinationAirport);

        validateNewFlightRoute(newFlightRouteEntity);

        if (doCreateReturnFlight) {
            return createNewFlightRouteWithReturnFlight(newFlightRouteEntity);
        } else {
            return createNewFlightRouteWithoutReturnFlight(newFlightRouteEntity);
        }

    }

    private Long createNewFlightRouteWithoutReturnFlight(FlightRouteEntity newFlightRouteEntity) {
        em.persist(newFlightRouteEntity);
        em.flush();
        return newFlightRouteEntity.getFlightRouteId();
    }

    private Long createNewFlightRouteWithReturnFlight(FlightRouteEntity newFlightRouteEntity) {

        FlightRouteEntity returnFlightRouteEntity = new FlightRouteEntity(newFlightRouteEntity.getDestinationAirport(), newFlightRouteEntity.getOriginAirport()); // reverse origin and destination
        returnFlightRouteEntity.setIsReturnFlightRoute(true); // indicate that it is the return flight
        newFlightRouteEntity.setReturnFlightRoute(returnFlightRouteEntity); // associate flight with return flight

        em.persist(newFlightRouteEntity);
        em.flush();

        return newFlightRouteEntity.getFlightRouteId();
    }

    private void validateNewFlightRoute(FlightRouteEntity newFlightRouteEntity) throws CreateNewFlightRouteException {
        validate(newFlightRouteEntity);
        if (!newFlightRouteEntity.isValid()) {
            throw new CreateNewFlightRouteException("CreateNewFlightRouteException: Destination airport and origin airport must be different!");
        }
        if (!isUniqueODpair(newFlightRouteEntity)) {
            throw new CreateNewFlightRouteException("CreateNewFlightRouteException: Flight route with origin airport and destination airport already exists!");
        }
    }

    private void validate(FlightRouteEntity flightRouteEntity) throws CreateNewFlightRouteException {
        javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<FlightRouteEntity>> errors = validator.validate(flightRouteEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += error.getPropertyPath() + ": " + error.getInvalidValue() + " - " + error.getMessage() + "\n";
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewFlightRouteException("CreateNewFlightRouteException: Invalid inputs!\n" + errorMessage);
        }
    }

    private boolean isUniqueODpair(FlightRouteEntity flightRouteEntity) {
        if (retrieveAllFlightRoutes().isEmpty()) { // no flight routes in database
            return true;
        }

        try {
            Query query = em.createQuery("SELECT f FROM FlightRouteEntity f WHERE f.originAirport = :inputOriginAirport AND f.destinationAirport = :inputDestinationAirport AND f.isDisabled = FALSE");
            query.setParameter("inputOriginAirport", flightRouteEntity.getDestinationAirport());
            query.setParameter("inputDestinationAirport", flightRouteEntity.getDestinationAirport());
            return false;
        } catch (NoResultException ex) {
            return true;
        }
    }

    @Override
    public FlightRouteEntity retrieveFlightRouteById(Long flightRouteId) throws FlightRouteNotFoundException {
        FlightRouteEntity flightRouteEntity = em.find(FlightRouteEntity.class, flightRouteId);

        if (flightRouteEntity == null) {
            throw new FlightRouteNotFoundException("FlightRouteNotFoundException: Flight route with ID " + flightRouteId + " does not exist!");
        }

        flightRouteEntity.getFlights().size();
        return flightRouteEntity;
    }

    @Override
    public List<FlightRouteEntity> retrieveAllFlightRoutes() {
        // order by origin aiport 
        Query query = em.createQuery("SELECT f FROM FlightRouteEntity f WHERE f.isReturnFlightRoute = FALSE ORDER BY f.originAirport.airportName ASC");
        List<FlightRouteEntity> flightRoutes = (List<FlightRouteEntity>) query.getResultList();

        return flightRoutes;
    }

    @Override
    public void deleteFlightRouteById(Long flightRouteId) throws FlightRouteNotFoundException, FlightRouteInUseException {
        FlightRouteEntity flightRouteEntity = em.find(FlightRouteEntity.class, flightRouteId);

        if (flightRouteEntity == null) {
            throw new FlightRouteNotFoundException("FlightRouteNotFoundException: Flight route with ID " + flightRouteId + " does not exist!");
        }

        if (flightRouteEntity.getIsReturnRlight()) {
            deleteReturnFlightRoute(flightRouteEntity);
            return;
        }

        if (!flightRouteEntity.getFlights().isEmpty()) { // flight route in use
            flightRouteEntity.setIsDisabled(true); // set flight to disabled
            if (flightRouteEntity.getReturnFlightRoute() != null) {
                flightRouteEntity.getReturnFlightRoute().setIsDisabled(true); // set return flight route to disabled
            }
            em.flush();
            throw new FlightRouteInUseException("FlightRouteInUseException: Flight route with ID " + flightRouteId + " is in use! " + "\nFlight route is now disabled!");
        }

        em.remove(flightRouteEntity);
    }

    private void deleteReturnFlightRoute(FlightRouteEntity returnFlightRouteEntity) throws FlightRouteInUseException {
        Query query = em.createQuery("SELECT f FROM FlightRouteEntity f WHERE f.returnFlightRoute = :inReturnFlightRoute"); // find parent flight
        query.setParameter("inReturnFlightRoute", returnFlightRouteEntity);
        FlightRouteEntity parentFlightRouteEntity = (FlightRouteEntity) query.getSingleResult();

        parentFlightRouteEntity.setReturnFlightRoute(null); // remove association from parent to return flight route
        em.merge(parentFlightRouteEntity); // update parent

        if (!returnFlightRouteEntity.getFlights().isEmpty()) {
            // flights associated with the return flight
            returnFlightRouteEntity.setIsDisabled(true); // set disabled
            throw new FlightRouteInUseException("FlightRouteInUseException: Flight route with ID " + returnFlightRouteEntity.getFlightRouteId() + " is in use! " + "\nFlight route is now disabled!");
        }

        em.remove(returnFlightRouteEntity);
    }

}
