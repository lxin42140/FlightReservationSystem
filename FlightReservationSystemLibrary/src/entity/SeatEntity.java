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

//    @NotNull
//    @OneToOne(optional = false)
//    @JoinColumn(name = "cabinConfigurationId", nullable = false)
//    private CabinConfigurationEntity cabinConfiguration;
    @NotNull
    @Column(name = "CabinClass", nullable = false)
    @Enumerated(EnumType.STRING)
    private CabinClassEnum cabinClassEnum;

    @Column(nullable = false, length = 5)
    @NotBlank
    private String seatNumber;

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

//    public SeatEntity(CabinConfigurationEntity cabinConfiguration, Integer seatRowNumber, Character seatRowLetter, FlightScheduleEntity flightSchedule) {
//        this.cabinConfiguration = cabinConfiguration;
//        this.seatRowNumber = seatRowNumber;
//        this.seatRowLetter = seatRowLetter;
//        this.flightSchedule = flightSchedule;
//    }
    public Long getSeatId() {
        return seatId;
    }

//    public CabinConfigurationEntity getCabinConfiguration() {
//        return cabinConfiguration;
//    }
//
//    public void setCabinConfiguration(CabinConfigurationEntity cabinConfiguration) {
//        this.cabinConfiguration = cabinConfiguration;
//    }
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
