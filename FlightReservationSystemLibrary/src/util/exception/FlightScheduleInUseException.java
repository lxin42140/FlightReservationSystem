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
public class FlightScheduleInUseException extends Exception {

    public FlightScheduleInUseException() {
    }

    public FlightScheduleInUseException(String string) {
        super(string);
    }

}
