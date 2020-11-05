/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.FlightReservationEntity;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Li Xin
 */
@Stateless
public class FlightReservationSessionBean implements FlightReservationSessionBeanRemote, FlightReservationSessionBeanLocal {

    @PersistenceContext(unitName = "FlightReservationSystem-ejbPU")
    private EntityManager em;

    @Override
    public List<FlightReservationEntity> viewFlightReservationsByFlightScheduleId(Long flightScheduleId) {
        Query query = em.createQuery("SELECT DISTINCT f from FlightReservationEntity f, IN (f.flightSchedules) fs, IN (f.passengers.seats) s WHERE fs.flightScheduleId =:inFlightScheduleId ORDER BY s.seatNumber");
        query.setParameter("inFlightScheduleId", flightScheduleId);
        List<FlightReservationEntity> flightReservations = query.getResultList();

        return flightReservations;
    }

}
