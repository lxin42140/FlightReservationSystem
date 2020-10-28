/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import util.enumeration.CabinClassEnum;
//import util.enumeration.SeatStatusEnum;

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
    @Column(nullable = false)
    private CabinClassEnum cabinClass;

//    @NotNull
//    private SeatStatusEnum seatStatus;
    @Min(value = 1)
    @Max(value = 100)
    @Column(nullable = false)
    private Long seatRowNumber;

    @Size(min = 1, max = 1)
    @NotBlank
    @Column(nullable = false)
    private Character seatRowLetter;

    @ManyToOne(optional = true)
    @JoinColumn(name = "passengerId", nullable = false)
    private PassengerEntity passenger;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FlightScheduleId", nullable = false)
    @NotNull
    private FlightScheduleEntity flightSchedule;

    public Long getSeatId() {
        return seatId;
    }

    public CabinClassEnum getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClassEnum cabinClass) {
        this.cabinClass = cabinClass;
    }

//    public SeatStatusEnum getSeatStatus() {
//        return seatStatus;
//    }
//
//    public void setSeatStatus(SeatStatusEnum seatStatus) {
//        this.seatStatus = seatStatus;
//    }
    public Long getSeatRowNumber() {
        return seatRowNumber;
    }

    public void setSeatRowNumber(Long seatRowNumber) {
        this.seatRowNumber = seatRowNumber;
    }

    public Character getSeatRowLetter() {
        return seatRowLetter;
    }

    public void setSeatRowLetter(Character seatRowLetter) {
        this.seatRowLetter = seatRowLetter;
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
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        SeatEntity other = (SeatEntity) object;
        if ((this.seatId == null && other.seatId != null) || (this.seatId != null && !this.seatId.equals(other.seatId))) {
            return false;
        }
        return true;
    }
}
