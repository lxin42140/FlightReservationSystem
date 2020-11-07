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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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

    @Temporal(TemporalType.TIMESTAMP)
    private Date recurrentEndDate;

    private Integer recurrentFrequency;

    @Column(nullable = false)
    private Boolean isDisabled;

    @Column(nullable = false)
    private Boolean isReturnFlightSchedulePlan;

    @OneToOne(optional = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "returnFlightSchedulePlanId")
    private FlightSchedulePlanEntity returnFlightSchedulePlan;

    @OneToMany(mappedBy = "flightSchedulePlan", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @NotEmpty
    private List<FlightScheduleEntity> flightSchedules;

    @OneToMany(mappedBy = "flightSchedulePlan", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @NotEmpty
    private List<FareEntity> fares;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @NotNull
    @JoinColumn(name = "flightNumber", nullable = false)
    private FlightEntity flight;

    public FlightSchedulePlanEntity() {
        this.isDisabled = false;
        this.isReturnFlightSchedulePlan = false;
        this.flightSchedules = new ArrayList<>();
        this.fares = new ArrayList<>();
    }

    public Long getFlightSchedulePlanId() {
        return flightSchedulePlanId;
    }

    public Date getRecurrentEndDate() {
        return recurrentEndDate;
    }

    public void setRecurrentEndDate(Date recurrentEndDate) {
        this.recurrentEndDate = recurrentEndDate;
    }

    public Integer getRecurrentFrequency() {
        return recurrentFrequency;
    }

    public void setRecurrentFrequency(Integer recurrentFrequency) {
        this.recurrentFrequency = recurrentFrequency;
    }

    public FlightEntity getFlight() {
        return flight;
    }

    public void setFlight(FlightEntity flight) {
        this.flight = flight;
    }

    public Boolean getIsReturnFlightSchedulePlan() {
        return isReturnFlightSchedulePlan;
    }

    public void setIsReturnFlightSchedulePlan(Boolean isReturnFlightSchedulePlan) {
        this.isReturnFlightSchedulePlan = isReturnFlightSchedulePlan;
    }

    public Boolean getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(Boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public FlightSchedulePlanEntity getReturnFlightSchedulePlan() {
        return returnFlightSchedulePlan;
    }

    public void setReturnFlightSchedulePlan(FlightSchedulePlanEntity returnFlightSchedulePlan) {
        this.returnFlightSchedulePlan = returnFlightSchedulePlan;
    }

    public List<FlightScheduleEntity> getFlightSchedules() {
        this.flightSchedules.sort((FlightScheduleEntity a, FlightScheduleEntity b) -> b.getDepartureDate().compareTo(a.getDepartureDate()));
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
        if (!(object instanceof FlightSchedulePlanEntity)) {
            return false;
        }
        FlightSchedulePlanEntity other = (FlightSchedulePlanEntity) object;
        if ((this.flightSchedulePlanId == null && other.flightSchedulePlanId != null) || (this.flightSchedulePlanId != null && !this.flightSchedulePlanId.equals(other.flightSchedulePlanId))) {
            return false;
        }
        return true;
    }

}
