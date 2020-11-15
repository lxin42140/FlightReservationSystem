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
public class RemoteSeat implements Serializable {

    private Long seatID;
    private String cabinClass;
    private String seatNumber;
    private long passengerId;

    public RemoteSeat() {
    }

    public RemoteSeat(Long seatID, String cabinClass, String seatNumber) {
        this.seatID = seatID;
        this.cabinClass = cabinClass;
        this.seatNumber = seatNumber;
    }

    public long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(long passengerId) {
        this.passengerId = passengerId;
    }

    public Long getSeatID() {
        return seatID;
    }

    public void setSeatID(Long seatID) {
        this.seatID = seatID;
    }

    public String getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.seatID);
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
        final RemoteSeat other = (RemoteSeat) obj;
        if (!Objects.equals(this.seatID, other.seatID)) {
            return false;
        }
        return true;
    }

}
