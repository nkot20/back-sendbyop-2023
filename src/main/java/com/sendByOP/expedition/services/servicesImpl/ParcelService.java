package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.ParcelMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.services.iServices.IParcelService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.ParcelRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ParcelService implements IParcelService {
    private final ParcelRepository parcelRepository;
    private final ParcelMapper parcelMapper;
    private final BookingMapper bookingMapper;

    @Override
    public ParcelDto saveParcel(ParcelDto parcel) throws SendByOpException {
        CHeckNull.checkIntitule(parcel.getDescription());
        return parcelMapper.toDto(parcelRepository.save(parcelMapper.toEntity(parcel)));
    }

    @Override
    public void deleteParcel(ParcelDto parcel) {
        parcelRepository.delete(parcelMapper.toEntity(parcel));
    }

    @Override
    public ParcelDto findParcelById(int id) throws SendByOpException {
        return parcelMapper.toDto(
                parcelRepository.findById(id)
                        .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND))
        );
    }

    @Override
    public List<ParcelDto> findAllParcelsByBooking(BookingDto bookingId) {
        return parcelMapper.toDtoList(parcelRepository.findByIdre(bookingMapper.toEntity(bookingId)));
    }
}
