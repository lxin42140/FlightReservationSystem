/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

//import static entity.FlightEntity_.flightNumber;
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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import util.enumeration.FlightScheduleTypeEnum;

/**
 *
 * @author Li Xin
 */
@Entity
public class FlightSchedulePlanEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightSchedulePlanId;

    @NotNull
    @Column(nullable = false)
    private FlightScheduleTypeEnum flightSchedulePlanType;

    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    private boolean isDisabled;

    @OneToOne(optional = true, cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @JoinColumn(name = "returnFlightSchedulePlanId")
    private FlightSchedulePlanEntity returnFlightSchedulePlan;

    @OneToMany(mappedBy = "flightSchedulePlan", cascade = {CascadeType.PERSIST})
    @NotEmpty
    private List<FlightScheduleEntity> flightSchedules;

    @OneToMany(mappedBy = "flightSchedulePlan", cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @NotEmpty
    private List<FareEntity> fares;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "flightNumber", nullable = false)
    private FlightEntity flight;

    public FlightSchedulePlanEntity() {
        this.flightSchedules = new ArrayList<>();
        this.fares = new ArrayList<>();
    }

    public Long getFlightSchedulePlanId() {
        return flightSchedulePlanId;
    }

    public FlightEntity getFlight() {
        return flight;
    }

    public void setFlight(FlightEntity flight) {
        this.flight = flight;
    }

    public FlightScheduleTypeEnum getFlightSchedulePlanType() {
        return flightSchedulePlanType;
    }

    public void setFlightSchedulePlanType(FlightScheduleTypeEnum flightSchedulePlanType) {
        this.flightSchedulePlanType = flightSchedulePlanType;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public FlightSchedulePlanEntity getReturnFlightSchedulePlan() {
        return returnFlightSchedulePlan;
    }

    public void setReturnFlightSchedulePlan(FlightSchedulePlanEntity returnFlightSchedulePlan) {
        this.returnFlightSchedulePlan = returnFlightSchedulePlan;
    }

    public List<FlightScheduleEntity> getFlightSchedules() {
        return flightSchedules;
    }

    public void setFlightSchedules(List<FlightScheduleEntity> flightSchedules) {
        this.flightSchedules = flightSchedules;
    }

    public List<FareEntity> getFares() {
        return fares;
    }

    public void setFares(List<FareEntity> fares) {
        this.fares = fares;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flightSchedulePlanId != null ? flightSchedulePlanId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        FlightSchedulePlanEntity other = (FlightSchedulePlanEntity) object;
        if ((this.flightSchedulePlanId == null && other.flightSchedulePlanId != null) || (this.flightSchedulePlanId != null && !this.flightSchedulePlanId.equals(other.flightSchedulePlanId))) {
            return false;
        }
        return true;
    }

}
