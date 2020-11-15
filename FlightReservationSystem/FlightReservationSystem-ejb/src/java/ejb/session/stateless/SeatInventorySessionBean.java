/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import entity.FareEntity;
import entity.FlightScheduleEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import pojo.SeatInventory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewSeatInventoryException;
import util.exception.FlightScheduleNotFoundException;
import util.exception.SeatNotFoundException;
import util.exception.ReserveSeatException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class SeatInventorySessionBean implements SeatInventorySessionBeanRemote, SeatInventorySessionBeanLocal {

    @EJB
    private FlightSessionBeanLocal flightSessionBeanLocal;

    @EJB
    private FlightScheduleSessionBeanLocal flightScheduleSessionBeanLocal;

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public void createSeatInventoryForFlightSchedule(FlightScheduleEntity flightScheduleEntity, AircraftConfigurationEntity aircraftConfigurationEntity) throws CreateNewSeatInventoryException {

        List<SeatEntity> seatInventory = flightScheduleEntity.getSeatInventory();

        // create seat inventory for each cabin configuration
        for (CabinConfigurationEntity cabinConfigurationEntity : aircraftConfigurationEntity.getCabinConfigurations()) {
            this.addSeatsForCabinConfiguration(seatInventory, cabinConfigurationEntity, flightScheduleEntity);
        }

    }

    private void addSeatsForCabinConfiguration(List<SeatEntity> seatInventory, CabinConfigurationEntity cabinConfigurationEntity, FlightScheduleEntity flightScheduleEntity) throws CreateNewSeatInventoryException {
        long numRows = cabinConfigurationEntity.getNumberOfRows();
        long numSeatAbreast = cabinConfigurationEntity.getNumberOfSeatsAbreast();

        for (int i = 1; i <= numRows; i++) {
            int firstLetter = 65; // ASCII code for 'A'
            for (int j = 0; j < numSeatAbreast; j++) {
                // create new seat and associate with the flight schedule
                String seatNumber = "" + i + ((char) firstLetter);
                SeatEntity seatEntity = new SeatEntity(cabinConfigurationEntity.getCabinClass(), seatNumber, flightScheduleEntity);

                validate(seatEntity);

                seatInventory.add(seatEntity);
                ++firstLetter;
            }
        }
    }

    private void validate(SeatEntity seatEntity) throws CreateNewSeatInventoryException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<SeatEntity>> errors = validator.validate(seatEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewSeatInventoryException("CreateNewSeatInventoryException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public List<SeatEntity> retrieveReservedSeatsByFlightScheduleId(Long flightScheduleId) {
        Query query = em.createQuery("select s from SeatEntity s where s.flightSchedule.flightScheduleId :=inFlightScheduleID AND s.passenger IS NOT null order by s.cabinClassEnum");
        query.setParameter("inFlightScheduleID", flightScheduleId);

        return (List<SeatEntity>) query.getResultList();
    }

    @Override
    public SeatInventory viewSeatsInventoryByFlightScheduleId(Long flightScheduleId) throws FlightScheduleNotFoundException {
        SeatInventory seatInventory = new SeatInventory();

        FlightScheduleEntity flightScheduleEntity = flightScheduleSessionBeanLocal.retrieveFlightScheduleById(flightScheduleId);

        // set total balanced seats of the flight schedule
        seatInventory.setTotalAvailSeats(flightScheduleEntity.getSeatInventory().size());

        // retrieve unique cabins from seat inventory
        Query query = em.createQuery("SELECT DISTINCT s.cabinClassEnum FROM SeatEntity s WHERE s.flightSchedule.flightScheduleId =:inFlightScheduleId");
        query.setParameter("inFlightScheduleId", flightScheduleId);
        List<CabinClassEnum> cabinClasses = (List<CabinClassEnum>) query.getResultList();

        for (CabinClassEnum cabinClass : cabinClasses) {
            // 0 - total avail
            // 1 - reserved
            // 2 - balanced = 0 - 1
            Integer[] arr = seatInventory.getCabinSeatsInventory().get(cabinClass);

            // retrieve all avail seats of the particular cabin class for the particular flight
            query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.cabinClassEnum =:inCabinClass AND s.flightSchedule.flightScheduleId =:inFlightScheduleId");
            query.setParameter("inCabinClass", cabinClass);
            query.setParameter("inFlightScheduleId", flightScheduleId);
            arr[0] = query.getResultList().size();

            // retrieve all seats of the particualr cabin class for the particualr flight where the passenger is not null (i.e. reserved)
            query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.cabinClassEnum =:inCabinClass AND s.flightSchedule.flightScheduleId =:inFlightScheduleId AND s.passenger IS NOT NULL");
            query.setParameter("inCabinClass", cabinClass);
            query.setParameter("inFlightScheduleId", flightScheduleId);
            arr[1] = query.getResultList().size();

            // update total reserved seats
            seatInventory.setTotalReservedSeats(seatInventory.getTotalReservedSeats() + arr[1]);

            // set balanced seats as total - reserved
            arr[2] = arr[0] - arr[1];
        }

        // cal total balanced seats
        seatInventory.setTotalBalancedSeats(seatInventory.getTotalAvailSeats() - seatInventory.getTotalReservedSeats());

        return seatInventory;
    }

    @Override
    public SeatEntity retrieveAvailableSeatFromFlightScheduleAndCabin(Long flightScheduleId, CabinClassEnum cabinClassEnum, String seatNumber) throws SeatNotFoundException, ReserveSeatException {
        Query query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.flightSchedule.flightScheduleId = :inFlightScheduleId AND s.cabinClassEnum = :incabinClassEnum AND s.seatNumber =:inSeatNumber");

        query.setParameter("inFlightScheduleId", flightScheduleId);
        query.setParameter("incabinClassEnum", cabinClassEnum);
        query.setParameter("inSeatNumber", seatNumber);

        try {
            SeatEntity seatEntity = (SeatEntity) query.getSingleResult();

            if (seatEntity.getPassenger() != null) {
                throw new ReserveSeatException("ReserveSeatException: Seat with seat number " + seatNumber + " already reserved!");
            }

            return seatEntity;
        } catch (NoResultException ex) {
            throw new SeatNotFoundException("SeatNotFoundException: Seat with seat number " + seatNumber + " does not exist!");
        }
    }

    @Override
    public List<SeatEntity> retrieveAllAvailableSeatsFromFlightScheduleAndCabin(Long flightScheduleId, CabinClassEnum cabinClassEnum) {
        if (cabinClassEnum == null) {
            Query query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.flightSchedule.flightScheduleId = :inFlightScheduleId AND s.passenger IS NULL");

            query.setParameter("inFlightScheduleId", flightScheduleId);
            return (List<SeatEntity>) query.getResultList();

        } else {
            Query query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.flightSchedule.flightScheduleId = :inFlightScheduleId AND s.cabinClassEnum = :incabinClassEnum AND s.passenger IS NULL");

            query.setParameter("inFlightScheduleId", flightScheduleId);
            query.setParameter("incabinClassEnum", cabinClassEnum);
            return (List<SeatEntity>) query.getResultList();
        }
    }

    @Override // local only
    // assign cheapest fare
    public void reserveSeatsForCustomer(PassengerEntity passenger) throws ReserveSeatException {
        try {
            if (passenger == null) {
                throw new ReserveSeatException("ReserveSeatException: Invalid passenger!");
            }

            // track total fare amount
            BigDecimal fareAmount = BigDecimal.ZERO;
            // track seats that the customer have booked to prevent duplicated seat booking for same passenger in same flight schedule
            HashSet<String> fareBasisCodes = new HashSet<>();

            for (SeatEntity seat : passenger.getSeats()) {
//            SeatEntity managedSeat = em.find(SeatEntity.class, seat.getSeatId());

                if (seat.getPassenger() != null) {
                    throw new ReserveSeatException("ReserveSeatException: Seat with seat number " + seat.getSeatNumber() + " already reserved!");
                }

                // retrieve cheapest fare associated with the flight schedule and the cabin class of the seat
                List<FareEntity> fares = seat.getFlightSchedule().getFlightSchedulePlan().getFares();
                FareEntity cheapestFare = fares.get(0);
                for (FareEntity fare : fares) {
                    if (fare.getCabinClass().equals(seat.getCabinClassEnum()) && fare.getFareAmount().compareTo(cheapestFare.getFareAmount()) < 0) {
                        cheapestFare = fare;
                    }
                }

                if (fareBasisCodes.contains(cheapestFare.getFareBasisCode())) {
                    throw new ReserveSeatException("ReserveSeatException: Passenger has multiple seat reservations within the same flight schedule!");
                }

                //em.persist(seat);
                seat.setFareBasisCode(cheapestFare.getFareBasisCode());
                seat.setPassenger(passenger);
                fareAmount = fareAmount.add(cheapestFare.getFareAmount());
            }
            // update total fare amount
            passenger.setFareAmount(fareAmount);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override // local only
    // assign most expensive fare
    public void reserveSeatsForPartner(PassengerEntity passenger) throws ReserveSeatException {
        if (passenger == null) {
            throw new ReserveSeatException("ReserveSeatException: Invalid passenger!");
        }

        // track total fare amount
        BigDecimal fareAmount = BigDecimal.ZERO;
        // track seats that the customer have booked to prevent duplicated seat booking for same passenger in same flight schedule
        HashSet<String> fareBasisCodes = new HashSet<>();

        for (SeatEntity seat : passenger.getSeats()) {
            if (seat.getPassenger() != null) {
                throw new ReserveSeatException("ReserveSeatException: Seat with seat number " + seat.getSeatNumber() + " already reserved!");
            }

            // retrieve most expensive fare associated with the flight schedule and the cabin class of the seat
            List<FareEntity> fares = seat.getFlightSchedule().getFlightSchedulePlan().getFares();
            FareEntity expensiveFare = fares.get(0);
            for (FareEntity fare : fares) {
                if (fare.getCabinClass().equals(seat.getCabinClassEnum()) && fare.getFareAmount().compareTo(expensiveFare.getFareAmount()) > 0) {
                    expensiveFare = fare;
                }
            }

            if (fareBasisCodes.contains(expensiveFare.getFareBasisCode())) {
                throw new ReserveSeatException("ReserveSeatException: Passenger has multiple seat reservations within the same flight schedule!");
            }

            em.merge(seat);
            seat.setFareBasisCode(expensiveFare.getFareBasisCode());
            seat.setPassenger(passenger);
            fareAmount = fareAmount.add(expensiveFare.getFareAmount());
        }
        // update total fare amount
        passenger.setFareAmount(fareAmount);
    }

}
