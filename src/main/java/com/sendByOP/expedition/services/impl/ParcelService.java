package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.mappers.ParcelMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.repositories.ReservationRepository;
import com.sendByOP.expedition.services.iServices.IParcelService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.ParcelRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ParcelService implements IParcelService {
    private final ParcelRepository parcelRepository;
    private final ParcelMapper parcelMapper;
    private final BookingMapper bookingMapper;
    private final ReservationRepository reservationRepository;

    @Override
    public ParcelDto saveParcel(ParcelDto parcel) throws SendByOpException {
        if (parcel == null) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
        CHeckNull.checkIntitule(parcel.getDescription());
        try {
            return parcelMapper.toDto(parcelRepository.save(parcelMapper.toEntity(parcel)));
        } catch (Exception e) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public void deleteParcel(ParcelDto parcel) throws SendByOpException {
        if (parcel == null) {
            throw new SendByOpException(ErrorInfo.UNEXPECTED_ERROR);
        }
        try {
            parcelRepository.delete(parcelMapper.toEntity(parcel));
        } catch (Exception e) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public void deleteParcelById(int id) throws SendByOpException {
        ParcelDto parcel = findParcelById(id);
        deleteParcel(parcel);
    }

    @Override
    public ParcelDto findParcelById(int id) throws SendByOpException {
        return parcelMapper.toDto(
                parcelRepository.findById(id)
                        .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND))
        );
    }

    @Override
    public List<ParcelDto> findAllParcelsByBooking(BookingDto bookingId) throws SendByOpException {
        if (bookingId == null) {
            throw new SendByOpException(ErrorInfo.UNEXPECTED_ERROR);
        }
        try {
            return parcelMapper.toDtoList(parcelRepository.findByReservation(bookingMapper.toEntity(bookingId)));
        } catch (Exception e) {
            throw new SendByOpException(ErrorInfo.INTERNAL_ERROR);
        }
    }

    @Override
    public List<ParcelDto> findAllParcelsByBookingId(int bookingId) throws SendByOpException {
        BookingDto booking = bookingMapper.toDto(reservationRepository.findById(bookingId)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESOURCE_NOT_FOUND)));
        log.info("Successfully fetched booking with ID: {}", bookingId);

        return findAllParcelsByBooking(booking);
    }
}
