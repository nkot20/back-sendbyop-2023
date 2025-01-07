package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.*;
import com.sendByOP.expedition.services.iServices.IColisService;
import com.sendByOP.expedition.services.iServices.IReservationService;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.repositories.FactureRepository;
import com.sendByOP.expedition.repositories.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PaiementService {

    @Autowired
    IColisService colisService;

    @Autowired
    IReservationService reservationService;

    @Autowired
    VolService volService;

    @Autowired
    FactureRepository factureRepository;

    @Autowired
    PaiementRepository paiementRepository;

    public Reservation calculMontantFacture(int idRe) throws SendByOpException {
        // On recherche la réservation et on vérifis si elle n'a pas déja été payée
        Reservation reservation = reservationService.getReservation(idRe);
        if(reservation.getStatutPayement() != 0){
            //On recherche le vol de la réservation
            Vol vol = reservation.getVol();

            //On vérifie s'il y a encore des kilos diponibles sur la réservation
            if(vol.getNbkilo() != 0) {
                // Recherche du montant du vol
                int montantRe = vol.getMontantkilo();

                //System.out.println(montantRe);
                //Liste des colis de la réservation
                List<Colis> colisList = colisService.findAllColisByForReservation(reservation);

                float montantTotal = 0;

                // Calcul du montant total de la réservation
                for (Colis colis: colisList) {
                    montantTotal = montantTotal + colis.getKilo()*montantRe;
                }

                //Float montantPaiement = montantTotal - (10*montantTotal)/100;

                //System.out.println(montantTotal);
                //Génération de la facture
                Facture facture = new Facture();
                facture.setMontantfac(montantTotal + 3);
                facture.setIdRe(reservation);
                Facture newFacture = factureRepository.save(facture);

                Paiement paiement = new Paiement();

                paiement.setMontant((double) (montantTotal + 3));
                paiement.setDate(new Date());
                paiement.setClient(reservation.getReserveur());
                TypeVirement typeVirement = new TypeVirement();
                typeVirement.setIdType(3);
                paiement.setTypePaiment(typeVirement);
                save(paiement);
                //Calcul du pourcentage de sendByOp


                //Envoi de l'argent à l'expéditeur

                //Envoi de l'argent à SendByOp
                reservation.setStatutPayement(1);
                Reservation newReservation = reservationService.updateReservation(reservation);

                return newReservation;
            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    public Paiement save(Paiement paiement) {
        return paiementRepository.save(paiement);
    }

    public List<Paiement> getAll() {
        return paiementRepository.findAll();
    }

    public List<Paiement> getByClient(Client client) {
        return paiementRepository.findByClient(client);
    }
}
