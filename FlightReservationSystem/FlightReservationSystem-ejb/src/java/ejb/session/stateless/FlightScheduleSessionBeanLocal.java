/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewFlightScheduleException;
import util.exception.FlightScheduleNotFoundException;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightScheduleSessionBeanLocal {

    public void createNewFlightSchedules(FlightSchedulePlanEntity flightSchedulePlanEntity, List<FlightScheduleEntity> flightSchedules) throws CreateNewFlightScheduleException;

    public FlightScheduleEntity retrieveFlightScheduleById(Long flightScheduleId) throws FlightScheduleNotFoundException;

    public void checkFlightSchedules(FlightScheduleEntity newFlightScheduleEntity, FlightEntity flightEntity) throws CreateNewFlightScheduleException;

    // local interface only
    public FlightScheduleEntity createReturnFlightSchedule(FlightScheduleEntity flightSchedule, Integer layoverPeriodForReturnFlight);
}
