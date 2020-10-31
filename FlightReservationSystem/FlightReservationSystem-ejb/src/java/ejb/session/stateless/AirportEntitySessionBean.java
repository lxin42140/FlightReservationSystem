/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AirportEntity;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewAirportException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class AirportEntitySessionBean implements AirportEntitySessionBeanRemote, AirportEntitySessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public Long createNewAirport(AirportEntity newAirportEntity) throws CreateNewAirportException {

        validate(newAirportEntity);

        try {
            em.persist(newAirportEntity);
            em.flush();
            return newAirportEntity.getAirportId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewAirportException("CreateNewAirportException: Airport with same name or IATA code already exists!");
            } else {
                throw new CreateNewAirportException("CreateNewAirportException: " + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new CreateNewAirportException("CreateNewAirportException: " + ex.getMessage());
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validate(AirportEntity airportEntity) throws CreateNewAirportException {
        javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<AirportEntity>> errors = validator.validate(airportEntity);

        String errorMessage = "Input data validation error!\n";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }
        if (errorMessage.length() > 0) {

            throw new CreateNewAirportException("CreateNewAirportException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public List<AirportEntity> retrieveAllAirports() {
        Query query = em.createQuery("SELECT a FROM AirportEntity a");
        List<AirportEntity> airports = (List<AirportEntity>) query.getResultList();
        return airports;
    }

    @Override
    public AirportEntity retrieveAirportByid(Long airportId) throws AirportNotFoundException {
        if (airportId == null) {
            throw new AirportNotFoundException("AirportNotFoundException: Invalid airport ID!");
        }

        AirportEntity airport = em.find(AirportEntity.class, airportId);
        if (airport == null) {
            throw new AirportNotFoundException("AirportNotFoundException: Airport with ID " + airportId + " does not exist!");
        }
        return airport;
    }

    @Override
    public AirportEntity retrieveAirportByName(String airportName) throws AirportNotFoundException {
        if (airportName.length() == 0) {
            throw new AirportNotFoundException("AirportNotFoundException: Invalid airport name!");
        }

        try {
            Query query = em.createQuery("SELECT a FROM AirportEntity a WHERE a.airportName = :inputName");
            query.setParameter("inputName", airportName);
            AirportEntity airport = (AirportEntity) query.getSingleResult();
            return airport;
        } catch (NoResultException ex) {
            throw new AirportNotFoundException("AirportNotFoundException: Airport with name " + airportName + " does not exist!");
        }
    }

    @Override
    public AirportEntity retrieveAirportByIataCode(String iataCode) throws AirportNotFoundException {
        if (iataCode.length() == 0) {
            throw new AirportNotFoundException("AirportNotFoundException: Invalid IATA code!");
        }

        try {
            Query query = em.createQuery("SELECT a FROM AirportEntity a WHERE a.iataAirlineCode = :inputCode");
            query.setParameter("inputCode", iataCode);
            AirportEntity airport = (AirportEntity) query.getSingleResult();
            return airport;
        } catch (NoResultException ex) {
            throw new AirportNotFoundException("AirportNotFoundException: Airport with IATA code " + iataCode + " does not exist!");
        }
    }

}
