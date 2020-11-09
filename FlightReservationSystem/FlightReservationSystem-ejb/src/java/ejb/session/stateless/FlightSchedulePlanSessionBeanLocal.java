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
import java.util.HashSet;
import java.util.List;
import javax.ejb.Local;
import util.exception.CreateNewFlightSchedulePlanException;
import util.exception.FlightNotFoundException;
import util.exception.FlightSchedulePlanInUseException;
import util.exception.FlightSchedulePlanNotFoundException;
import util.exception.UpdateFlightSchedulePlanFailedException;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightSchedulePlanSessionBeanLocal {

    public Long createNewNonRecurrentFlightSchedulePlan(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public Long createRecurrentFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public List<FlightSchedulePlanEntity> retrieveAllFlightSchedulePlans();

    public FlightSchedulePlanEntity retrieveFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException;

    // manual
    public Long updateAddFlightScheduleToFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> newFlightSchedules, Boolean doCreateReturnFlightSchedule) throws UpdateFlightSchedulePlanFailedException;

    // manual
    public Long updateRemoveFlightScheduleFromFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException;

    // recurrent
    public Long updateRecurrentFlightSchedulePlanParameters(Long flightSchedulePlanId, Date newEndDate, Integer newRecurrentFrequency) throws UpdateFlightSchedulePlanFailedException;

    // manual
    public Long updateFlightScheduleDetailForNonRecurrentFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> updatedFlightSchedules) throws UpdateFlightSchedulePlanFailedException;

    public Long updateFareAmountInFlightSchedulePlan(Long flightSchedulePlanId, List<FareEntity> updateFares) throws UpdateFlightSchedulePlanFailedException;

    public Long updateAddFareToFlightSchedulePlan(Long flightSchedulePlanId, List<FareEntity> newFares) throws UpdateFlightSchedulePlanFailedException;

    public Long updateRemoveFareFromFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> fareIds) throws UpdateFlightSchedulePlanFailedException;

    public void deleteFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException, FlightSchedulePlanInUseException;

}
