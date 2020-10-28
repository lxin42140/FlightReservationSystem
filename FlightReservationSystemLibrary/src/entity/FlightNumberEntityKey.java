/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
//@Embeddable
public class FlightNumberEntityKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private String iataAirlineCode;

    private String flightNumber;

    public FlightNumberEntityKey() {
    }

    public FlightNumberEntityKey(String iataAirlineCode, String flightNumber) {
        this.iataAirlineCode = iataAirlineCode;
        this.flightNumber = flightNumber;
    }

    public String getIataAirlineCode() {
        return iataAirlineCode;
    }

    public void setIataAirlineCode(String iataAirlineCode) {
        this.iataAirlineCode = iataAirlineCode;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.iataAirlineCode);
        hash = 67 * hash + Objects.hashCode(this.flightNumber);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlightNumberEntityKey other = (FlightNumberEntityKey) obj;
        if (!Objects.equals(this.iataAirlineCode, other.iataAirlineCode)) {
            return false;
        }
        if (!Objects.equals(this.flightNumber, other.flightNumber)) {
            return false;
        }
        return true;
    }

}
