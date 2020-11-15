/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Li Xin
 */
public class RemoteFare implements Serializable {

    private Long fareId;

    private String fareBasisCode;

    private double fareAmount;

    public RemoteFare(Long fareId, String fareBasisCode, double fareAmount) {
        this.fareId = fareId;
        this.fareBasisCode = fareBasisCode;
        this.fareAmount = fareAmount;
    }

    public Long getFareId() {
        return fareId;
    }

    public void setFareId(Long fareId) {
        this.fareId = fareId;
    }

    public String getFareBasisCode() {
        return fareBasisCode;
    }

    public void setFareBasisCode(String fareBasisCode) {
        this.fareBasisCode = fareBasisCode;
    }

    public double getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(double fareAmount) {
        this.fareAmount = fareAmount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.fareId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteFare other = (RemoteFare) obj;
        if (!Objects.equals(this.fareId, other.fareId)) {
            return false;
        }
        return true;
    }

}
