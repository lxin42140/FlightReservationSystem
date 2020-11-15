/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.ws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Li Xin
 */
public class RemoteReservationDetails implements Serializable {

    private double totalAmount;
    List<RemoteFlightSchedule> itinery;
    List<RemotePassenger> passengers;
    List<RemoteSeat> seats;

    public RemoteReservationDetails() {
        this.itinery = new ArrayList<>();
        this.passengers = new ArrayList<>();
        this.seats = new ArrayList<>();
    }

    public List<RemoteFlightSchedule> getItinery() {
        return itinery;
    }

    public void setItinery(List<RemoteFlightSchedule> itinery) {
        this.itinery = itinery;
    }

    public List<RemotePassenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<RemotePassenger> passengers) {
        this.passengers = passengers;
    }

    public List<RemoteSeat> getSeats() {
        return seats;
    }

    public void setSeats(List<RemoteSeat> seats) {
        this.seats = seats;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

}
