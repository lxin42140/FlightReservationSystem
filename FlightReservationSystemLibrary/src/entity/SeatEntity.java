/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import util.enumeration.CabinClassEnum;

/**
 *
 * @author Li Xin
 */
@Entity
public class SeatEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @NotNull
    @Column(name = "CabinClass", nullable = false)
    @Enumerated(EnumType.STRING)
    private CabinClassEnum cabinClassEnum;

    @Column(nullable = false, length = 5)
    @NotBlank
    private String seatNumber;

    // track the fare basis code used to book the seat
    @Size(min = 3, max = 7)
    @Column(length = 7)
    private String fareBasisCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FlightScheduleId", nullable = false)
    @NotNull
    private FlightScheduleEntity flightSchedule;

    @ManyToOne(optional = true, cascade = {})
    @JoinColumn(name = "passengerId")
    private PassengerEntity passenger;

    public SeatEntity() {
    }

    public SeatEntity(CabinClassEnum cabinClassEnum, String seatNumber, FlightScheduleEntity flightSchedule) {
        this.cabinClassEnum = cabinClassEnum;
        this.seatNumber = seatNumber;
        this.flightSchedule = flightSchedule;
    }

    public Long getSeatId() {
        return seatId;
    }

    public CabinClassEnum getCabinClassEnum() {
        return cabinClassEnum;
    }

    public void setCabinClassEnum(CabinClassEnum cabinClassEnum) {
        this.cabinClassEnum = cabinClassEnum;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    public PassengerEntity getPassenger() {
        return passenger;
    }

    public void setPassenger(PassengerEntity passenger) {
        this.passenger = passenger;
    }

    public FlightScheduleEntity getFlightSchedule() {
        return flightSchedule;
    }

    public void setFlightSchedule(FlightScheduleEntity flightSchedule) {
        this.flightSchedule = flightSchedule;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (seatId != null ? seatId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof SeatEntity)) {
            return false;
        }
        SeatEntity other = (SeatEntity) object;
        if ((this.seatId == null && other.seatId != null) || (this.seatId != null && !this.seatId.equals(other.seatId))) {
            return false;
        }
        return true;
    }
}
