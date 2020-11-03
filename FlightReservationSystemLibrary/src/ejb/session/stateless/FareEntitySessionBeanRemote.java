/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightSchedulePlanEntity;
import javax.ejb.Remote;
import util.exception.CreateNewFareException;

/**
 *
 * @author kiyon
 */
@Remote
public interface FareEntitySessionBeanRemote {

    public void createFareForFlightSchedulePlan(FareEntity fare, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException;

}
