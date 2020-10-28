/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.singleton;

import ejb.session.stateless.PartnerEntitySessionBeanLocal;
import entity.PartnerEntity;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import util.exception.CreateNewPartnerException;

/**
 *
 * @author Li Xin
 */
@Singleton
@LocalBean
@Startup
public class PartnerInitSessionBean {

    @EJB
    private PartnerEntitySessionBeanLocal partnerEntitySessionBean;

    @PostConstruct
    public void postConstruct() {
        if (!partnerEntitySessionBean.retrieveAllPartners().isEmpty()) {
            return;
        }

        try {
            partnerEntitySessionBean.createNewPartner(new PartnerEntity("Holiday Reservation.com", "partner1", "password"));
        } catch (CreateNewPartnerException ex) {
            System.out.println(ex);
        }
    }
}
