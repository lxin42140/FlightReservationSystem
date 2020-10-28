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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import util.enumeration.EmployeeAccessRightEnum;

/**
 *
 * @author Li Xin
 */
@Entity
public class EmployeeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Size(min = 1, max = 32)
    @Column(length = 32)
    private String firstName;

    @Size(min = 1, max = 32)
    @Column(length = 32)
    private String lastName;

    @NotBlank
    @Size(max = 32)
    @Column(unique = true, length = 32)
    private String userName;

    @Size(min = 1, max = 10)
    private String password;

    @NotNull
    private EmployeeAccessRightEnum employeeAccessRight;

    public EmployeeEntity() {
    }

    public EmployeeEntity(String firstName, String lastName, String userName, String password, EmployeeAccessRightEnum employeeAccessRight) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.employeeAccessRight = employeeAccessRight;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public EmployeeAccessRightEnum getEmployeeAccessRight() {
        return employeeAccessRight;
    }

    public void setEmployeeAccessRight(EmployeeAccessRightEnum employeeAccessRight) {
        this.employeeAccessRight = employeeAccessRight;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (employeeId != null ? employeeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AircraftTypeEntity)) {
            return false;
        }
        EmployeeEntity other = (EmployeeEntity) object;
        if ((this.employeeId == null && other.employeeId != null) || (this.employeeId != null && !this.employeeId.equals(other.employeeId))) {
            return false;
        }
        return true;
    }

}
