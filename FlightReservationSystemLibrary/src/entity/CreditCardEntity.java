/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Li Xin
 */
@Entity
public class CreditCardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditCardId;

    @Future
    @NotNull
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dateOfExpiry;

    @Min(value = 3)
    @Max(value = 3)
    @Column(nullable = false)
    private Long cvc;

    @OneToOne(optional = false)
    @NotNull
    @JoinColumn(nullable = false)
    private FlightReservationEntity flightReservation;

    public Long getCreditCardId() {
        return creditCardId;
    }

    public Date getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(Date dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }

    public Long getCvc() {
        return cvc;
    }

    public void setCvc(Long cvc) {
        this.cvc = cvc;
    }

    public FlightReservationEntity getFlightReservation() {
        return flightReservation;
    }

    public void setFlightReservation(FlightReservationEntity flightReservation) {
        this.flightReservation = flightReservation;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (creditCardId != null ? creditCardId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        CreditCardEntity other = (CreditCardEntity) object;
        if ((this.creditCardId == null && other.creditCardId != null) || (this.creditCardId != null && !this.creditCardId.equals(other.creditCardId))) {
            return false;
        }
        return true;
    }

}
