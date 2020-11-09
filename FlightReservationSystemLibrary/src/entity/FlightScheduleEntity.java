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
    private Integer estimatedFlightDuration;

    @OneToMany(mappedBy = "flightSchedule", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
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

//    public FlightScheduleEntity(Date departureDate, Integer estimatedFlightDuration, FlightSchedulePlanEntity flightSchedulePlan) {
//        this();
//        this.departureDate = departureDate;
//        this.estimatedFlightDuration = estimatedFlightDuration;
//        this.flightSchedulePlan = flightSchedulePlan;
//    }
    public FlightScheduleEntity(Date departureDate, Integer estimatedFlightDuration) {
        this();
        this.departureDate = departureDate;
        this.estimatedFlightDuration = estimatedFlightDuration;
    }

    public FlightScheduleEntity getReturnFlightSchedule() {
        return returnFlightSchedule;
    }

    public void setReturnFlightSchedule(FlightScheduleEntity returnFlightSchedule) {
        this.returnFlightSchedule = returnFlightSchedule;
    }

//    public Boolean getIsReturnFlightSchedule() {
//        return isReturnFlightSchedule;
//    }
//
//    public void setIsReturnFlightSchedule(Boolean isReturnFlightSchedule) {
//        this.isReturnFlightSchedule = isReturnFlightSchedule;
//    }
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

    public Integer getEstimatedFlightDuration() {
        return estimatedFlightDuration;
    }

    public void setEstimatedFlightDuration(Integer estimatedFlightDuration) {
        this.estimatedFlightDuration = estimatedFlightDuration;
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

    // return arrival date time in time of destination country
    public Date getArrivalDateTime() {
        GregorianCalendar departureDateTimeCalender = new GregorianCalendar();
        departureDateTimeCalender.setTime(this.departureDate);
        departureDateTimeCalender.add(GregorianCalendar.HOUR_OF_DAY, this.estimatedFlightDuration);
        Date arrivalDateTime = departureDateTimeCalender.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String destinationTimeZoneId = this.flightSchedulePlan.getFlight().getFlightRoute().getDestinationAirport().getTimeZoneId();
        sdf.setTimeZone(TimeZone.getTimeZone(destinationTimeZoneId));
        String arrivalDate = sdf.format(arrivalDateTime);

        Date arrivalDateInLocalTime = arrivalDateTime;
        try {
            arrivalDateInLocalTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a").parse(arrivalDate);
        } catch (ParseException ex) {
        }

        return arrivalDateInLocalTime;
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
