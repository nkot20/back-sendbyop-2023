package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.ReviewMapper;
import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.models.dto.ReviewDto;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.models.entities.Customer;
import com.sendByOP.expedition.repositories.AvisRepository;
import com.sendByOP.expedition.services.iServices.IAvisService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AvisService implements IAvisService {

    private final CustomerService clientService;
    private final AvisRepository avisRepository;
    private final ReviewMapper avisMapper;
    private final CustomerMapper customerMapper;

    @Override
    public ReviewDto saveOpinion(ReviewDto avisDto) {
        Review avis = avisMapper.toEntity(avisDto);

        Review savedAvis = avisRepository.save(avis);

        return avisMapper.toDto(savedAvis);
    }

    @Override
    public List<ReviewDto> getByTransporter(int transporterId) {
        CustomerDto transporterDto = clientService.getClientById(transporterId);

        if (transporterDto == null) {
            throw new IllegalArgumentException("Transporteur introuvable");
        }

        Customer transporter = customerMapper.toEntity(transporterDto);

        List<Review> avisList = avisRepository.findByTransporteurOrderByDateAsc(transporter);

        return avisList.stream()
                .map(avisMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getByExpeditor(int expeditorId) {
        CustomerDto expeditorDto = clientService.getClientById(expeditorId);

        if (expeditorDto == null) {
            throw new IllegalArgumentException("Expéditeur introuvable");
        }

        Customer expeditor = customerMapper.toEntity(expeditorDto);

        List<Review> avisList = avisRepository.findByExpediteurOrderByDateAsc(expeditor);

        return avisList.stream()
                .map(avisMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> findByTransporteur(CustomerDto transporteurDto) {
        Customer transporteur = customerMapper.toEntity(transporteurDto);;

        List<Review> avisList = avisRepository.findByTransporteurOrderByDateAsc(transporteur);

        return avisList.stream()
                .map(avisMapper::toDto)
                .collect(Collectors.toList());
    }
}