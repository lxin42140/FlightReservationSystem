/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Li Xin
 */
public class RemoteFlightSchedule implements Serializable {

    private Long flightScheduleID;
    private Date departureDate;
    private String flightNumber;
    private Date arrivalDate;
    private int itineryNumber;

    public RemoteFlightSchedule(Long flightScheduleID, Date departureDate, String flightNumber, Date arrivalDate) {
        this.flightScheduleID = flightScheduleID;
        this.departureDate = departureDate;
        this.flightNumber = flightNumber;
        this.arrivalDate = arrivalDate;
    }

    public RemoteFlightSchedule(Long flightScheduleID, Date departureDate, String flightNumber, Date arrivalDate, int itineryNumber) {
        this.flightScheduleID = flightScheduleID;
        this.departureDate = departureDate;
        this.flightNumber = flightNumber;
        this.arrivalDate = arrivalDate;
        this.itineryNumber = itineryNumber;
    }

    public int getItineryNumber() {
        return itineryNumber;
    }

    public void setItineryNumber(int itineryNumber) {
        this.itineryNumber = itineryNumber;
    }

    public Long getFlightScheduleID() {
        return flightScheduleID;
    }

    public void setFlightScheduleID(Long flightScheduleID) {
        this.flightScheduleID = flightScheduleID;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.flightScheduleID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteFlightSchedule other = (RemoteFlightSchedule) obj;
        if (!Objects.equals(this.flightScheduleID, other.flightScheduleID)) {
            return false;
        }
        return true;
    }

}
