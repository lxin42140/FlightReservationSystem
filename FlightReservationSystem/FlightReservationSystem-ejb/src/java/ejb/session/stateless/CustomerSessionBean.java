/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CustomerEntity;
import java.util.Set;
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
import util.exception.CreateNewCustomerException;
import util.exception.CustomerNotFoundException;
import util.exception.InvalidLoginCredentialsException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class CustomerSessionBean implements CustomerSessionBeanRemote, CustomerSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public Long registerNewCustomer(CustomerEntity customer) throws CreateNewCustomerException {
        validate(customer);

        try {
            em.persist(customer);
            em.flush();
            return customer.getUserId();
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewCustomerException("CreateNewCustomerException: Customer with same username/email/mobile phone number already exists!");
            } else {
                throw new CreateNewCustomerException("CreateNewCustomerException: " + ex.getMessage());
            }
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validate(CustomerEntity customer) throws CreateNewCustomerException {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<CustomerEntity>> errors = validator.validate(customer);

        String errorMessage = "";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {
            throw new CreateNewCustomerException("CreateNewCustomerException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public CustomerEntity retrieveCustomerByUsernameAndPassword(String username, String password) throws CustomerNotFoundException, InvalidLoginCredentialsException {
        Query query = em.createQuery("SELECT c from CustomerEntity c WHERE c.userName =:inUsername");
        query.setParameter("inUsername", username);

        try {
            CustomerEntity customerEntity = (CustomerEntity) query.getSingleResult();
            if (!customerEntity.getPassword().equals(password)) {
                throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Invalid password");
            }
            return customerEntity;
        } catch (NoResultException ex) {
            throw new CustomerNotFoundException("CustomerNotFoundException: Customer with username " + username + " does not exist!");
        }
    }

}
