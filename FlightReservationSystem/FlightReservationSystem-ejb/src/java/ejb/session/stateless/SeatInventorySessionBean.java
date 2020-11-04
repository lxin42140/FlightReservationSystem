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
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewSeatInventoryException;
import util.exception.SeatNotFoundException;
import util.exception.ReserveSeatException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class SeatInventorySessionBean implements SeatInventorySessionBeanRemote, SeatInventorySessionBeanLocal {

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
                SeatEntity seatEntity = new SeatEntity(cabinConfigurationEntity.getCabinClass(), i, (char) firstLetter, flightScheduleEntity);

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
    public SeatEntity retrieveSeatFromFlightSchedule(Long flightScheduleId, CabinClassEnum cabinClassEnum, Long rowNumber, Character rowLetter) throws SeatNotFoundException {
        Query query = em.createQuery("SELECT s FROM SeatEntity s WHERE s.flightSchedule = :inFlightSchedule AND s.cabinClassEnum = :incabinClassEnum AND s.seatRowNumber = :inRowNumber AND s.seatRowLetter = :inRowLetter");

        query.setParameter("inFlightSchedule", flightScheduleId);
        query.setParameter("incabinClassEnum", cabinClassEnum);
        query.setParameter("inRowNumber", rowNumber);
        query.setParameter("inRowLetter", rowLetter);

        try {
            SeatEntity seatEntity = (SeatEntity) query.getSingleResult();
            return seatEntity;
        } catch (NoResultException ex) {
            throw new SeatNotFoundException("SeatNotFoundException: Seat with seat number " + rowNumber + rowLetter + " does not exist!");
        }
    }

    @Override
    public void reserveSeatForPassenger(Long flightScheduleId, CabinClassEnum cabinClassEnum, Long rowNumber, Character rowLetter, PassengerEntity passengerEntity) throws SeatNotFoundException, ReserveSeatException {
        if (passengerEntity == null) {
            throw new ReserveSeatException("ReserveSeatException: Invalid passenger!");
        }

        SeatEntity seatEntity = this.retrieveSeatFromFlightSchedule(flightScheduleId, cabinClassEnum, rowNumber, rowLetter);

        if (seatEntity.getPassenger() != null) {
            throw new ReserveSeatException("ReserveSeatException: Seat with seat number " + rowNumber + rowLetter + " already reserved!");
        }

        //associate passenger with seat
        seatEntity.setPassenger(passengerEntity);
        //associate seat with passenger
        passengerEntity.getSeats().add(seatEntity);

        em.merge(seatEntity);
    }

}
