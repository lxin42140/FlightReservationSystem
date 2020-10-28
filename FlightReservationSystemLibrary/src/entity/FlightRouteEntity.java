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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Li Xin
 */
@Entity
public class FlightRouteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightRouteId;

    private boolean isDisabled;

    private boolean isReturnFlightRoute;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "originAirportId", nullable = false)
    @NotNull
    private AirportEntity originAirport;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "destinationAirportId", nullable = false)
    @NotNull
    private AirportEntity destinationAirport;

    @OneToOne(optional = true, cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @JoinColumn(name = "returnFlightRouteId")
    private FlightRouteEntity returnFlightRoute;

    @OneToMany(mappedBy = "flightRoute", cascade = {CascadeType.PERSIST})
    private List<FlightEntity> flights;

    public FlightRouteEntity() {
        this.flights = new ArrayList<>();
    }

    public FlightRouteEntity(AirportEntity originAirport, AirportEntity destinationAirport) {
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.flights = new ArrayList<>();
    }

    public Long getFlightRouteId() {
        return flightRouteId;
    }

    public AirportEntity getOriginAirport() {
        return originAirport;
    }

    public void setOriginAirport(AirportEntity originAirport) {
        this.originAirport = originAirport;
    }

    public AirportEntity getDestinationAirport() {
        return destinationAirport;
    }

    public void setDestinationAirport(AirportEntity destinationAirport) {
        this.destinationAirport = destinationAirport;
    }

    public FlightRouteEntity getReturnFlightRoute() {
        return returnFlightRoute;
    }

    public void setReturnFlightRoute(FlightRouteEntity returnFlightRoute) {
        this.returnFlightRoute = returnFlightRoute;
    }

    public boolean getIsReturnRlight() {
        return this.isReturnFlightRoute;
    }

    public void setIsReturnFlightRoute(boolean isReturnFlight) {
        this.isReturnFlightRoute = isReturnFlight;
    }

    public boolean getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public List<FlightEntity> getFlights() {
        return flights;
    }

    public void setFlights(List<FlightEntity> flights) {
        this.flights = flights;
    }

    public boolean isValid() {
        return !this.originAirport.equals(this.destinationAirport);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (flightRouteId != null ? flightRouteId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        FlightRouteEntity other = (FlightRouteEntity) object;
        if ((this.flightRouteId == null && other.flightRouteId != null) || (this.flightRouteId != null && !this.flightRouteId.equals(other.flightRouteId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FlightRouteEntity{" + "flightRouteId=" + flightRouteId + ", isReturnRlight=" + isReturnFlightRoute + ", originAirport=" + originAirport + ", destinationAirport=" + destinationAirport + '}';
    }

}
