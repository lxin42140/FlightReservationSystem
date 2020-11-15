/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightScheduleEntity;
import entity.FlightSchedulePlanEntity;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Remote;
import util.enumeration.CabinClassEnum;
import util.exception.CreateNewFlightScheduleException;
import util.exception.FlightScheduleNotFoundException;

/**
 *
 * @author Li Xin
 */
@Remote
public interface FlightScheduleSessionBeanRemote {

    public void createNewFlightSchedules(FlightSchedulePlanEntity flightSchedulePlanEntity, List<FlightScheduleEntity> flightSchedules) throws CreateNewFlightScheduleException;

    public FlightScheduleEntity retrieveFlightScheduleById(Long flightScheduleId) throws FlightScheduleNotFoundException;

    public HashMap<CabinClassEnum, Double> getLowestFaresForCabin(Long flightScheduleId) throws FlightScheduleNotFoundException;

    public HashMap<CabinClassEnum, Double> getHighestFaresForCabin(Long flightScheduleId) throws FlightScheduleNotFoundException;

}
