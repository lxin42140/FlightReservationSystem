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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)

public class UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, length = 32, nullable = false)
    @Size(min = 1, max = 32)
    private String userName;

    @Column(length = 10, nullable = false)
    @Size(min = 1, max = 10)
    private String password;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FlightReservationEntity> flightReservations;

    public UserEntity() {
        this.flightReservations = new ArrayList<>();
    }

    public UserEntity(String userName, String password) {
        this();
        this.userName = userName;
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<FlightReservationEntity> getFlightReservations() {
        return flightReservations;
    }

    public void setFlightReservations(List<FlightReservationEntity> flightReservations) {
        this.flightReservations = flightReservations;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserEntity)) {
            return false;
        }
        UserEntity other = (UserEntity) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

}
