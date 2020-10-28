/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.exception;

/**
 *
 * @author kiyon
 */
public class CabinConfigurationNotFoundException extends Exception {
    
    public CabinConfigurationNotFoundException() {
    }
    
    public CabinConfigurationNotFoundException(String message) {
        super(message);
    }
}
