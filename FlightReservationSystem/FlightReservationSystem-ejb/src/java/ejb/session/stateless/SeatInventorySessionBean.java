/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.CabinConfigurationEntity;
import entity.FlightScheduleEntity;
import entity.PassengerEntity;
import entity.SeatEntity;
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

// DO NOT DELETE
//    @Override
//    public SeatEntity retrieveSeatFromFlightSchedule(Long flightScheduleId, CabinClassEnum cabinClassEnum, String seatNumber) throws SeatNotFoundException {
//        Query query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.flightSchedule = :inFlightSchedule AND s.cabinClassEnum = :incabinClassEnum AND s.seatNumber =:inSeatNumber");
//
//        query.setParameter("inFlightSchedule", flightScheduleId);
//        query.setParameter("incabinClassEnum", cabinClassEnum);
//        query.setParameter("inSeatNumber", seatNumber);
//
//        try {
//            SeatEntity seatEntity = (SeatEntity) query.getSingleResult();
//            return seatEntity;
//        } catch (NoResultException ex) {
//            throw new SeatNotFoundException("SeatNotFoundException: Seat with seat number " + seatNumber + " does not exist!");
//        }
//    }

// DO NOT DELETE
//    @Override
//    public void reserveSeatForPassenger(Long flightScheduleId, CabinClassEnum cabinClassEnum, String seatNumber, PassengerEntity passengerEntity) throws SeatNotFoundException, ReserveSeatException {
//        if (passengerEntity == null) {
//            throw new ReserveSeatException("ReserveSeatException: Invalid passenger!");
//        }
//
//        SeatEntity seatEntity = this.retrieveSeatFromFlightSchedule(flightScheduleId, cabinClassEnum, seatNumber);
//
//        if (seatEntity.getPassenger() != null) {
//            throw new ReserveSeatException("ReserveSeatException: Seat with seat number " + seatNumber + " already reserved!");
//        }
//
//        //associate passenger with seat
//        seatEntity.setPassenger(passengerEntity);
//        //associate seat with passenger
//        passengerEntity.getSeats().add(seatEntity);
//
//        em.merge(seatEntity);
//    }

}
