/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
@Entity
public class CustomerEntity extends UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 32)
    @Column(length = 32, nullable = false)
    private String firstName;

    @Size(min = 1, max = 32)
    @Column(length = 32, nullable = false)
    private String lastName;

    @Email
    @Column(length = 100, nullable = false)
    private String email;

    @Size(min = 8, max = 13)
    @Column(length = 113, nullable = false)
    private String mobilePhoneNumber;

    @Size(min = 1, max = 100)
    @Column(length = 100, nullable = false)
    private String address;

    public CustomerEntity() {
        super();
    }

    public CustomerEntity(String firstName, String lastName, String email, String mobilePhoneNumber, String address, String userName, String password) {
        super(userName, password);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.address = address;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
