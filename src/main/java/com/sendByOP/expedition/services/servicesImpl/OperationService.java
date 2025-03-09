package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.OperationMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.OperationDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.entities.Operation;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.OperationType;
import com.sendByOP.expedition.reponse.ResponseMessages;
import com.sendByOP.expedition.repositories.OperationRepository;
import com.sendByOP.expedition.services.iServices.IOperationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OperationService implements IOperationService {

    private final TypeOPerationService typeOperationService;
    private final ReservationService reservationService;
    private final OperationRepository operationRepository;
    private final BookingMapper reservationMapper;
    private final OperationMapper operationMapper;

    @Override
    public OperationDto saveOperation(OperationDto operationDto, int typeId) {
        OperationType typeoperation = typeOperationService.findTypeById(typeId);
        if (typeoperation == null) {
            throw new IllegalArgumentException(ResponseMessages
                    .TYPE_OPERATION_NOT_FOUND.getMessage());
        }

        operationDto.setIdTypeOperation(typeoperation.getIdtypeoperation());
        Operation operation = operationMapper.toEntity(operationDto);
        Operation savedOperation = operationRepository.save(operation);
        return operationMapper.toDto(savedOperation);
    }

    @Override
    public OperationDto searchOperation(int id) {
        Operation operation = operationRepository.findByIdOpe(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(ResponseMessages
                                .OPERATION_NOT_FOUND.getMessage()));
        return operationMapper.toDto(operation);
    }

    @Override
    public void deleteOperation(OperationDto operationDto) {
        Operation operation = operationMapper.toEntity(operationDto);
        operationRepository.delete(operation);
    }

    @Override
    public List<OperationDto> findOperationByType(OperationDto operationDto) {
        Operation operation = operationMapper.toEntity(operationDto);
        List<Operation> operations = operationRepository.findByIdTypeOperation(operation);
        return operations.stream()
                .map(operationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto enregistrerDepotParExpediteur(int id) throws Exception {
        BookingDto reservationDto = reservationService.getReservation(id);
        Booking reservation = reservationMapper.toEntity(reservationDto);

        Operation operation = new Operation();
        OperationType typeoperation = typeOperationService.findTypeById(1);

        operation.setDate(new Date());
        operation.setIdReser(reservation);
        operation.setIdTypeOperation(typeoperation);

        Operation newOperation = operationRepository.save(operation);
        if (newOperation == null) {
            throw new Exception("Problème survenu lors de l'enregistrement");
        }

        reservation.setEtatReceptionExp(1);
        BookingDto updatedReservation = reservationService.updateReservation(
                reservationDto);
        return updatedReservation;
    }

    @Override
    public BookingDto saveDepotParClient(int id) throws Exception {
        BookingDto reservationDto = reservationService.getReservation(id);
        Booking reservation = reservationMapper.toEntity(reservationDto);

        OperationType typeoperation = typeOperationService.findTypeById(2);

        Operation operation = new Operation();
        operation.setDate(new Date());
        operation.setIdReser(reservation);
        operation.setIdTypeOperation(typeoperation);

        Operation newOperation = operationRepository.save(operation);
        if (newOperation == null) {
            throw new Exception("Problème survenu lors de l'enregistrement");
        }

        reservation.setEtatReceptionClient(1);
        BookingDto updatedReservation = reservationService.updateReservation(reservationDto);
        return updatedReservation;
    }

    @Override
    public void deleteOperation(int operationId) {
        OperationDto operationDto = searchOperation(operationId);
        if (operationDto == null) {
            throw new IllegalArgumentException(ResponseMessages.OPERATION_NOT_FOUND.getMessage());
        }
        Operation operation = operationMapper.toEntity(operationDto);
        operationRepository.delete(operation);
    }
}