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

    public Long createNewNonRecurrentFlightSchedulePlan(List<FlightScheduleEntity> flightSchedules, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public Long createRecurrentWeeklyFlightSchedulePlan(Integer dayOfWeek, Integer departureHourOfDay, Integer departureMinuteOfHour, Date recurrentStartDate, Date recurrentEndDate, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public Long createRecurrentNDaysFlightSchedulePlan(Date endDate, Integer recurrentDaysFrequency, FlightScheduleEntity baseFlightSchedule, List<FareEntity> fares, String flightNumber, Boolean doCreateReturnFlightSchedule, Integer layoverPeriodForReturnFlight) throws CreateNewFlightSchedulePlanException, FlightNotFoundException;

    public List<FlightSchedulePlanEntity> retrieveAllFlightSchedulePlans();

    public FlightSchedulePlanEntity retrieveFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException;

    // manual, update departure date and flight duration for main plan only
    public Long updateFlightScheduleDetailForManualFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> updatedFlightSchedules) throws UpdateFlightSchedulePlanFailedException;

    // manual, add for main flight schedule plan only
    public Long updateAddFlightScheduleToManualFlightSchedulePlan(Long flightSchedulePlanId, List<FlightScheduleEntity> newFlightSchedules, Boolean doCreateReturnFlightSchedule) throws UpdateFlightSchedulePlanFailedException;

    // manual, can remove from either main (which removes from both), or return only
    public Long updateRemoveFlightScheduleFromManualFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> flightScheduleIdsToRemove) throws UpdateFlightSchedulePlanFailedException;

    // recurrent, update end date and recurrent frequency for main only
    public Long updateRecurrentNDaysFlightSchedulePlanParameters(Long flightSchedulePlanId, Date newEndDate, Integer newRecurrentFrequency) throws UpdateFlightSchedulePlanFailedException;

    // recurrent weekly, update the day of week only
    public Long updateRecurrentWeeklyFlightSchedulePlanDayOfWeek(Long flightSchedulePlanId, Integer newDayOfWeek) throws UpdateFlightSchedulePlanFailedException;

    // recurrent weekly, update EITHER start date OR end date, can be earlier start date or later starter or earler end date or later end date
    public Long updateRecurrentWeeklyFlightSchedulePlanRange(Long flightSchedulePlanId, Date newStartDate, Date newEndDate) throws UpdateFlightSchedulePlanFailedException;

    // Either for main or return
    public Long updateFareAmountInFlightSchedulePlan(Long flightSchedulePlanId, List<FareEntity> updateFares) throws UpdateFlightSchedulePlanFailedException;

    // Either for main or return
    public Long updateAddFareToFlightSchedulePlan(Long flightSchedulePlanId, List<FareEntity> newFares) throws UpdateFlightSchedulePlanFailedException;

    // Either for main or return
    public Long updateRemoveFareFromFlightSchedulePlan(Long flightSchedulePlanId, HashSet<Long> fareIds) throws UpdateFlightSchedulePlanFailedException;

    public void deleteFlightSchedulePlanById(Long flightSchedulePlanId) throws FlightSchedulePlanNotFoundException, FlightSchedulePlanInUseException;
}
