/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.EmployeeEntity;
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
import util.exception.CreateNewEmployeeException;
import util.exception.EmployeeNotFoundException;
import util.exception.InvalidInputException;
import util.exception.InvalidLoginCredentialsException;

/**
 *
 * @author Li Xin
 */
@Stateless
public class EmployeeEntitySessionBean implements EmployeeEntitySessionBeanRemote, EmployeeEntitySessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public void createNewEmployee(EmployeeEntity newEmployeeEntity) throws CreateNewEmployeeException {

        validate(newEmployeeEntity);

        try {
            em.persist(newEmployeeEntity);
        } catch (PersistenceException ex) {
            if (isSQLIntegrityConstraintViolationException(ex)) {
                throw new CreateNewEmployeeException("CreateNewEmployeeException: Employee with same username already exists!");
            } else {
                throw new CreateNewEmployeeException("CreateNewEmployeeException: " + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new CreateNewEmployeeException("CreateNewEmployeeException: " + ex.getMessage());
        }
    }

    private boolean isSQLIntegrityConstraintViolationException(PersistenceException ex) {
        return ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException");
    }

    private void validate(EmployeeEntity employeeEntity) throws CreateNewEmployeeException {
        javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EmployeeEntity>> errors = validator.validate(employeeEntity);

        String errorMessage = "Input data validation error!\n";

        for (ConstraintViolation error : errors) {
            errorMessage += "\n\t" + error.getPropertyPath() + " - " + error.getInvalidValue() + "; " + error.getMessage();
        }

        if (errorMessage.length() > 0) {

            throw new CreateNewEmployeeException("CreateNewEmployeeException: Invalid inputs!\n" + errorMessage);
        }
    }

    @Override
    public List<EmployeeEntity> retrieveAllEmployees() {
        Query query = em.createQuery("SELECT e FROM EmployeeEntity e");
        List<EmployeeEntity> employees = (List<EmployeeEntity>) query.getResultList();
        return employees;
    }

    @Override
    public EmployeeEntity retrieveEmployeeByUsernamePassword(String userName, String password) throws InvalidLoginCredentialsException, EmployeeNotFoundException {
        if (userName.length() == 0 || password.length() == 0) {
            throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Missing credentials!");
        }

        try {
            Query query = em.createQuery("SELECT e FROM EmployeeEntity e WHERE e.userName = :inputUserName");
            query.setParameter("inputUserName", userName);
            EmployeeEntity employeeEntity = (EmployeeEntity) query.getSingleResult();
            if (!employeeEntity.getPassword().equals(password)) {
                throw new InvalidLoginCredentialsException("InvalidLoginCredentialsException: Invalid password!");
            }
            return employeeEntity;
        } catch (NoResultException ex) {
            throw new EmployeeNotFoundException("EmployeeNotFoundException: Employee with username " + userName + " does not exist!");
        }
    }

}
