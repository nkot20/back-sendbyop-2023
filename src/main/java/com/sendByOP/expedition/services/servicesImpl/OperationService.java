package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.OperationMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.OperationDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.models.entities.Operation;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.OperationType;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.reponse.ResponseMessages;
import com.sendByOP.expedition.repositories.OperationRepository;
import com.sendByOP.expedition.services.iServices.IOperationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Validated
public class OperationService implements IOperationService {

    private final TypeOPerationService typeOperationService;
    private final ReservationService reservationService;
    private final OperationRepository operationRepository;
    private final BookingMapper reservationMapper;
    private final OperationMapper operationMapper;

    @Override
    public OperationDto saveOperation(OperationDto operationDto, int typeId) throws SendByOpException {
        try {
            OperationType operationType = typeOperationService.findTypeById(typeId);
            if (operationType == null) {
                throw new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND, "Operation type not found");
            }

            operationDto.setOperationTypeId(operationType.getId());
            Operation operation = operationMapper.toEntity(operationDto);
            Operation savedOperation = operationRepository.save(operation);
            log.info("Operation saved successfully with ID: {}", savedOperation.getId());
            return operationMapper.toDto(savedOperation);
        } catch (Exception e) {
            log.error("Error saving operation: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    @Override
    public OperationDto searchOperation(int id) throws SendByOpException {
        try {
            Operation operation = operationRepository.findByIdOpe(id)
                    .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND, "Operation not found"));
            return operationMapper.toDto(operation);
        } catch (SendByOpException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching operation: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    @Override
    public void deleteOperation(OperationDto operationDto) throws SendByOpException {
        try {
            if (operationDto == null) {
                throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
            }
            Operation operation = operationMapper.toEntity(operationDto);
            operationRepository.delete(operation);
            log.info("Operation deleted successfully with ID: {}", operationDto.getId());
        } catch (Exception e) {
            log.error("Error deleting operation: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    @Override
    public List<OperationDto> findOperationByType(OperationDto operationDto) throws SendByOpException {
        try {
            if (operationDto == null) {
                throw new SendByOpException(ErrorInfo.REFERENCE_RESSOURCE_REQUIRED);
            }
            Operation operation = operationMapper.toEntity(operationDto);
            List<Operation> operations = operationRepository.findByIdTypeOperation(operation);
            return operations.stream()
                    .map(operationMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding operations by type: {}", e.getMessage());
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }
    }

    @Override
    public BookingDto enregistrerDepotParExpediteur(int id) throws Exception {
        BookingDto reservationDto = reservationService.getReservation(id);
        Booking reservation = reservationMapper.toEntity(reservationDto);

        OperationType operationType = typeOperationService.findTypeById(1);

        Operation operation = Operation.builder()
            .operationDate(new Date())
            .reservation(reservation)
            .operationType(operationType)
            .build();

        operationRepository.save(operation);

        reservation.setSenderReceptionStatus(1);
        return reservationService.updateReservation(reservationDto);
    }

    @Override
    public BookingDto saveDepotParClient(int id) throws Exception {
        BookingDto reservationDto = reservationService.getReservation(id);
        Booking reservation = reservationMapper.toEntity(reservationDto);

        OperationType typeoperation = typeOperationService.findTypeById(2);

        Operation operation = Operation.builder()
            .operationDate(new Date())
            .reservation(reservation)
            .operationType(typeoperation)
            .build();

        Operation newOperation = operationRepository.save(operation);
        if (newOperation == null) {
            throw new Exception("Problème survenu lors de l'enregistrement");
        }

        reservation.setSenderReceptionStatus(1);
        BookingDto updatedReservation = reservationService.updateReservation(reservationDto);
        return updatedReservation;
    }

    @Override
    public void deleteOperation(int operationId) throws SendByOpException {
        OperationDto operationDto = searchOperation(operationId);
        if (operationDto == null) {
            throw new IllegalArgumentException(ResponseMessages.OPERATION_NOT_FOUND.getMessage());
        }
        Operation operation = operationMapper.toEntity(operationDto);
        operationRepository.delete(operation);
    }
}