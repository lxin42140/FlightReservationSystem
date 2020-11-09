/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightSchedulePlanEntity;
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Remote;
import util.exception.CreateNewFareException;
import util.exception.FlightSchedulePlanNotFoundException;
import util.exception.UpdateFlightSchedulePlanFailedException;

/**
 *
 * @author kiyon
 */
@Remote
public interface FareEntitySessionBeanRemote {

    public void createNewFares(List<FareEntity> fares, FlightSchedulePlanEntity flightSchedulePlanEntity) throws CreateNewFareException;
    
    public FareEntity updateFareAmount(Long flightScheduleId, Long fareId, BigDecimal updatedFareAmount) throws FlightSchedulePlanNotFoundException, UpdateFlightSchedulePlanFailedException;

}
