/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import util.enumeration.CabinClassEnum;

/**
 *
 * @author Li Xin
 */
@Entity
public class FareEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fareId;

    @Size(min = 3, max = 7)
    @Column(length = 7, nullable = false)
    @NotNull
    private String fareBasisCode;

    @Column(nullable = false, precision = 19, scale = 4)
    @Positive
    @NotNull
    private BigDecimal fareAmount;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CabinClassEnum cabinClass;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flightSchedulePlanId", nullable = false)
    @NotNull
    private FlightSchedulePlanEntity flightSchedulePlan;

    public FareEntity() {
    }

    public FareEntity(String fareBasisCode, BigDecimal fareAmount, CabinClassEnum cabinClass) {
        this.fareBasisCode = fareBasisCode;
        this.fareAmount = fareAmount;
        this.cabinClass = cabinClass;
    }

    public FareEntity(String fareBasisCode, BigDecimal fareAmount, CabinClassEnum cabinClass, FlightSchedulePlanEntity flightSchedulePlan) {
        this.fareBasisCode = fareBasisCode;
        this.fareAmount = fareAmount;
        this.cabinClass = cabinClass;
        this.flightSchedulePlan = flightSchedulePlan;
    }

    public Long getFareId() {
        return fareId;
    }

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    public BigDecimal getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(BigDecimal fareAmount) {
        this.fareAmount = fareAmount;
    }

    public CabinClassEnum getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClassEnum cabinClass) {
        this.cabinClass = cabinClass;
    }

    public FlightSchedulePlanEntity getFlightSchedulePlan() {
        return flightSchedulePlan;
    }

    public void setFlightSchedulePlan(FlightSchedulePlanEntity flightSchedulePlan) {
        this.flightSchedulePlan = flightSchedulePlan;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fareId != null ? fareId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        FareEntity other = (FareEntity) object;
        if ((this.fareId == null && other.fareId != null) || (this.fareId != null && !this.fareId.equals(other.fareId))) {
            return false;
        }
        return true;
    }

}
