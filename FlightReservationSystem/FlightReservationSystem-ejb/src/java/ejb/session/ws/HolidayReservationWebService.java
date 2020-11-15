/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import ejb.session.stateless.AirportEntitySessionBeanLocal;
import ejb.session.stateless.FlightReservationSessionBeanLocal;
import ejb.session.stateless.FlightScheduleSessionBeanLocal;
import ejb.session.stateless.FlightSearchSessionBeanLocal;
import ejb.session.stateless.PartnerEntitySessionBeanLocal;
import ejb.session.stateless.SeatInventorySessionBeanLocal;
import entity.AirportEntity;
import entity.CreditCardEntity;
import entity.FareEntity;
import entity.FlightReservationEntity;
import entity.FlightScheduleEntity;
import entity.PartnerEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewFlightReservationException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.InvalidLoginCredentialsException;
import util.exception.NoMatchingFlightsException;
import util.exception.PartnerNotFoundException;
import util.exception.SearchFlightFailedException;
import util.exception.SeatNotFoundException;

/**
 *
 * @author Li Xin
 */
@WebService(serviceName = "HolidayReservationWebService")
@Stateless()
public class HolidayReservationWebService {

    @EJB(name = "FlightScheduleSessionBeanLocal")
    private FlightScheduleSessionBeanLocal flightScheduleSessionBeanLocal;

    @EJB(name = "FlightReservationSessionBeanLocal")
    private FlightReservationSessionBeanLocal flightReservationSessionBeanLocal;

    @EJB(name = "AirportEntitySessionBeanLocal")
    private AirportEntitySessionBeanLocal airportEntitySessionBeanLocal;

    @EJB(name = "SeatInventorySessionBeanLocal")
    private SeatInventorySessionBeanLocal seatInventorySessionBeanLocal;

    @EJB(name = "FlightSearchSessionBeanLocal")
    private FlightSearchSessionBeanLocal flightSearchSessionBeanLocal;

    @EJB(name = "PartnerEntitySessionBeanLocal")
    private PartnerEntitySessionBeanLocal partnerEntitySessionBeanLocal;

    @WebMethod(operationName = "login")
    public Boolean login(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password
    ) {
        try {
            this.partnerEntitySessionBeanLocal.retrievePartnerByUsernamePassword(username, password);
            return true;
        } catch (InvalidLoginCredentialsException | PartnerNotFoundException ex) {
            return false;
        }
    }

    @WebMethod(operationName = "retrieveAllAirports")
    public List<RemoteAirport> retrieveAllAirports() {
        List<AirportEntity> airports = airportEntitySessionBeanLocal.retrieveAllAirports();
        List<RemoteAirport> remoteAirports = new ArrayList<>();
        for (AirportEntity airport : airports) {
            remoteAirports.add(new RemoteAirport(airport.getAirportId(), airport.getIataAirlineCode()));
        }
        return remoteAirports;
    }

    private List<RemoteFlightSchedule> flattenSearch(HashMap<Integer, List<FlightScheduleEntity>> flights) {
        Set<Map.Entry<Integer, List<FlightScheduleEntity>>> set = flights.entrySet();

        List<RemoteFlightSchedule> result = new ArrayList<>();

        for (Map.Entry<Integer, List<FlightScheduleEntity>> entry : set) {

            ArrayList<FlightScheduleEntity> flightSchedules = (ArrayList) entry.getValue();

            for (int i = 0; i < flightSchedules.size(); i++) {
                FlightScheduleEntity flightSchedule = flightSchedules.get(i);
                result.add(new RemoteFlightSchedule(flightSchedule.getFlightScheduleId(), flightSchedule.getDepartureDate(), flightSchedule.getFlightSchedulePlan().getFlight().getFlightNumber(), flightSchedule.getArrivalDateTime(), entry.getKey()));
            }
        }

        return result;
    }

    ////flightSearchSessionBeanRemote.searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabinClass);
    @WebMethod(operationName = "searchOneWayFlights")
    public List<RemoteFlightSchedule> searchOneWayFlights(@WebParam(name = "departureAirportId") Long departureAirportId,
            @WebParam(name = "arrivalAirportId") Long arrivalAirportId,
            @WebParam(name = "departureDate") Date departureDate,
            @WebParam(name = "numberOfPassengers") Integer numberOfPassengers,
            @WebParam(name = "preferDirectFlight") Boolean preferDirectFlight,
            @WebParam(name = "preferredCabinClass") String preferredCabinClass
    ) throws NoMatchingFlightsException, SearchFlightFailedException {
        CabinClassEnum preferredCabin = null;
        if (preferredCabinClass != null) {
            preferredCabin = CabinClassEnum.valueOf(preferredCabinClass);
        }
        HashMap<Integer, List<FlightScheduleEntity>> oneWayFlights = flightSearchSessionBeanLocal.searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabin);
//        SearchResultWrapper wrapper = new SearchResultWrapper();
//        wrapper.setOneWayResults(flattenSearch(oneWayFlights));

        return flattenSearch(oneWayFlights);
    }

//searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass).get(0);    @WebMethod(operationName = "searchTwoWaysFlights")
    @WebMethod(operationName = "searchTwoWaysFlights")
    public List<RemoteFlightSchedule> searchTwoWaysFlights(
            @WebParam(name = "departureAirportId") Long departureAirportId,
            @WebParam(name = "arrivalAirportId") Long arrivalAirportId,
            @WebParam(name = "departureDate") Date departureDate,
            @WebParam(name = "returnDate") Date returnDate,
            @WebParam(name = "numberOfPassengers") Integer numberOfPassengers,
            @WebParam(name = "preferDirectFlight") Boolean preferDirectFlight,
            @WebParam(name = "preferredCabinClass") String preferredCabinClass
    ) throws NoMatchingFlightsException, SearchFlightFailedException {
        CabinClassEnum preferredCabin = null;
        if (preferredCabinClass != null) {
            preferredCabin = CabinClassEnum.valueOf(preferredCabinClass);
        }

        List<HashMap<Integer, List<FlightScheduleEntity>>> searchTwoWaysFlights = this.flightSearchSessionBeanLocal.searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabin);
        HashMap<Integer, List<FlightScheduleEntity>> toFlights = searchTwoWaysFlights.get(0);
        HashMap<Integer, List<FlightScheduleEntity>> returnFlights = searchTwoWaysFlights.get(1);

        List<RemoteFlightSchedule> flattenToFlights = flattenSearch(toFlights);
        List<RemoteFlightSchedule> flattenReturnFlights = flattenSearch(returnFlights);

        List<RemoteFlightSchedule> results = new ArrayList<>();
        results.addAll(flattenToFlights);
        results.addAll(flattenReturnFlights);

        return results;
    }

    @WebMethod(operationName = "reserveFlight")
    public Long reserveFlight(
            @WebParam(name = "toFlightSchedules") List<Long> toFlightSchedules,
            @WebParam(name = "returnFlightSchedules") List<Long> returnFlightSchedules,
            @WebParam(name = "passengers") List<String> newPassengers,
            @WebParam(name = "seats") List<String> selectedSeats,
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password,
            @WebParam(name = "creditCard") List<String> creditCard
    ) throws CreateNewFlightReservationException {
        try {
            // get partner
            PartnerEntity parter = this.partnerEntitySessionBeanLocal.retrievePartnerByUsernamePassword(username, password);

            // add flight schedules fom id
            List<FlightScheduleEntity> flightSchedules = new ArrayList<>();
            for (Long id : toFlightSchedules) {
                flightSchedules.add(flightScheduleSessionBeanLocal.retrieveFlightScheduleById(id));
            }
            for (Long id : returnFlightSchedules) {
                flightSchedules.add(flightScheduleSessionBeanLocal.retrieveFlightScheduleById(id));
            }

            List<PassengerEntity> passengers = new ArrayList<>();
            for (String passengerDetails : newPassengers) {
                String[] passenger = passengerDetails.split(" ");
                PassengerEntity newPassenger = new PassengerEntity(passenger[0], passenger[1], passenger[2]);
                passengers.add(newPassenger);
            }

            for (PassengerEntity passenger : passengers) {
                List<SeatEntity> seats = new ArrayList<>();
                for (String seat : selectedSeats) {
                    String[] seatDetails = seat.split(" ");
                    String passportNumber = seatDetails[0];

                    if (passenger.getPassportNumber().equals(passportNumber)) {
                        Long seatId = Long.parseLong(seatDetails[1]);
                        seats.add(seatInventorySessionBeanLocal.retrieveSeatById(seatId));
                    }
                }
                passenger.getSeats().addAll(seats);
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM");
//            CreditCardEntity creditCardEntity = new CreditCardEntity(creditCard.getCardNumber(), creditCard.getFirstName(), creditCard.getLastName(), creditCard.getDateOfExpiry(), creditCard.getCvc());
            CreditCardEntity creditCardEntity = new CreditCardEntity(creditCard.get(0), creditCard.get(1), creditCard.get(2), format.parse(creditCard.get(3)), creditCard.get(4));

            return this.flightReservationSessionBeanLocal.createNewFlightReservation(flightSchedules, passengers, creditCardEntity, parter);
        } catch (CreateNewFlightReservationException | FlightScheduleNotFoundException | InvalidLoginCredentialsException | ParseException | PartnerNotFoundException | SeatNotFoundException ex) {
            throw new CreateNewFlightReservationException(ex.getMessage());
        }
    }

    //retrieveAllAvailableSeatsFromFlightScheduleAndCabin
    @WebMethod(operationName = "retrieveAllAvailableSeatsFromFlightScheduleAndCabin")
    public List<RemoteSeat> retrieveAllAvailableSeatsFromFlightScheduleAndCabin(
            @WebParam(name = "flightScheduleId") Long flightScheduleId,
            @WebParam(name = "cabinClass") String cabinClass
    ) throws FlightScheduleNotFoundException {
        CabinClassEnum preferredCabin = CabinClassEnum.valueOf(cabinClass);
        List<SeatEntity> availSeats = this.seatInventorySessionBeanLocal.retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flightScheduleId, preferredCabin);
        List<RemoteSeat> seats = new ArrayList<>();
        for (SeatEntity seat : availSeats) {
            seats.add(new RemoteSeat(seat.getSeatId(), seat.getCabinClassEnum().toString(), seat.getSeatNumber()));
        }
        return seats;
    }

    @WebMethod(operationName = "retrieveAllFaresForFlightSchedule")
    public List<RemoteFare> retrieveAllFaresForFlightSchedule(
            @WebParam(name = "flightScheduleId") Long flightScheduleId
    ) throws FlightScheduleNotFoundException {
        FlightScheduleEntity flight = flightScheduleSessionBeanLocal.retrieveFlightScheduleById(flightScheduleId);
        List<FareEntity> fares = flight.getFlightSchedulePlan().getFares();
        List<RemoteFare> remoteFares = new ArrayList<>();
        for (FareEntity fare : fares) {
            remoteFares.add(new RemoteFare(fare.getFareId(), fare.getFareBasisCode(), fare.getFareAmount().doubleValue()));
        }
        return remoteFares;
    }

    @WebMethod(operationName = "retrieveAllReservations")
    public List<RemoteReservation> retrieveAllReservations(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) throws InvalidLoginCredentialsException, PartnerNotFoundException {
        PartnerEntity parter = this.partnerEntitySessionBeanLocal.retrievePartnerByUsernamePassword(username, password);

        List<FlightReservationEntity> reservations = flightReservationSessionBeanLocal.viewFlightReservationByUser(parter.getUserId());
        List<RemoteReservation> remotes = new ArrayList<>();
        for (FlightReservationEntity flightReservation : reservations) {
            remotes.add(new RemoteReservation(flightReservation.getFlightReservationId(), flightReservation.getTotalAmount().doubleValue()));
        }

        return remotes;
    }

    @WebMethod(operationName = "retrieveReservations")
    public RemoteReservationDetails retrieveReservations(
            @WebParam(name = "reservationId") Long reservationId
    ) {
        FlightReservationEntity reservation = this.flightReservationSessionBeanLocal.viewFlightReservationByFlightReservationId(reservationId);
        RemoteReservationDetails remoteReservationDetails = new RemoteReservationDetails();

        List<FlightScheduleEntity> flightSchedules = reservation.getFlightSchedules();
        for (FlightScheduleEntity flightSchedule : flightSchedules) {
            remoteReservationDetails.getItinery().add(new RemoteFlightSchedule(flightSchedule.getFlightScheduleId(), flightSchedule.getDepartureDate(), flightSchedule.getFlightSchedulePlan().getFlight().getFlightNumber(), flightSchedule.getArrivalDateTime()));
        }

        List<PassengerEntity> passengers = reservation.getPassengers();
        for (PassengerEntity passenger : passengers) {
            remoteReservationDetails.getPassengers().add(new RemotePassenger(passenger.getPassengerId(), passenger.getFirstName(), passenger.getLastName(), passenger.getPassportNumber()));
        }

        List<RemoteSeat> seats = new ArrayList<>();
        for (PassengerEntity passenger : passengers) {
            for (SeatEntity seat : passenger.getSeats()) {
                RemoteSeat remoteSeat = new RemoteSeat();
                remoteSeat.setCabinClass(seat.getCabinClassEnum().toString());
                remoteSeat.setSeatID(seat.getSeatId());
                remoteSeat.setSeatNumber(seat.getSeatNumber());
                remoteSeat.setPassengerId(seat.getPassenger().getPassengerId());
            }
        }

        remoteReservationDetails.setTotalAmount(reservation.getTotalAmount().doubleValue());

        return remoteReservationDetails;
    }

}
