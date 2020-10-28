/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 *
 * @author Li Xin
 */
@Entity
public class FlightReservationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightReservationId;

    @Positive
    @Column(length = 7, unique = true)
    private BigDecimal totalAmount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId", nullable = false)
    @NotNull
    private UserEntity user;

    @OneToOne(mappedBy = "flightReservation", optional = false, cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @JoinColumn(name = "creditCardId", nullable = false)
    @NotNull
    private CreditCardEntity creditCard;

    @OneToMany(mappedBy = "flightReservation", cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @NotEmpty
    private List<PassengerEntity> passengers;

    @OneToMany
    @NotEmpty
    private List<FlightScheduleEntity> flightSchedules;

    public FlightReservationEntity() {
        this.passengers = new ArrayList<>();
        this.flightSchedules = new ArrayList<>();
    }

    public Long getFlightReservationId() {
        return flightReservationId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CreditCardEntity getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCardEntity creditCard) {
        this.creditCard = creditCard;
    }

    public List<PassengerEntity> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerEntity> passengers) {
        this.passengers = passengers;
    }

    public List<FlightScheduleEntity> getFlightSchedules() {
        return flightSchedules;
    }

    public void setFlightSchedules(List<FlightScheduleEntity> flightSchedules) {
        this.flightSchedules = flightSchedules;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flightReservationId != null ? flightReservationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        FlightReservationEntity other = (FlightReservationEntity) object;
        if ((this.flightReservationId == null && other.flightReservationId != null) || (this.flightReservationId != null && !this.flightReservationId.equals(other.flightReservationId))) {
            return false;
        }
        return true;
    }

}
