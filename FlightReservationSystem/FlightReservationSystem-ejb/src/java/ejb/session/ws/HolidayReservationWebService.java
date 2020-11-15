///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ejb.session.ws;
//
//import ejb.session.stateless.FlightSearchSessionBeanLocal;
//import ejb.session.stateless.PartnerEntitySessionBeanLocal;
//import ejb.session.stateless.SeatInventorySessionBeanLocal;
//import entity.FlightScheduleEntity;
//import entity.PartnerEntity;
//import entity.SeatEntity;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import javax.ejb.EJB;
//import javax.jws.WebService;
//import javax.jws.WebMethod;
//import javax.jws.WebParam;
//import javax.ejb.Stateless;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import pojo.SeatInventory;
//import util.enumeration.CabinClassEnum;
//import util.exception.FlightScheduleNotFoundException;
//import util.exception.InvalidLoginCredentialsException;
//import util.exception.NoMatchingFlightsException;
//import util.exception.PartnerNotFoundException;
//import util.exception.SearchFlightFailedException;
//
///**
// *
// * @author Li Xin
// */
//@WebService(serviceName = "HolidayReservationWebService")
//@Stateless()
//public class HolidayReservationWebService {
//
//    @EJB(name = "SeatInventorySessionBeanLocal")
//    private SeatInventorySessionBeanLocal seatInventorySessionBeanLocal;
//
//    @EJB(name = "FlightSearchSessionBeanLocal")
//    private FlightSearchSessionBeanLocal flightSearchSessionBeanLocal;
//
//    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
//    private EntityManager em;
//
//    @EJB(name = "PartnerEntitySessionBeanLocal")
//    private PartnerEntitySessionBeanLocal partnerEntitySessionBeanLocal;
//
//    /**
//     * This is a sample web service operation
//     */
//    @WebMethod(operationName = "doLogin")
//    public PartnerEntity doLogin(@WebParam(name = "username") String username,
//            @WebParam(name = "password") String password
//    ) throws InvalidLoginCredentialsException, PartnerNotFoundException {
//        return this.partnerEntitySessionBeanLocal.retrievePartnerByUsernamePassword(username, password);
//    }
//
//    ////flightSearchSessionBeanRemote.searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabinClass);
//    @WebMethod(operationName = "searchOneWayFlights")
//    public HashMap<Integer, List<FlightScheduleEntity>> searchOneWayFlights(@WebParam(name = "departureAirportId") Long departureAirportId,
//            @WebParam(name = "arrivalAirportId") Long arrivalAirportId,
//            @WebParam(name = "departureDate") Date departureDate,
//            @WebParam(name = "numberOfPassengers") Integer numberOfPassengers,
//            @WebParam(name = "preferDirectFlight") Boolean preferDirectFlight,
//            @WebParam(name = "preferredCabinClass") String preferredCabinClass
//    ) throws NoMatchingFlightsException, SearchFlightFailedException {
//        CabinClassEnum preferredCabin = null;
//        if (preferredCabinClass != null) {
//            preferredCabin = CabinClassEnum.valueOf(preferredCabinClass);
//        }
//        return flightSearchSessionBeanLocal.searchOneWayFlights(departureAirportId, arrivalAirportId, departureDate, numberOfPassengers, preferDirectFlight, preferredCabin);
//    }
//
////searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabinClass).get(0);    @WebMethod(operationName = "searchTwoWaysFlights")
//    @WebMethod(operationName = "searchTwoWaysFlights")
//    public List<HashMap<Integer, List<FlightScheduleEntity>>> searchTwoWaysFlights(
//            @WebParam(name = "departureAirportId") Long departureAirportId,
//            @WebParam(name = "arrivalAirportId") Long arrivalAirportId,
//            @WebParam(name = "departureDate") Date departureDate,
//            @WebParam(name = "returnDate") Date returnDate,
//            @WebParam(name = "numberOfPassengers") Integer numberOfPassengers,
//            @WebParam(name = "preferDirectFlight") Boolean preferDirectFlight,
//            @WebParam(name = "preferredCabinClass") String preferredCabinClass
//    ) throws NoMatchingFlightsException, SearchFlightFailedException {
//        CabinClassEnum preferredCabin = null;
//        if (preferredCabinClass != null) {
//            preferredCabin = CabinClassEnum.valueOf(preferredCabinClass);
//        }
//
//        return this.flightSearchSessionBeanLocal.searchTwoWaysFlights(departureAirportId, arrivalAirportId, departureDate, returnDate, numberOfPassengers, preferDirectFlight, preferredCabin);
//    }
//
////        public SeatInventory viewSeatsInventoryByFlightScheduleId(Long flightScheduleId) throws FlightScheduleNotFoundException {
//    @WebMethod(operationName = "viewSeatsInventoryByFlightScheduleId")
//    public SeatInventory viewSeatsInventoryByFlightScheduleId(
//            @WebParam(name = "flightScheduleId") Long flightScheduleId
//    ) throws FlightScheduleNotFoundException {
//        return this.seatInventorySessionBeanLocal.viewSeatsInventoryByFlightScheduleId(flightScheduleId);
//    }
//
//    //retrieveAllAvailableSeatsFromFlightScheduleAndCabin
//    @WebMethod(operationName = "retrieveAllAvailableSeatsFromFlightScheduleAndCabin")
//    public List<SeatEntity> retrieveAllAvailableSeatsFromFlightScheduleAndCabin(
//            @WebParam(name = "flightScheduleId") Long flightScheduleId,
//            @WebParam(name = "cabinClass") String cabinClass
//    ) throws FlightScheduleNotFoundException {
//        CabinClassEnum preferredCabin = CabinClassEnum.valueOf(cabinClass);
//        List<SeatEntity> availSeats = this.seatInventorySessionBeanLocal.retrieveAllAvailableSeatsFromFlightScheduleAndCabin(flightScheduleId, preferredCabin);
//        for (SeatEntity seat : availSeats) {
//            em.detach(seat);
//            seat.setFlightSchedule(null);
//        }
//        return availSeats;
//    }
//
//}
