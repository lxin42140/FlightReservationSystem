/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightScheduleEntity;
import entity.SeatEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.FlightScheduleNotFoundException;
import util.exception.NoMatchingFlightsException;
import util.exception.SearchFlightFailedException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightSearchSessionBean implements FlightSearchSessionBeanRemote, FlightSearchSessionBeanLocal {

    @EJB
    private SeatInventorySessionBeanLocal seatInventorySessionBeanLocal;

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    // enter null for preferredCabinClass if there is no preference
    // enter null for preferDirectFlight if there is no preference
    // 0 for to flights, 1 for return flights
    @Override
    public List<HashMap<Integer, List<FlightScheduleEntity>>> searchTwoWaysFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Date returnDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException, SearchFlightFailedException {
        if (null == departureAirportId || null == arrivalAirportId || null == departureDate || null == returnDate || numberOfPassengers == null || numberOfPassengers <= 0) {
            throw new SearchFlightFailedException("SearchFlightFailedException: Invalid one or more search parameters!");
        }

//        CabinClassEnum preferredCabinClass = null;
//        if (preferredCabinClassString != null || !preferredCabinClassString.isEmpty()) {
//            preferredCabinClass = CabinClassEnum.valueOf(preferredCabinClassString);
//        }
        List<List<FlightScheduleEntity>> toFlights = new ArrayList<>();
        List<List<FlightScheduleEntity>> returnFlights = new ArrayList<>();

        if (preferDirectFlight != null && preferDirectFlight) {
            toFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            returnFlights = searchDirectFlightSchedules(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
        } else if (preferDirectFlight != null && !preferDirectFlight) {
            List<List<FlightScheduleEntity>> toOneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> toTwoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

            toFlights.addAll(toOneTransistFlights);
            toFlights.addAll(toTwoTransistFlights);

            List<List<FlightScheduleEntity>> returnOneTransistFlights = searchOneTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> returnTwoTransistFlights = searchTwoTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);

            returnFlights.addAll(returnOneTransistFlights);
            returnFlights.addAll(returnTwoTransistFlights);
        } else {
            List<List<FlightScheduleEntity>> toDirectFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> toOneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> toTwoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

            toFlights.addAll(toDirectFlights);
            toFlights.addAll(toOneTransistFlights);
            toFlights.addAll(toTwoTransistFlights);

            List<List<FlightScheduleEntity>> returnDirectFlights = searchDirectFlightSchedules(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> returnOneTransistFlights = searchOneTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> returnTwoTransistFlights = searchTwoTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);

            returnFlights.addAll(returnDirectFlights);
            returnFlights.addAll(returnOneTransistFlights);
            returnFlights.addAll(returnTwoTransistFlights);

        }

        if (toFlights.isEmpty()) {
            throw new NoMatchingFlightsException("NoMatchingFlightsException: No available flights that match the requirements!");
        } else if (returnFlights.isEmpty()) {
            throw new NoMatchingFlightsException("NoMatchingFlightsException: No available return flights that match the requirements!");
        }

        for (List<FlightScheduleEntity> itinery : toFlights) {
            for (FlightScheduleEntity flightSchedule : itinery) {
                em.detach(flightSchedule.getSeatInventory());
            }
        }

        for (List<FlightScheduleEntity> itinery : returnFlights) {
            for (FlightScheduleEntity flightSchedule : itinery) {
                em.detach(flightSchedule.getSeatInventory());
            }
        }

        List<HashMap<Integer, List<FlightScheduleEntity>>> searchResult = new ArrayList<>();
        HashMap<Integer, List<FlightScheduleEntity>> toFlightSearchResults = new HashMap<>();
        HashMap<Integer, List<FlightScheduleEntity>> returnFlightSearchResults = new HashMap<>();

        for (int i = 0; i < toFlights.size(); i++) {
            toFlightSearchResults.put(i + 1, toFlights.get(i));
            returnFlightSearchResults.put(i + 1, returnFlights.get(i));
        }

        searchResult.add(toFlightSearchResults);
        searchResult.add(returnFlightSearchResults);

        return searchResult;
    }

    // enter null for preferredCabinClass if there is no preference
    // enter null for preferDirectFlight if there is no preference
    @Override
    public HashMap<Integer, List<FlightScheduleEntity>> searchOneWayFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException, SearchFlightFailedException {
        if (null == departureAirportId || null == arrivalAirportId || null == departureDate || numberOfPassengers == null || numberOfPassengers <= 0) {
            throw new SearchFlightFailedException("SearchFlightFailedException: Invalid one or more search parameters!");
        }

//        CabinClassEnum preferredCabinClass = null;
//        if (preferredCabinClassString != null || !preferredCabinClassString.isEmpty()) {
//            preferredCabinClass = CabinClassEnum.valueOf(preferredCabinClassString);
//        }
        HashMap<Integer, List<FlightScheduleEntity>> searchResult = new HashMap<>();
        List<List<FlightScheduleEntity>> oneWayFlights = new ArrayList<>();

        if (preferDirectFlight != null && preferDirectFlight) { // client prefer direct flight 
            oneWayFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
        } else if (preferDirectFlight != null && !preferDirectFlight) { // client prefer connecting flights
            List<List<FlightScheduleEntity>> oneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> twoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

            oneWayFlights.addAll(oneTransistFlights);
            oneWayFlights.addAll(twoTransistFlights);
        } else { // retrieve both connecting and direct
            List<List<FlightScheduleEntity>> directFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> oneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> twoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

            oneWayFlights.addAll(directFlights);
            oneWayFlights.addAll(oneTransistFlights);
            oneWayFlights.addAll(twoTransistFlights);
        }
        if (oneWayFlights.isEmpty()) {
            throw new NoMatchingFlightsException("NoMatchingFlightsException: No available flights that match the requirements!");
        }

        for (List<FlightScheduleEntity> itinery : oneWayFlights) {
            for (FlightScheduleEntity flightSchedule : itinery) {
                em.detach(flightSchedule.getSeatInventory());
            }
        }

        for (int i = 0; i < oneWayFlights.size(); i++) {
            searchResult.put(i + 1, oneWayFlights.get(i));
        }

        return searchResult;
    }

    private List<List<FlightScheduleEntity>> searchDirectFlightSchedules(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException {

        List<List<FlightScheduleEntity>> results = new ArrayList<>(); // each list is one flight route

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(departureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 3 * 24);
        Date afterLimit = cal.getTime(); // get limit date for 3 days after the required departure date

        cal.setTime(departureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, -3 * 24);
        Date earlierLimit = cal.getTime(); // get limit date for 3 days prior the required departure date  

        Query query = em.createQuery("SELECT f FROM FlightScheduleEntity f "
                + "WHERE f.departureDate BETWEEN :earlierLimit AND :inAferLimit "
                + "AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inDepartureAirportId "
                + "AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId =:inArrivalAirportId");

        query.setParameter("earlierLimit", earlierLimit);
        query.setParameter("inAferLimit", afterLimit);
        query.setParameter("inDepartureAirportId", departureAirportId);
        query.setParameter("inArrivalAirportId", arrivalAirportId);

        List<FlightScheduleEntity> prelimFlightSchedules = (List<FlightScheduleEntity>) query.getResultList();

        // filter the flight schedules to remove any flight schedule that do not have the sufficent number of empty seats
        List<FlightScheduleEntity> flightSchedulesWithSufficientSeats = prelimFlightSchedules.stream().filter(a -> a.getSeatInventory().stream().filter(s -> s.getPassenger() == null).count() >= numberOfPassengers).collect(Collectors.toList());

        if (preferredCabinClass != null) {
            // filter the flight schedules to remove any flight schedule that do not have the desired cabin class
            List<FlightScheduleEntity> flightSchedulesWithSufficientSeatsAndCabinClass = prelimFlightSchedules.stream().filter(a -> a.getFlightSchedulePlan().getFlight().getAircraftConfiguration().getCabinConfigurations().stream().filter(c -> c.getCabinClass() == preferredCabinClass).count() > 0).collect(Collectors.toList());

            for (FlightScheduleEntity flightSchedule : flightSchedulesWithSufficientSeatsAndCabinClass) {
                List<FlightScheduleEntity> result = new ArrayList<>();
                result.add(flightSchedule);
                results.add(result);
            }

            return results;
        }

        for (FlightScheduleEntity flightSchedule : flightSchedulesWithSufficientSeats) {
            List<FlightScheduleEntity> result = new ArrayList<>();
            result.add(flightSchedule);
            results.add(result);
        }

        return results;
    }

    private List<List<FlightScheduleEntity>> searchOneTransistConnectingFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        List<List<FlightScheduleEntity>> results = new ArrayList<>(); // each list is one connecting routes

        // retrieve base flight
        List<FlightScheduleEntity> baseFlightSchedules = searchBaseFlightForConnectingFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

        if (baseFlightSchedules.isEmpty()) {
            return results;
        }

        Iterator<FlightScheduleEntity> baseFlightIterator = baseFlightSchedules.iterator();

        while (baseFlightIterator.hasNext()) {
            FlightScheduleEntity baseFlightSchedule = baseFlightIterator.next();

            // get connecting flights that connect the current base flight to destination
            List<FlightScheduleEntity> oneTransistFlights = searchEndFlightForConnectingFlightSchedules(baseFlightSchedule, arrivalAirportId, numberOfPassengers, preferredCabinClass);

            if (oneTransistFlights.isEmpty()) { // remove the base flight schedule if it cannot be connected to destination
                baseFlightIterator.remove();
            } else {
                // for every connecting flight, create a new list and add to results
                for (FlightScheduleEntity connectingFlight : oneTransistFlights) {
                    List<FlightScheduleEntity> result = new ArrayList<>();
                    result.add(baseFlightSchedule);
                    result.add(connectingFlight);

                    results.add(result);
                }
            }
        }
        return results;
    }

    private List<List<FlightScheduleEntity>> searchTwoTransistConnectingFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {

        List<List<FlightScheduleEntity>> results = new ArrayList<>(); // each list is one connecting routes

        // retrieve base flight
        List<FlightScheduleEntity> baseFlightSchedules = searchBaseFlightForConnectingFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

        if (baseFlightSchedules.isEmpty()) {
            return results;
        }

        Iterator<FlightScheduleEntity> baseFlightIterator = baseFlightSchedules.iterator();

        while (baseFlightIterator.hasNext()) {
            // get base flight
            FlightScheduleEntity baseFlightSchedule = baseFlightIterator.next();

            // search intermediate flights that connect to base flight
            List<FlightScheduleEntity> intermediateFlights = searchIntermediateFlightForConnectingFlightSchedules(baseFlightSchedule, arrivalAirportId, numberOfPassengers, preferredCabinClass);

            // no intermediate flights at all
            if (intermediateFlights.isEmpty()) { // remove the base flight schedule if it there are no connecting flights
                baseFlightIterator.remove();
                continue;
            }

            Iterator<FlightScheduleEntity> intermediateFlightIterator = intermediateFlights.iterator();

            while (intermediateFlightIterator.hasNext()) {
                // get intermediate flight
                FlightScheduleEntity intermediateFlightSchedule = intermediateFlightIterator.next();

                // get flights that connect intermediate flight to end flight
                List<FlightScheduleEntity> endFlights = searchEndFlightForConnectingFlightSchedules(intermediateFlightSchedule, arrivalAirportId, numberOfPassengers, preferredCabinClass);

                // no end flights
                if (endFlights.isEmpty()) {
                    intermediateFlightIterator.remove(); // intermediate flight cannot connect to destination
                    continue;
                }

                for (FlightScheduleEntity endFlight : endFlights) {
                    List<FlightScheduleEntity> result = new ArrayList<>();
                    result.add(baseFlightSchedule);
                    result.add(intermediateFlightSchedule);
                    result.add(endFlight);

                    results.add(result);
                }
            }

            // intermediate flights cannot connect to destination
            if (intermediateFlights.isEmpty()) {
                baseFlightIterator.remove();
            }
        }

        return results;
    }

    // flights that leave from the departure airport
    private List<FlightScheduleEntity> searchBaseFlightForConnectingFlightSchedules(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(departureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 3 * 24);
        Date afterLimit = cal.getTime(); // get limit date for 3 days after the required departure date

        cal.setTime(departureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, -3 * 24);
        Date earlierLimit = cal.getTime(); // get limit date for 3 days prior the required departure date  

        Query query = em.createQuery("SELECT f FROM FlightScheduleEntity f "
                + "WHERE f.departureDate BETWEEN :earlierLimit AND :inAferLimit "
                + "AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inDepartureAirportId "
                + "AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId <> :inArrivalAirportId");

        query.setParameter("earlierLimit", earlierLimit);
        query.setParameter("inAferLimit", afterLimit);
        query.setParameter("inDepartureAirportId", departureAirportId);
        query.setParameter("inArrivalAirportId", arrivalAirportId);

        List<FlightScheduleEntity> flightSchedules = (List<FlightScheduleEntity>) query.getResultList();

        return filterAvailableFlights(flightSchedules, numberOfPassengers, preferredCabinClass);
    }

    //SIN - TPE, TPE - NSW, NSW - NRT
    private List<FlightScheduleEntity> searchIntermediateFlightForConnectingFlightSchedules(FlightScheduleEntity intermediateFlightScheduleEntity, Long arrivalAirportId, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        GregorianCalendar cal = new GregorianCalendar();

        Date arrivalDateTime = intermediateFlightScheduleEntity.getArrivalDateTime();
        cal.setTime(arrivalDateTime);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 2); // lay over of 2 hours
        Date minDepartureDate = cal.getTime(); // min departure time for connecting flight

        cal.setTime(minDepartureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 8);
        Date maxDepartureDate = cal.getTime();

        // origin airport of the connecting flight is the destination airport of the prior flight;
        Long originAirportId = intermediateFlightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId();

        // retrieve a list of flight schedules that connect the base flight schedule and not to the destination airport
        Query query = em.createQuery("SELECT f FROM FlightScheduleEntity f "
                + "WHERE f.departureDate BETWEEN :inMinDepartureDate AND :inMaxDepartureDate "
                + "AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inOriginAirportId "
                + "AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId <> :inArrivalAirportId");

        query.setParameter("inMinDepartureDate", minDepartureDate);
        query.setParameter("inMaxDepartureDate", maxDepartureDate);
        query.setParameter("inOriginAirportId", originAirportId);
        query.setParameter("inArrivalAirportId", arrivalAirportId);

        List<FlightScheduleEntity> flightSchedules = (List<FlightScheduleEntity>) query.getResultList();

        return filterAvailableFlights(flightSchedules, numberOfPassengers, preferredCabinClass);
    }

    //SIN - TPE, TPE - NRT
    private List<FlightScheduleEntity> searchEndFlightForConnectingFlightSchedules(FlightScheduleEntity baseFlightScheduleEntity, Long arrivalAirportId, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        GregorianCalendar cal = new GregorianCalendar();

        Date arrivalDateTime = baseFlightScheduleEntity.getArrivalDateTime();
        cal.setTime(arrivalDateTime);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 2); // lay over of 2 hours
        Date minDepartureDate = cal.getTime(); // min departure time for connecting flight

        cal.setTime(minDepartureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 8);
        Date maxDepartureDate = cal.getTime(); // min departure time for connecting flight

        // origin airport of the connecting flight is the destination airport of the prior flight;
        Long originAirportId = baseFlightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId();

        // retrieve a list of flight schedules that connect the base flight schedule to the destinaion airport
        Query query = em.createQuery("SELECT f FROM FlightScheduleEntity f "
                + "WHERE f.departureDate BETWEEN :inMinDepartureDate AND :inMaxDepartureDate "
                + "AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inOriginAirportId "
                + "AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId =:inArrivalAirportId");

        query.setParameter("inMinDepartureDate", minDepartureDate);
        query.setParameter("inMaxDepartureDate", maxDepartureDate);
        query.setParameter("inOriginAirportId", originAirportId);
        query.setParameter("inArrivalAirportId", arrivalAirportId);

        List<FlightScheduleEntity> flightSchedules = (List<FlightScheduleEntity>) query.getResultList();

        return filterAvailableFlights(flightSchedules, numberOfPassengers, preferredCabinClass);
    }

    private List<FlightScheduleEntity> filterAvailableFlights(List<FlightScheduleEntity> flightSchedules, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        Iterator<FlightScheduleEntity> iterator = flightSchedules.iterator();

        while (iterator.hasNext()) {
            FlightScheduleEntity flightSchedule = iterator.next();

            try {
                // make sure return flight schedule has enough seats
                SeatInventory seatInventory = seatInventorySessionBeanLocal.viewSeatsInventoryByFlightScheduleId(flightSchedule.getFlightScheduleId());

                // not enough seats for return flight 
                if (seatInventory.getTotalAvailSeats() < numberOfPassengers) {
                    iterator.remove();
                    continue;
                }

                // not enogh seats for the preferred cabin
                if (preferredCabinClass != null) {
                    HashMap<CabinClassEnum, Integer[]> cabinSeatsInventory = seatInventory.getCabinSeatsInventory();
                    if (cabinSeatsInventory.get(preferredCabinClass)[2] < numberOfPassengers) {
                        iterator.remove();
                    }
                }
            } catch (FlightScheduleNotFoundException ex) {
                iterator.remove();
            }
        }

        return flightSchedules;
    }

}
