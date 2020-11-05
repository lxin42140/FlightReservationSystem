/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightReservationEntity;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Li Xin
 */
@Local
public interface FlightReservationSessionBeanLocal {

    public List<FlightReservationEntity> viewFlightReservationsByFlightScheduleId(Long flightScheduleId);
    
}
