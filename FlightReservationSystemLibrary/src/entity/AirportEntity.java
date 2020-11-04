/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
@Entity
public class AirportEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long airportId;

    @Column(unique = true, nullable = false)
    @Size(min = 1, max = 5)
    private String iataAirlineCode;

    @Column(unique = true, length = 80, nullable = false)
    @NotBlank
    @Size(max = 80)
    private String airportName;

    @Column(length = 80, nullable = false)
    @NotBlank
    @Size(max = 80)
    private String city;

    @Column(length = 80, nullable = false)
    @NotBlank
    @Size(max = 80)
    private String province;

    @Column(length = 80, nullable = false)
    @NotBlank
    @Size(max = 80)
    private String country;

    @Column(nullable = false, length = 40)
    @NotBlank
    private String timeZoneId;

    public AirportEntity() {
    }

    public AirportEntity(String iataAirlineCode, String airportName, String city, String province, String country, String timeZone) {
        this.iataAirlineCode = iataAirlineCode;
        this.airportName = airportName;
        this.city = city;
        this.province = province;
        this.country = country;
        this.timeZoneId = timeZone;
    }

    public Long getAirportId() {
        return airportId;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getIataAirlineCode() {
        return iataAirlineCode;
    }

    public void setIataAirlineCode(String iataAirlineCode) {
        this.iataAirlineCode = iataAirlineCode;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (airportId != null ? airportId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        AirportEntity other = (AirportEntity) object;
        if ((this.airportId == null && other.airportId != null) || (this.airportId != null && !this.airportId.equals(other.airportId))) {
            return false;
        }
        return true;
    }

}
