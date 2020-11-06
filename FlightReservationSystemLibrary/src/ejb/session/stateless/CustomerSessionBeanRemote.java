/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.CustomerEntity;
import javax.ejb.Remote;
import util.exception.CreateNewCustomerException;
import util.exception.CustomerNotFoundException;
import util.exception.InvalidLoginCredentialsException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface CustomerSessionBeanRemote {

    public Long registerNewCustomer(CustomerEntity customer) throws CreateNewCustomerException;

    public CustomerEntity retrieveCustomerByUsernameAndPassword(String username, String password) throws CustomerNotFoundException, InvalidLoginCredentialsException;

}
