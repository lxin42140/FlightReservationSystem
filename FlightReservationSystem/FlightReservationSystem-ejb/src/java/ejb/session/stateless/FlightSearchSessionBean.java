/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightScheduleEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import util.enumeration.CabinClassEnum;
import util.exception.NoMatchingFlightsException;
import util.exception.SearchFlightFailedException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightSearchSessionBean implements FlightSearchSessionBeanRemote, FlightSearchSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    // enter null for preferredCabinClass if there is no preference
    // enter null for preferDirectFlight if there is no preference
    // 1 for to flights, 2 for return flights
    @Override
    public HashMap<Integer, List<List<FlightScheduleEntity>>> searchTwoWaysFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Date returnDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException, SearchFlightFailedException {
        if (null == departureAirportId || null == arrivalAirportId || null == departureDate || null == returnDate || numberOfPassengers == null || numberOfPassengers <= 0) {
            throw new SearchFlightFailedException("SearchFlightFailedException: Invalid one or more search parameters!");
        }

        HashMap<Integer, List<List<FlightScheduleEntity>>> results = new HashMap<>();

        if (preferDirectFlight != null && preferDirectFlight) {
            List<List<FlightScheduleEntity>> toFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);

            if (toFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available direct flights that match the requirements!");
            }

            results.put(1, toFlights);

            List<List<FlightScheduleEntity>> returnFlights = searchDirectFlightSchedules(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);

            if (returnFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available direct return flights that match the requirements!");
            }

            results.put(2, returnFlights);

            return results;
        } else if (preferDirectFlight != null && !preferDirectFlight) {
            List<List<FlightScheduleEntity>> toOneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> toTwoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            toOneTransistFlights.addAll(toTwoTransistFlights);

            if (toOneTransistFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available connecting flights that match the requirements!");
            }

            results.put(1, toOneTransistFlights);

            List<List<FlightScheduleEntity>> returnOneTransistFlights = searchOneTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> returnTwoTransistFlights = searchTwoTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            returnOneTransistFlights.addAll(returnTwoTransistFlights);

            if (returnOneTransistFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available connecting return flights that match the requirements!");
            }

            results.put(2, returnOneTransistFlights);

            return results;
        } else {
            List<List<FlightScheduleEntity>> toDirectFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> toOneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> toTwoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            toDirectFlights.addAll(toOneTransistFlights);
            toDirectFlights.addAll(toTwoTransistFlights);

            if (toDirectFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available flights that match the requirements!");
            }

            results.put(1, toDirectFlights);

            List<List<FlightScheduleEntity>> returnDirectFlights = searchDirectFlightSchedules(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> returnOneTransistFlights = searchOneTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> returnTwoTransistFlights = searchTwoTransistConnectingFlights(arrivalAirportId, departureAirportId, returnDate, numberOfPassengers, preferredCabinClass);
            returnDirectFlights.addAll(returnOneTransistFlights);
            returnDirectFlights.addAll(returnTwoTransistFlights);

            if (returnDirectFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available return flights that match the requirements!");
            }

            results.put(1, returnDirectFlights);

            return results;
        }
    }

    // enter null for preferredCabinClass if there is no preference
    // enter null for preferDirectFlight if there is no preference
    @Override
    public List<List<FlightScheduleEntity>> searchOneWayFlights(Long departureAirportId, Long arrivalAirportId, Date departureDate, Integer numberOfPassengers, Boolean preferDirectFlight, CabinClassEnum preferredCabinClass) throws NoMatchingFlightsException, SearchFlightFailedException {
        if (null == departureAirportId || null == arrivalAirportId || null == departureDate || numberOfPassengers == null || numberOfPassengers <= 0) {
            throw new SearchFlightFailedException("SearchFlightFailedException: Invalid one or more search parameters!");
        }

        if (preferDirectFlight != null && preferDirectFlight) { // client prefer direct flight 
            List<List<FlightScheduleEntity>> directFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            if (directFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available direct flights that match the requirements!");
            }
            return directFlights;
        } else if (preferDirectFlight != null && !preferDirectFlight) { // client prefer connecting flights
            List<List<FlightScheduleEntity>> oneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> twoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            if (oneTransistFlights.isEmpty() && twoTransistFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available connecting flights that match the requirements!");
            }
            oneTransistFlights.addAll(twoTransistFlights); // add all two transit flights to one transist flights
            return oneTransistFlights;
        } else { // retrieve both connecting and direct
            List<List<FlightScheduleEntity>> directFlights = searchDirectFlightSchedules(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> oneTransistFlights = searchOneTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            List<List<FlightScheduleEntity>> twoTransistFlights = searchTwoTransistConnectingFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferredCabinClass);
            if (directFlights.isEmpty() && oneTransistFlights.isEmpty() && twoTransistFlights.isEmpty()) {
                throw new NoMatchingFlightsException("NoMatchingFlightsException: No available flights that match the requirements!");
            }
            directFlights.addAll(oneTransistFlights);
            directFlights.addAll(twoTransistFlights);
            return directFlights;
        }
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
                + "WHERE f.departureDate >= :earlierLimit AND f.departureDate <= :inAferLimit "
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
                + "WHERE f.departureDate >= :earlierLimit AND f.departureDate <= :inAferLimit "
                + "AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inDepartureAirportId "
                + "AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId <> :inArrivalAirportId");

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
            return flightSchedulesWithSufficientSeatsAndCabinClass;
        }

        return flightSchedulesWithSufficientSeats;
    }

    //SIN - TPE, TPE - NSW, NSW - NRT
    private List<FlightScheduleEntity> searchIntermediateFlightForConnectingFlightSchedules(FlightScheduleEntity intermediateFlightScheduleEntity, Long arrivalAirportId, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        GregorianCalendar cal = new GregorianCalendar();

        Date arrivalDateTime = intermediateFlightScheduleEntity.getArrivalDateTime();
        cal.setTime(arrivalDateTime);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 2); // lay over of 2 hours
        Date minDepartureDate = cal.getTime(); // min departure time for connecting flight

        cal.setTime(minDepartureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 4); // lay over of 2 hours
        Date maxDepartureDate = cal.getTime(); // min departure time for connecting flight

        // origin airport of the connecting flight is the destination airport of the prior flight;
        Long originAirportId = intermediateFlightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId();

        // retrieve a list of flight schedules that connect the base flight schedule and not to the destination airport
        Query query = em.createQuery("SELECT f FROM FlightScheduleEntity f WHERE f.departureDate >= :inMinDepartureDate AND f.departureDate <= :inMaxDepartureDate AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inOriginAirportId AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId <> :inArrivalAirportId");
        query.setParameter("inMinDepartureDate", minDepartureDate);
        query.setParameter("inMaxDepartureDate", maxDepartureDate);
        query.setParameter("inOriginAirportId", originAirportId);
        query.setParameter("inArrivalAirportId", arrivalAirportId);

        List<FlightScheduleEntity> prelimFlightSchedules = (List<FlightScheduleEntity>) query.getResultList();

        List<FlightScheduleEntity> flightSchedulesWithSufficientSeats = prelimFlightSchedules.stream().filter(a -> a.getSeatInventory().stream().filter(s -> s.getPassenger() == null).count() >= numberOfPassengers).collect(Collectors.toList());

        if (preferredCabinClass != null) {
            // filter the flight schedules to remove any flight schedule that do not have the desired cabin class
            List<FlightScheduleEntity> flightSchedulesWithSufficientSeatsAndCabinClass = prelimFlightSchedules.stream().filter(a -> a.getFlightSchedulePlan().getFlight().getAircraftConfiguration().getCabinConfigurations().stream().filter(c -> c.getCabinClass() == preferredCabinClass).count() > 0).collect(Collectors.toList());
            return flightSchedulesWithSufficientSeatsAndCabinClass;
        }

        return flightSchedulesWithSufficientSeats;
    }

    //SIN - TPE, TPE - NRT
    private List<FlightScheduleEntity> searchEndFlightForConnectingFlightSchedules(FlightScheduleEntity baseFlightScheduleEntity, Long arrivalAirportId, Integer numberOfPassengers, CabinClassEnum preferredCabinClass) {
        GregorianCalendar cal = new GregorianCalendar();

        Date arrivalDateTime = baseFlightScheduleEntity.getArrivalDateTime();
        cal.setTime(arrivalDateTime);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 2); // lay over of 2 hours
        Date minDepartureDate = cal.getTime(); // min departure time for connecting flight

        cal.setTime(minDepartureDate);
        cal.add(GregorianCalendar.HOUR_OF_DAY, 4); // lay over of 2 hours
        Date maxDepartureDate = cal.getTime(); // min departure time for connecting flight

        // origin airport of the connecting flight is the destination airport of the prior flight;
        Long originAirportId = baseFlightScheduleEntity.getFlightSchedulePlan().getFlight().getFlightRoute().getDestinationAirport().getAirportId();

        // retrieve a list of flight schedules that connect the base flight schedule to the destinaion airport
        Query query = em.createQuery("SELECT f FROM FlightScheduleEntity f WHERE f.departureDate >= :inMinDepartureDate AND f.departureDate <= :inMaxDepartureDate AND f.flightSchedulePlan.flight.flightRoute.originAirport.airportId =:inOriginAirportId AND f.flightSchedulePlan.flight.flightRoute.destinationAirport.airportId =:inArrivalAirportId");
        query.setParameter("inMinDepartureDate", minDepartureDate);
        query.setParameter("inMaxDepartureDate", maxDepartureDate);
        query.setParameter("inOriginAirportId", originAirportId);
        query.setParameter("inArrivalAirportId", arrivalAirportId);

        List<FlightScheduleEntity> prelimFlightSchedules = (List<FlightScheduleEntity>) query.getResultList();

        // filter the flight schedules to remove any flight schedule that do not have the sufficent number of empty seats
        List<FlightScheduleEntity> flightSchedulesWithSufficientSeats = prelimFlightSchedules.stream().filter(a -> a.getSeatInventory().stream().filter(s -> s.getPassenger() == null).count() >= numberOfPassengers).collect(Collectors.toList());

        if (preferredCabinClass != null) {
            // filter the flight schedules to remove any flight schedule that do not have the desired cabin class
            List<FlightScheduleEntity> flightSchedulesWithSufficientSeatsAndCabinClass = prelimFlightSchedules.stream().filter(a -> a.getFlightSchedulePlan().getFlight().getAircraftConfiguration().getCabinConfigurations().stream().filter(c -> c.getCabinClass() == preferredCabinClass).count() > 0).collect(Collectors.toList());
            return flightSchedulesWithSufficientSeatsAndCabinClass;
        }

        return flightSchedulesWithSufficientSeats;
    }

}
