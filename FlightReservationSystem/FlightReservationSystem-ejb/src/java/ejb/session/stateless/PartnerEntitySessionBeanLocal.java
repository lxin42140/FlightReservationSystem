/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.PartnerEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewPartnerException;
import util.exception.InvalidLoginCredentialsException;
import util.exception.PartnerNotFoundException;

/**
 *
 * @author Li Xin
 */
@Local
public interface PartnerEntitySessionBeanLocal {

    public Long createNewPartner(PartnerEntity newPartnerEntity) throws CreateNewPartnerException;

    public List<PartnerEntity> retrieveAllPartners();

    public PartnerEntity retrievePartnerByUsernamePassword(String userName, String password) throws InvalidLoginCredentialsException, PartnerNotFoundException;

}
