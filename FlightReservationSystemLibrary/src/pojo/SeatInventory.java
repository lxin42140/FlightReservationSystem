/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import util.enumeration.CabinClassEnum;

/**
 *
 * @author Li Xin
 */
public class SeatInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    HashMap<CabinClassEnum, Integer[]> cabinSeatsInventory;
    Integer totalAvailSeats;
    Integer totalReservedSeats;
    Integer totalBalancedSeats;

    public SeatInventory() {
        this.totalAvailSeats = 0;
        this.totalReservedSeats = 0;
        this.totalBalancedSeats = 0;
        this.cabinSeatsInventory = new HashMap<>();
        cabinSeatsInventory.put(CabinClassEnum.F, new Integer[3]);
        cabinSeatsInventory.put(CabinClassEnum.J, new Integer[3]);
        cabinSeatsInventory.put(CabinClassEnum.W, new Integer[3]);
        cabinSeatsInventory.put(CabinClassEnum.Y, new Integer[3]);
    }

    public HashMap<CabinClassEnum, Integer[]> getCabinSeatsInventory() {
        return cabinSeatsInventory;
    }

    public Integer getTotalAvailSeats() {
        return totalAvailSeats;
    }

    public void setTotalAvailSeats(Integer totalAvailSeats) {
        this.totalAvailSeats = totalAvailSeats;
    }

    public Integer getTotalReservedSeats() {
        return totalReservedSeats;
    }

    public void setTotalReservedSeats(Integer totalReservedSeats) {
        this.totalReservedSeats = totalReservedSeats;
    }

    public Integer getTotalBalancedSeats() {
        return totalBalancedSeats;
    }

    public void setTotalBalancedSeats(Integer totalBalancedSeats) {
        this.totalBalancedSeats = totalBalancedSeats;
    }

}
