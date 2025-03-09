package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.ParcelMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.dto.BookingDto;
import com.sendByOP.expedition.services.iServices.IColisService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.ColisRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ColisService implements IColisService {
    private final ColisRepository colisRepository;
    private final ParcelMapper colisMapper;
    private final BookingMapper reservationMapper;

    @Override
    public ParcelDto saveColis(ParcelDto colis) throws SendByOpException {
        CHeckNull.checkIntitule(colis.getDescription());
        return colisMapper.toDto(colisRepository.save(colisMapper.toEntity(colis)));
    }

    @Override
    public void deleteColis(ParcelDto colis){
        colisRepository.delete(colisMapper.toEntity(colis));
    }

    @Override
    public ParcelDto findColis(int id) throws SendByOpException {
        return colisMapper.toDto(
                colisRepository.findById(id)
                        .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND))
        );
    }

    @Override
    public List<ParcelDto> findAllColisByForReservation(BookingDto idRe){
        return colisMapper.toDtoList(colisRepository.findByIdre(reservationMapper.toEntity(idRe)));
    }
}
