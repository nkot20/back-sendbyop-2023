package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Operation;
import com.sendByOP.expedition.models.entities.Reservation;
import com.sendByOP.expedition.models.entities.Typeoperation;
import com.sendByOP.expedition.repositories.OperationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OperationService {
    @Autowired
    TypeOPerationService typeOPerationService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    OperationRepository operationRepository;

    public Operation saveOperation(Operation operation){
        return operationRepository.save(operation);
    }

    public Operation searchOperation(int id){
        return operationRepository.findByIdOpe(id).get();
    }

    public void deleteOperation(Operation operation){
        operationRepository.delete(operation);
    }

    public List<Operation> findOperationByType(Operation operation){
        return operationRepository.findByIdTypeOperation(operation);
    }

    public Reservation enregistrerDepotParExpdeiteur (int id) throws Exception{
        Reservation reservation = reservationService.getReservation(id);
        Operation operation = new Operation();
        Typeoperation typeoperation = typeOPerationService.findTypeById(1);

        operation.setDate(new Date());

        operation.setIdReser(reservation);

        operation.setIdTypeOperation(typeoperation);

        Operation newOperation = saveOperation(operation);

        if (newOperation == null) throw new Exception("Problème survenu lors de l'enregistrement");

        reservation.setEtatReceptionExp(1);

        Reservation newReservation = reservationService.updateReservation(reservation);
        return newReservation;
    }

    public Reservation enregistrerDepotParClient(int id) throws Exception {
        Reservation reservation = reservationService.getReservation(id);

        Typeoperation typeoperation = typeOPerationService.findTypeById(2);

        Operation operation = new Operation();

        operation.setDate(new Date());

        operation.setIdReser(reservation);

        operation.setIdTypeOperation(typeoperation);

        Operation newOperation = saveOperation(operation);

        if (newOperation == null) throw new Exception("Problème survenu lors de l'enregistrement");

        reservation.setEtatReceptionClient(1);

        Reservation newReservation = reservationService.updateReservation(reservation);

        return newReservation;
    }
}
