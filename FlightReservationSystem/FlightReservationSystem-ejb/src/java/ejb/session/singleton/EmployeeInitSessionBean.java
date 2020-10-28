/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.EmployeeEntitySessionBeanLocal;
import entity.EmployeeEntity;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.enumeration.EmployeeAccessRightEnum;
import util.exception.CreateNewEmployeeException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class EmployeeInitSessionBean {

    @EJB
    private EmployeeEntitySessionBeanLocal employeeEntitySessionBean;

    @PostConstruct
    public void postConstruct() {
        if (!employeeEntitySessionBean.retrieveAllEmployees().isEmpty()) {
            return;
        }

        try {
            employeeEntitySessionBean.createNewEmployee(new EmployeeEntity("Li", "Xin", "user1", "password", EmployeeAccessRightEnum.ADMIN));
            employeeEntitySessionBean.createNewEmployee(new EmployeeEntity("Li", "Xin", "user2", "password", EmployeeAccessRightEnum.FLEETMANAGER));
            employeeEntitySessionBean.createNewEmployee(new EmployeeEntity("Li", "Xin", "user3", "password", EmployeeAccessRightEnum.ROUTEMANAGER));
            employeeEntitySessionBean.createNewEmployee(new EmployeeEntity("Li", "Xin", "user4", "password", EmployeeAccessRightEnum.SCHEDULEMANAGER));
            employeeEntitySessionBean.createNewEmployee(new EmployeeEntity("Li", "Xin", "user5", "password", EmployeeAccessRightEnum.SALESMANAGER));
        } catch (CreateNewEmployeeException ex) {
            System.out.println(ex);
        }
    }

}
