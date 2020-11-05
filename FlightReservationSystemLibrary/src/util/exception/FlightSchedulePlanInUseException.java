/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.exception;

/**
 *
 * @author Li Xin
 */
public class FlightSchedulePlanInUseException extends Exception {

    public FlightSchedulePlanInUseException() {
    }

    public FlightSchedulePlanInUseException(String string) {
        super(string);
    }

}
