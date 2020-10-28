/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.EmployeeEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewEmployeeException;
import util.exception.EmployeeNotFoundException;
import util.exception.InvalidLoginCredentialsException;

/**
 *
 * @author Li Xin
 */
@Local
public interface EmployeeEntitySessionBeanLocal {

    public void createNewEmployee(EmployeeEntity newEmployeeEntity) throws CreateNewEmployeeException;

    public List<EmployeeEntity> retrieveAllEmployees();

    public EmployeeEntity retrieveEmployeeByUsernamePassword(String userName, String password) throws InvalidLoginCredentialsException, EmployeeNotFoundException;

}
