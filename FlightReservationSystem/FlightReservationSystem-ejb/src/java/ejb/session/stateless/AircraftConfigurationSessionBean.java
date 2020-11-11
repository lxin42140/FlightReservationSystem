/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AircraftConfigurationEntity;
import entity.AircraftTypeEntity;
import entity.CabinConfigurationEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import util.enumeration.CabinClassEnum;
import util.exception.AircraftConfigurationNotFoundException;
import util.exception.AircraftTypeNotFoundException;
import util.exception.CreateNewAircraftConfigurationException;
import util.exception.CreateNewCabinConfigurationException;

/**
 *
 * @author kiyon
 */
@Stateless
public class AircraftConfigurationSessionBean implements AircraftConfigurationSessionBeanRemote, AircraftConfigurationSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @EJB
    private CabinConfigurationEntitySessionBeanLocal cabinConfigurationEntitySessionBeanLocal;

    @EJB
    private AircraftTypeSessionBeanLocal aircraftTypeSessionBeanLocal;

    @Override
    public Long createNewAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration, List<CabinConfigurationEntity> cabinConfigurations, Long aircraftTypeId) throws CreateNewAircraftConfigurationException, AircraftTypeNotFoundException {
        validateNumberOfCabinConfigurations(cabinConfigurations);

        AircraftTypeEntity aircraftType = aircraftTypeSessionBeanLocal.retrieveAircraftTypeById(aircraftTypeId);

        // set unidirectional relationship
        aircraftConfiguration.setAircraftType(aircraftType);

        for (CabinConfigurationEntity cabinConfiguration : cabinConfigurations) {
            try {
                cabinConfigurationEntitySessionBeanLocal.createNewCabinConfigurationForAircraftConfiguration(cabinConfiguration, aircraftConfiguration);
            } catch (CreateNewCabinConfigurationException ex) {
                throw new CreateNewAircraftConfigurationException(ex.getMessage());
            }
        }

        validateAircraftConfiguration(aircraftConfiguration);

        try {
            em.persist(aircraftConfiguration);
            em.flush();
            return aircraftConfiguration.getAircraftConfigurationId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Aircraft configuration with same name already exists!");
            } else {
                throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: " + ex.getMessage());
            }
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validateNumberOfCabinConfigurations(List<CabinConfigurationEntity> cabinConfigurations) throws CreateNewAircraftConfigurationException {
        if (cabinConfigurations.isEmpty()) {
            throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Please provide at least one cabin configuration!");
        } else if (cabinConfigurations.size() > 4) {
            throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Please provide at most 4 cabin configuration!");
        }
        // check that no duplicated cabin classes are created
        HashSet<CabinClassEnum> cabinclasses = new HashSet<>();
        for (CabinConfigurationEntity cabinConfiguration : cabinConfigurations) {
            if (cabinclasses.contains(cabinConfiguration.getCabinClass())) {
                throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Duplicated cabin class " + cabinConfiguration.getCabinClass() + " !");
            }
        }
    }

    private void validateAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration) throws CreateNewAircraftConfigurationException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<AircraftConfigurationEntity>> errors = validator.validate(aircraftConfiguration);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewAircraftConfigurationException("CreateNewAircraftConfigurationException: Invalid inputs!\n" + errorMessage);
        }
    }

    public List<AircraftConfigurationEntity> retrieveAllAircraftConfiguration() {
        Query query = em.createQuery("SELECT a FROM AircraftConfigurationEntity a ORDER BY a.aircraftType, a.aircraftConfigurationName");

        List<AircraftConfigurationEntity> aircraftConfigurations = (List<AircraftConfigurationEntity>) query.getResultList();

        return aircraftConfigurations;
    }

    public AircraftConfigurationEntity retrieveAircraftConfigurationById(Long aircraftConfigurationId) throws AircraftConfigurationNotFoundException {
        if (aircraftConfigurationId == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: Invalid aircraft configuration ID!");
        }

        AircraftConfigurationEntity aircraftConfigurationEntity = em.find(AircraftConfigurationEntity.class, aircraftConfigurationId);

        if (aircraftConfigurationEntity == null) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: aircraft configuration with ID " + aircraftConfigurationId + " does not exist!");
        }

        //aircraftConfigurationEntity.getCabinConfigurations().size();
        return aircraftConfigurationEntity;
    }

    @Override
    public AircraftConfigurationEntity retrieveAircraftConfigurationByName(String aircraftConfigName) throws AircraftConfigurationNotFoundException {
        Query query = em.createQuery("SELECT a FROM AircraftConfigurationEntity a WHERE a.aircraftConfigurationName :=inAircraftConfigName");
        query.setParameter("inAircraftConfigName", aircraftConfigName);

        try {
            return (AircraftConfigurationEntity) query.getSingleResult();
        } catch (NoResultException ex) {
            throw new AircraftConfigurationNotFoundException("AircraftConfigurationNotFoundException: aircraft configuration with name " + aircraftConfigName + " does not exist!");
        }
    }
}
