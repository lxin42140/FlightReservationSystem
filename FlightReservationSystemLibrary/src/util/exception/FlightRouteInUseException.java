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
public class FlightRouteInUseException extends Exception {

    public FlightRouteInUseException() {
    }

    public FlightRouteInUseException(String string) {
        super(string);
    }

}
