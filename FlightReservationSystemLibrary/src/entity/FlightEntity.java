/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Li Xin
 */
@Entity
@IdClass(FlightNumberEntityKey.class)
public class FlightEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank
    private String iataAirlineCode;

    @Id
    @NotBlank
    private String flightNumber;

    private boolean isDisabled;

    private boolean isReturnFlight;

    @OneToOne(optional = true, cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @JoinColumns(value = {
        @JoinColumn(name = "returnIataAirlineCode", referencedColumnName = "iataAirlineCode"),
        @JoinColumn(name = "returnFlightNumber", referencedColumnName = "flightNumber")}
    )
    private FlightEntity returnFlight;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "aircraftConfigurationId", nullable = false)
    @NotNull
    private AircraftConfigurationEntity aircraftConfiguration;

    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "flightRouteId", nullable = false)
    @NotNull
    private FlightRouteEntity flightRoute;

    @OneToMany(mappedBy = "flight", cascade = {CascadeType.PERSIST})
    private List<FlightSchedulePlanEntity> flightSchedulePlans;

    public FlightEntity() {
        this.flightSchedulePlans = new ArrayList<>();
    }

    public FlightEntity(String iataAirlineCode, String flightNumber, AircraftConfigurationEntity aircraftConfiguration, FlightRouteEntity flightRoute) {
        this.iataAirlineCode = iataAirlineCode;
        this.flightNumber = flightNumber;
        this.aircraftConfiguration = aircraftConfiguration;
        this.flightRoute = flightRoute;
    }

    public boolean isIsDisabled() {
        return isDisabled;
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

    public void setIsDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public boolean getIsReturnFlight() {
        return isReturnFlight;
    }

    public void setIsReturnFlight(boolean isReturnFlight) {
        this.isReturnFlight = isReturnFlight;
    }

    public FlightRouteEntity getFlightRoute() {
        return flightRoute;
    }

    public void setFlightRoute(FlightRouteEntity flightRoute) {
        this.flightRoute = flightRoute;
    }

    public AircraftConfigurationEntity getAircraftConfiguration() {
        return aircraftConfiguration;
    }

    public void setAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration) {
        this.aircraftConfiguration = aircraftConfiguration;
    }

    public List<FlightSchedulePlanEntity> getFlightSchedulePlans() {
        return flightSchedulePlans;
    }

    public void setFlightSchedulePlans(List<FlightSchedulePlanEntity> flightSchedulePlans) {
        this.flightSchedulePlans = flightSchedulePlans;
    }

    public FlightEntity getReturnFlight() {
        return returnFlight;
    }

    public void setReturnFlight(FlightEntity returnFlight) {
        this.returnFlight = returnFlight;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flightNumber != null ? flightNumber.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        FlightEntity other = (FlightEntity) object;
        if ((this.flightNumber == null && other.flightNumber != null) || (this.flightNumber != null && !this.flightNumber.equals(other.flightNumber))) {
            return false;
        }
        return true;
    }

}
