/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FareEntity;
import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.Date;
import java.util.List;
import javax.ejb.Remote;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightNotFoundException;
import util.exception.FlightSchedulePlanInUseException;
import util.exception.FlightSchedulePlanNotFoundException;
import util.exception.UpdateFlightSchedulePlanFailedException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface FlightSchedulePlanSessionBeanRemote {

    public Long createNewNonRecurrentFlightSchedulePlan(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public Long createRecurrentFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public List<FlightSchedulePlanEntity> retrieveAllFlightSchedulePlans();

    public FlightSchedulePlanEntity retrieveFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException;

    public Long updateFlightSchedulePlan(FlightSchedulePlanEntity updatedFlightSchedulePlanEntity) throws FlightSchedulePlanNotFoundException, UpdateFlightSchedulePlanFailedException;

    public void deleteFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException, FlightSchedulePlanInUseException;
}
