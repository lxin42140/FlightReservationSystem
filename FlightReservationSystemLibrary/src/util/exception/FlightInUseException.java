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
public class FlightInUseException extends Exception {

    public FlightInUseException() {
    }

    public FlightInUseException(String string) {
        super(string);
    }

}
