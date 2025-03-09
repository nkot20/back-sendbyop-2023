package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.mappers.FactureMapper;
import com.sendByOP.expedition.models.dto.*;
import com.sendByOP.expedition.models.dto.InvoiceDto;
import com.sendByOP.expedition.models.dto.ParcelDto;
import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.services.iServices.IColisService;
import com.sendByOP.expedition.services.iServices.IPaymentService;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.FactureRepository;
import com.sendByOP.expedition.repositories.PaiementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IColisService colisService;
    private final IReservationService reservationService;
    private final VolService volService;
    private final FactureRepository factureRepository;
    private final PaiementRepository paiementRepository;
    private final CustomerService clientservice;
    private final FactureMapper factureMapper;

    @Override
    public BookingDto calculMontantFacture(int idRe) throws SendByOpException {
        // On recherche la réservation et on vérifis si elle n'a pas déja été payée
        BookingDto reservation = reservationService.getReservation(idRe);
        if(reservation.getStatutPayement() != 0){
            //On recherche le vol de la réservation
            FlightDto vol = reservation.getVol();

            //On vérifie s'il y a encore des kilos diponibles sur la réservation
            if(vol.getNbkilo() != 0) {
                // Recherche du montant du vol
                int montantRe = vol.getMontantkilo();

                //System.out.println(montantRe);
                //Liste des colis de la réservation
                List<ParcelDto> colisList = colisService.findAllColisByForReservation(reservation);

                float montantTotal = 0;

                // Calcul du montant total de la réservation
                for (ParcelDto colis: colisList) {
                    montantTotal = montantTotal + colis.getKilo()*montantRe;
                }

                //Float montantPaiement = montantTotal - (10*montantTotal)/100;

                //System.out.println(montantTotal);
                //Génération de la facture
                InvoiceDto factureDto = InvoiceDto.builder()
                        .montantfac(montantTotal + 3)
                        .idRe(reservation.getIdRe())
                        .build();
                com.sendByOP.expedition.models.entities.Invoice newFacture = factureRepository.save(factureMapper.toEntity(factureDto));

                PaymentDto paiement = PaymentDto.builder()
                        .montant(Double.valueOf(montantTotal+3))
                        .date(new Date())
                        .clientId()
                        .build();

                paiement.setMontant((double) (montantTotal + 3));
                paiement.setDate(new Date());
                paiement.setClient(reservation.getReserveur());
                PaymentType typeVirement = new PaymentType();
                typeVirement.setIdType(3);
                paiement.setTypePaiment(typeVirement);
                save(paiement);
                //Calcul du pourcentage de sendByOp


                //Envoi de l'argent à l'expéditeur

                //Envoi de l'argent à SendByOp
                reservation.setStatutPayement(1);
                Booking newReservation = reservationService.updateReservation(reservation);

                return newReservation;
            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    @Override
    public Payment save(Payment paiement) {
        return paiementRepository.save(paiement);
    }

    @Override
    public List<Payment> getAll() {
        return paiementRepository.findAll();
    }

    @Override
    public List<Payment> getPaymentsByClient(String email) throws SendByOpException {
        CustomerDto client = clientservice.getCustomerByEmail(email);
        if (client == null) {
            throw new IllegalArgumentException(ErrorInfo.RESSOURCE_NOT_FOUND.getMessage());
        }
        return fetchPaymentsByClient(client);
    }

    private List<Payment> fetchPaymentsByClient(CustomerDto client) {
        return paiementRepository.findByClient(client);
    }

    @Override
    public Booking processPayment(int reservationId, Payment paiement) throws SendByOpException {
        Booking reservation = reservationService.getReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException(ErrorInfo.RESSOURCE_NOT_FOUND.getMessage());
        }

        // Logique métier pour calculer le montant et mettre à jour la réservation
        reservation = calculMontantFacture(reservationId);
        return reservation;
    }
}
