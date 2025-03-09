package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.mappers.CustomerMapper;
import com.sendByOP.expedition.mappers.ReceiverMapper;
import com.sendByOP.expedition.mappers.BookingMapper;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.ReservationRepository;
import com.sendByOP.expedition.utils.CHeckNull;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService implements IReservationService {
    private final ReservationRepository reservationRepository;
    private final ColisService colisService;
    private final ReceveurService receveurService;
    private final SendMailService sendEmailService;
    private final RefusService refusService;
    private final BookingMapper reservationMapper;
    private final ReceiverMapper receiverMapper;
    private final CustomerMapper customerMapper;

    @Override
    public BookingDto saveReservation(BookingDto reservation) throws SendByOpException {
       CHeckNull.checkNumero(reservation.getReserveur().getIdp());
        return reservationMapper
                .toDto(reservationRepository.save(reservationMapper.toEntity(reservation)));
    }

    @Override
    public BookingDto saveReservationWithColis(BookingDto reservation) throws SendByOpException {
        reservation.setStatutPayement(0);
        reservation.setEtatReceptionExp(0);
        reservation.setAvisClient("");
        reservation.setAvisExpediteur("");
        reservation.setEtatReceptionClient(0);
        reservation.setAnnuler(0);
        reservation.setStastutPaimentTransporteur(0);
        reservation.setStatutReExpe(0);
        ReceiverDto receveur = receveurService.save(receiverMapper.toEntity(reservation.getReceveur()));
        reservation.setReceveur(receveur);
        List<ParcelDto> colisList = new ArrayList<ParcelDto>();

        reservation.getColisList().forEach(colis -> {
            colisList.add(colis);
        });
        reservation.setColisList(colisList);
        BookingDto newReservation = saveReservation(reservation);


        reservation.getColisList().forEach(colis -> {
            colis.setIdre(newReservation);
            try {
                colisService.saveColis(colis);
            } catch (SendByOpException e) {
                e.printStackTrace();
            }
        });
        return newReservation;
    }

    @Override
    public BookingDto updateReservation(BookingDto reservation) throws SendByOpException {
        return saveReservation(reservation);
    }

    @Override
    public void deleteReservation(int id){
        reservationRepository.deleteById(id);
    }

    @Override
    public BookingDto getReservation(int id) throws SendByOpException {
        return reservationMapper.toDto(reservationRepository.findByIdRe(id)
                .orElseThrow(() -> new SendByOpException(ErrorInfo.RESSOURCE_NOT_FOUND)));
    }

    @Override
    public List<BookingDto> reservationList(){
        return reservationMapper.toDtoList(reservationRepository.findAllByOrderByDatereDesc());
    }

    @Override
    public List<BookingDto> clientDestinatorReservationList(CustomerDto idClient){
        return reservationMapper.toDtoList(reservationRepository.findByReserveurOrderByDatereDesc(
                customerMapper.toEntity(idClient)
        ));
    }

    @Override
    public List<BookingDto> getReservationByDate(Date date) {
        return reservationMapper.toDtoList(reservationRepository.findByDatere(date));
    }

    @Override
    public BookingDto refuserReservation(BookingDto reservation, RejectionDto refus)
            throws MessagingException, UnsupportedEncodingException, SendByOpException {


        reservation.setStatutReExpe(2);

        BookingDto newReservation = updateReservation(reservation);
        refus.setIdRe(reservation.getIdRe());
        if (newReservation != null){
            refusService.saveRefus(refus);
        } else {
            return null;
        }

        String content = "Bonjour [[name]],<br>"

                + "La réservation que vous avez éffectué a été rejettée par l'expéditeur"
                + "Cordialement,<br>"
                + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>";

        sendEmailService.simpleHtmlMessage(newReservation.getVol().getIdclient(), content, "Validation de réservation");
        return newReservation;
    }

}
