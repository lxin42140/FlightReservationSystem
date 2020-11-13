/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 *
 * @author Li Xin
 */
@Entity
public class FlightScheduleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightScheduleId;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "returnFlightScheduleId")
    private FlightScheduleEntity returnFlightSchedule;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date departureDate;

    @Positive
    @Max(24)
    @Column(nullable = false, precision = 3)
    private Integer estimatedFlightDurationHour;

    private Integer estimatedFlightDurationMinute;

    @OneToMany(mappedBy = "flightSchedule", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @NotEmpty
    private List<SeatEntity> seatInventory;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "flightSchedulePlanId", nullable = false)
    @NotNull
    private FlightSchedulePlanEntity flightSchedulePlan;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "flightSchedules")
    private List<FlightReservationEntity> flightReservations;

    public FlightScheduleEntity() {
        this.seatInventory = new ArrayList<>();
        this.flightReservations = new ArrayList<>();
    }

    public FlightScheduleEntity(Date departureDate, Integer estimatedFlightDurationHour) {
        this();
        this.departureDate = departureDate;
        this.estimatedFlightDurationHour = estimatedFlightDurationHour;
        this.estimatedFlightDurationMinute = 0;
    }

    public FlightScheduleEntity(Date departureDate, Integer estimatedFlightDurationHour, Integer estimatedFlightDurationMinute) {
        this();
        this.departureDate = departureDate;
        this.estimatedFlightDurationHour = estimatedFlightDurationHour;
        this.estimatedFlightDurationMinute = estimatedFlightDurationMinute;
    }

    public FlightScheduleEntity(Integer estimatedFlightDurationHour, Integer estimatedFlightDurationMinute) {
        this();
        this.estimatedFlightDurationHour = estimatedFlightDurationHour;
        this.estimatedFlightDurationMinute = estimatedFlightDurationMinute;
    }

    public FlightScheduleEntity getReturnFlightSchedule() {
        return returnFlightSchedule;
    }

    public void setReturnFlightSchedule(FlightScheduleEntity returnFlightSchedule) {
        this.returnFlightSchedule = returnFlightSchedule;
    }

    public List<FlightReservationEntity> getFlightReservations() {
        return flightReservations;
    }

    public void setFlightReservations(List<FlightReservationEntity> flightReservations) {
        this.flightReservations = flightReservations;
    }

    public Long getFlightScheduleId() {
        return flightScheduleId;
    }

    public Date getDepartureDate() {
        return this.departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Integer getEstimatedFlightDurationHour() {
        return estimatedFlightDurationHour;
    }

    public void setEstimatedFlightDurationHour(Integer estimatedFlightDurationHour) {
        this.estimatedFlightDurationHour = estimatedFlightDurationHour;
    }

    public Integer getEstimatedFlightDurationMinute() {
        return estimatedFlightDurationMinute;
    }

    public void setEstimatedFlightDurationMinute(Integer estimatedFlightDurationMinute) {
        this.estimatedFlightDurationMinute = estimatedFlightDurationMinute;
    }

    public FlightSchedulePlanEntity getFlightSchedulePlan() {
        return flightSchedulePlan;
    }

    public void setFlightSchedulePlan(FlightSchedulePlanEntity flightSchedulePlan) {
        this.flightSchedulePlan = flightSchedulePlan;
    }

    public List<SeatEntity> getSeatInventory() {
        // sort seats according to seat number
        this.seatInventory.sort((SeatEntity a, SeatEntity b) -> a.getSeatNumber().compareTo(b.getSeatNumber()));

        return seatInventory;
    }

    public void setSeatInventory(List<SeatEntity> seatInventory) {
        this.seatInventory = seatInventory;
    }

    // retrieve flight duration as date
//    public Date getEstimatedFlightDuration() {
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, this.getEstimatedFlightDurationHour());
//        cal.set(Calendar.MINUTE, this.getEstimatedFlightDurationMinute());
//        return cal.getTime();
//    }

    // return arrival date time in time of destination country
    public Date getArrivalDateTime() {
        GregorianCalendar departureDateTimeCalender = new GregorianCalendar();

        departureDateTimeCalender.setTimeZone(TimeZone.getTimeZone(this.flightSchedulePlan.getFlight().getFlightRoute().getOriginAirport().getTimeZoneId()));
        departureDateTimeCalender.setTime(this.departureDate);
        departureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, this.estimatedFlightDurationHour);
        departureDateTimeCalender.add(GregorianCalendar.MINUTE, this.estimatedFlightDurationMinute); // add minute

        departureDateTimeCalender.setTimeZone(TimeZone.getTimeZone(this.flightSchedulePlan.getFlight().getFlightRoute().getDestinationAirport().getTimeZoneId()));

        return departureDateTimeCalender.getTime();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flightScheduleId != null ? flightScheduleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof FlightScheduleEntity)) {
            return false;
        }
        FlightScheduleEntity other = (FlightScheduleEntity) object;
        if ((this.flightScheduleId == null && other.flightScheduleId != null) || (this.flightScheduleId != null && !this.flightScheduleId.equals(other.flightScheduleId))) {
            return false;
        }
        return true;
    }
}
