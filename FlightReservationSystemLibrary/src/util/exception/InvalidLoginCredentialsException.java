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
public class InvalidLoginCredentialsException extends Exception {

    public InvalidLoginCredentialsException() {
    }

    public InvalidLoginCredentialsException(String msg) {
        super(msg);
    }

}
