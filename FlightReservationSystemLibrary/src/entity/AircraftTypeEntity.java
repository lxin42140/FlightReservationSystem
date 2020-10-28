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
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
@Entity
public class AircraftTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aricraftId;

    @Positive
    @Max(value = 1000)
    @Column(nullable = false)
    private Long maximumAircraftSeatCapacity;

    @NotBlank
    @Size(max = 32)
    @Column(length = 32, nullable = false, unique = true)
    private String aricraftTypeName;

    public AircraftTypeEntity() {
    }

    public AircraftTypeEntity(Long maximumAircraftSeatCapacity, String aricraftTypeName) {
        this.maximumAircraftSeatCapacity = maximumAircraftSeatCapacity;
        this.aricraftTypeName = aricraftTypeName;
    }

    
    public Long getAricraftId() {
        return aricraftId;
    }

    public Long getMaximumAircraftSeatCapacity() {
        return maximumAircraftSeatCapacity;
    }

    public void setMaximumAircraftSeatCapacity(Long maximumAircraftSeatCapacity) {
        this.maximumAircraftSeatCapacity = maximumAircraftSeatCapacity;
    }

    public String getAricraftTypeName() {
        return aricraftTypeName;
    }

    public void setAricraftTypeName(String aricraftTypeName) {
        this.aricraftTypeName = aricraftTypeName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (aricraftId != null ? aricraftId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        AircraftTypeEntity other = (AircraftTypeEntity) object;
        if ((this.aricraftId == null && other.aricraftId != null) || (this.aricraftId != null && !this.aricraftId.equals(other.aricraftId))) {
            return false;
        }
        return true;
    }

}
