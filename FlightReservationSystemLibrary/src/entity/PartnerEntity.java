/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

/**
 *
 * @author Li Xin
 */
@Entity
public class PartnerEntity extends UserEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(min = 1, max = 72)
    @Column(length = 72, nullable = false)
    private String partnerName;

    public PartnerEntity() {
        super();
    }

    public PartnerEntity(String partnerName, String userName, String password) {
        super(userName, password);
        this.partnerName = partnerName;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

}
