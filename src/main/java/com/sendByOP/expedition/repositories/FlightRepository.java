package com.sendByOP.expedition.repositories;

import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.models.entities.Flight;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends CrudRepository<Flight, Integer> {

    public List<Flight> findAllByOrderByDepartureDateDesc();

    public List<Flight> findByCustomerOrderByPublicationDateDesc(Customer customerId);

    public List<Flight> findByValidationStatus(int status);

    public Optional<Flight> findByFlightId(int id);

}
