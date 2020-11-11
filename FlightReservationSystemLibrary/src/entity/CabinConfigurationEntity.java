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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import util.enumeration.CabinClassEnum;

/**
 *
 * @author Li Xin
 */
@Entity
public class CabinConfigurationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cabinConfigurationId;

    @Min(value = 1)
    @Max(value = 2)
    @Column(nullable = false)
    private Long numberOfAisles;

    @Max(value = 10)
    @Column(nullable = false)
    private Long numberOfSeatsAbreast;

    @Min(value = 1)
    @Max(value = 100)
    @Column(nullable = false)
    private Long numberOfRows;

    @Positive
    @Max(value = 1000)
    @Column(nullable = false)
    private Long maximumCabinSeatCapacity;

    @Column(length = 5, nullable = false)
    @NotBlank
    @Size(min = 3, max = 5)
    private String seatingConfiguration;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CabinClassEnum cabinClass;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraftConfigurationId", nullable = false)
    private AircraftConfigurationEntity aircraftConfiguration;

    public CabinConfigurationEntity() {
    }

    public CabinConfigurationEntity(CabinClassEnum cabinClass, Long NumberOfAisle, Long NumberOfRow, Long NumberOfSeatAbreast, String SeatConfiguration, Long MaxCapacity) {
        this.cabinClass = cabinClass;
        numberOfAisles = NumberOfAisle;
        numberOfSeatsAbreast = NumberOfSeatAbreast;
        numberOfRows = NumberOfRow;
        maximumCabinSeatCapacity = MaxCapacity;
        seatingConfiguration = SeatConfiguration;
    }

    public Long getCabinConfigurationId() {
        return cabinConfigurationId;
    }

    public Long getNumberOfAisles() {
        return numberOfAisles;
    }

    public void setNumberOfAisles(Long numberOfAisles) {
        this.numberOfAisles = numberOfAisles;
    }

    public Long getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(Long numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public Long getNumberOfSeatsAbreast() {
        return numberOfSeatsAbreast;
    }

    public void setNumberOfSeatsAbreast(Long numberOfSeatsAbreast) {
        this.numberOfSeatsAbreast = numberOfSeatsAbreast;
    }

    public String getSeatingConfiguration() {
        return seatingConfiguration;
    }

    public void setSeatingConfiguration(String seatingConfiguration) {
        this.seatingConfiguration = seatingConfiguration;
    }

    public Long getMaximumCabinSeatCapacity() {
        return maximumCabinSeatCapacity;
    }

    public void setMaximumCabinSeatCapacity(Long maximumCabinSeatCapacity) {
        this.maximumCabinSeatCapacity = maximumCabinSeatCapacity;
    }

    public CabinClassEnum getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClassEnum cabinClass) {
        this.cabinClass = cabinClass;
    }

    public AircraftConfigurationEntity getAircraftConfiguration() {
        return aircraftConfiguration;
    }

    public void setAircraftConfiguration(AircraftConfigurationEntity aircraftConfiguration) {
        this.aircraftConfiguration = aircraftConfiguration;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cabinConfigurationId != null ? cabinConfigurationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof CabinConfigurationEntity)) {
            return false;
        }
        CabinConfigurationEntity other = (CabinConfigurationEntity) object;
        if ((this.cabinConfigurationId == null && other.cabinConfigurationId != null) || (this.cabinConfigurationId != null && !this.cabinConfigurationId.equals(other.cabinConfigurationId))) {
            return false;
        }
        return true;
    }
}
