/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejb.session.stateless;

import entity.AirportEntity;
import java.util.List;
import javax.ejb.Local;
import util.exception.AirportNotFoundException;
import util.exception.CreateNewAirportException;

/**
 *
 * @author Li Xin
 */
@Local
public interface AirportEntitySessionBeanLocal {

    public Long createNewAirport(AirportEntity newAirportEntity) throws CreateNewAirportException;

    public List<AirportEntity> retrieveAllAirports();

    public AirportEntity retrieveAirportByid(Long airportId) throws AirportNotFoundException;

    public AirportEntity retrieveAirportByIataCode(String iataCode) throws AirportNotFoundException;

    public AirportEntity retrieveAirportByName(String airportName) throws AirportNotFoundException;
    
}
