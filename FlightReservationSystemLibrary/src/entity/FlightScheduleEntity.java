/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Min;
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

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date departureDate;

    @Positive
    @Column(nullable = false)
    private Long estimatedFlightDuration;

    @OneToMany(mappedBy = "flightSchedule", cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @NotEmpty
    private List<SeatEntity> seatInventory;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flightSchedulePlanId", nullable = false)
    @NotNull
    private FlightSchedulePlanEntity flightSchedulePlan;

//    @ManyToMany(cascade = {CascadeType.PERSIST})
//    private List<FlightReservationEntity> flightReservations;
    
    public FlightScheduleEntity() {
        this.seatInventory = new ArrayList<>();
        //this.flightReservations = new ArrayList<>();
    }

    public Long getFlightScheduleId() {
        return flightScheduleId;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Long getEstimatedFlightDuration() {
        return estimatedFlightDuration;
    }

    public void setEstimatedFlightDuration(Long estimatedFlightDuration) {
        this.estimatedFlightDuration = estimatedFlightDuration;
    }

    public FlightSchedulePlanEntity getFlightSchedulePlan() {
        return flightSchedulePlan;
    }

    public void setFlightSchedulePlan(FlightSchedulePlanEntity flightSchedulePlan) {
        this.flightSchedulePlan = flightSchedulePlan;
    }

//    public List<FlightReservationEntity> getFlightReservations() {
//        return flightReservations;
//    }
//
//    public void setFlightReservations(List<FlightReservationEntity> flightReservations) {
//        this.flightReservations = flightReservations;
//    }
    public List<SeatEntity> getSeatInventory() {
        return seatInventory;
    }

    public void setSeatInventory(List<SeatEntity> seatInventory) {
        this.seatInventory = seatInventory;
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
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        FlightScheduleEntity other = (FlightScheduleEntity) object;
        if ((this.flightScheduleId == null && other.flightScheduleId != null) || (this.flightScheduleId != null && !this.flightScheduleId.equals(other.flightScheduleId))) {
            return false;
        }
        return true;
    }
}
