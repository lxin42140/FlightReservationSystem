/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightSchedulePlanEntity;
import javax.ejb.Local;
import util.exception.CreateNewFareException;

/**
 *
 * @author kiyon
 */
@Local
public interface FareEntitySessionBeanLocal {

    public void createFareForFlightSchedulePlan(FareEntity fare, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException;
    
}
