/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import javax.ejb.Remote;
import util.exception.CreateNewFlightScheduleException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface FlightScheduleSessionBeanRemote {

    public void createNewFlightSchedule(FlightScheduleEntity flightScheduleEntity, FlightSchedulePlanEntity flightSchedulePlanEntity, FlightEntity flightEntity) throws CreateNewFlightScheduleException;

}
