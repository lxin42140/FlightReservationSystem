/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.List;
import javax.ejb.Remote;
import util.exception.CreateNewFlightScheduleException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface FlightScheduleSessionBeanRemote {

    public void createNewFlightSchedules(FlightSchedulePlanEntity flightSchedulePlanEntity, List<FlightScheduleEntity> flightSchedules) throws CreateNewFlightScheduleException;

}
