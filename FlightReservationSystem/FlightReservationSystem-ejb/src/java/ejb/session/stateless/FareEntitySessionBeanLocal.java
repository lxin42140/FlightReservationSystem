/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightEntity;
import entity.FlightSchedulePlanEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewFareException;
import util.exception.InvalidFareException;

/**
 *
 * @author kiyon
 */
@Local
public interface FareEntitySessionBeanLocal {

    public void createNewFare(FareEntity fare, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException;

    public void createNewFares(List<FareEntity> fares, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException;


}
