/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.PartnerEntity;
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
import util.exception.CreateNewPartnerException;
import util.exception.InvalidLoginCredentialsException;
import util.exception.PartnerNotFoundException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class PartnerEntitySessionBean implements PartnerEntitySessionBeanRemote, PartnerEntitySessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public Long createNewPartner(PartnerEntity newPartnerEntity) throws CreateNewPartnerException {

        validate(newPartnerEntity);

        try {
            em.persist(newPartnerEntity);
            em.flush();
            return newPartnerEntity.getUserId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewPartnerException("CreateNewPartnerException: Partner with same username already exists!");
            } else {
                throw new CreateNewPartnerException("CreateNewPartnerException: " + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new CreateNewPartnerException("CreateNewPartnerException: " + ex.getMessage());
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validate(PartnerEntity partnerEntity) throws CreateNewPartnerException {
        javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<PartnerEntity>> errors = validator.validate(partnerEntity);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += error.getPropertyPath() + ": " + error.getInvalidValue() + " - " + error.getMessage() + "\n";
        }
        if (errorMessage.length() > 0) {
            throw new CreateNewPartnerException("CreateNewPartnerException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public List<PartnerEntity> retrieveAllPartners() {
        Query query = em.createQuery("SELECT p FROM PartnerEntity p");
        List<PartnerEntity> partners = (List<PartnerEntity>) query.getResultList();
        return partners;
    }

    @Override
    public PartnerEntity retrievePartnerByUsernamePassword(String userName, String password) throws InvalidLoginCredentialsException, PartnerNotFoundException {
        if (userName.length() == 0 || password.length() == 0) {
            throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Missing credentials!");
        }

        try {
            Query query = em.createQuery("SELECT p FROM PartnerEntity p WHERE p.userName = :inputUserName");
            query.setParameter("inputUserName", userName);
            PartnerEntity parterEntity = (PartnerEntity) query.getSingleResult();
            if (!parterEntity.getPassword().equals(password)) {
                throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Invalid password!");
            }
            return parterEntity;
        } catch (NoResultException ex) {
            throw new PartnerNotFoundException("PartnerNotFoundException: Partner with username " + userName + " does not exist!");
        }
    }
}
