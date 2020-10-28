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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
@Entity
public class AircraftConfigurationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aircraftConfigurationId;

    @NotBlank
    @Column(unique = true, length = 32, nullable = false)
    private String aircraftConfigurationName;

    @Positive
    @Max(value = 1000)
    @Column(nullable = false)
    private Long maximumConfigurationSeatCapacity;

    @Positive
    @Max(value = 4)
    @Column(nullable = false)
    private Long numberOfCabins;

    @OneToOne(optional = false)
    @JoinColumn(name = "aircraftTypeId", nullable = false)
    @NotNull
    private AircraftTypeEntity aircraftType;

    @Size(max = 4)
    @NotEmpty
    @OneToMany(mappedBy = "aircraftConfiguration", orphanRemoval = true, cascade = {CascadeType.PERSIST})
    private List<CabinConfigurationEntity> cabinConfigurations;

    public AircraftConfigurationEntity() {
        this.cabinConfigurations = new ArrayList<>();
    }

    public Long getAircraftConfigurationId() {
        return aircraftConfigurationId;
    }

    public String getAircraftConfigurationName() {
        return aircraftConfigurationName;
    }

    public void setAircraftConfigurationName(String aircraftConfigurationName) {
        this.aircraftConfigurationName = aircraftConfigurationName;
    }

    public AircraftTypeEntity getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(AircraftTypeEntity aircraftType) {
        this.aircraftType = aircraftType;
    }

    public Long getMaximumConfigurationSeatCapacity() {
        return maximumConfigurationSeatCapacity;
    }

    public void setMaximumConfigurationSeatCapacity(Long maximumConfigurationSeatCapacity) {
        this.maximumConfigurationSeatCapacity = maximumConfigurationSeatCapacity;
    }

    public Long getNumberOfCabins() {
        return numberOfCabins;
    }

    public void setNumberOfCabins(Long numberOfCabins) {
        this.numberOfCabins = numberOfCabins;
    }

    public List<CabinConfigurationEntity> getCabinConfigurations() {
        return cabinConfigurations;
    }

    public void setCabinConfigurations(List<CabinConfigurationEntity> cabinConfigurations) {
        this.cabinConfigurations = cabinConfigurations;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (aircraftConfigurationId != null ? aircraftConfigurationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the depositAccountId fields are not set
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        AircraftConfigurationEntity other = (AircraftConfigurationEntity) object;
        if ((this.aircraftConfigurationId == null && other.aircraftConfigurationId != null) || (this.aircraftConfigurationId != null && !this.aircraftConfigurationId.equals(other.aircraftConfigurationId))) {
            return false;
        }
        return true;
    }

}
