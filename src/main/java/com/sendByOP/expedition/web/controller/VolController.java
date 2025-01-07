package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.VolDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.AnnulationTrajet;
import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.entities.Vol;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.*;
import com.sendByOP.expedition.web.exceptions.vol.ImpossibleDePublierVolException;
import com.sendByOP.expedition.web.exceptions.vol.VolNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class VolController {

    @Autowired
    VolService volService;

    @Autowired
    Clientservice clientservice;

    @Autowired
    AnnulationTrajetService annulationTrajetService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    SendMailService sendMailService;

    @Autowired
    ReservationsARembourserService reservationsARembourserService;

    @GetMapping(value = "api/v1/vols")
    public ResponseEntity<?> getListeVol(){
        List<Vol> vols = (List<Vol>) volService.getAllVol();

        if(vols.isEmpty()) return new ResponseEntity<>(new ResponseMessage("Aucun vol présent"), HttpStatus.OK );

        return new ResponseEntity<>(vols, HttpStatus.OK );
    }


    //la liste de vols déja valider par l'admin avec pour état 1
    @GetMapping(value = "api/v1/vols/valid")
    public ResponseEntity<?> getListeVolValider(){

        List<Vol> vols = (List<Vol>) volService.getAllVolValid(1);

        //if(vols.isEmpty()) return new ResponseEntity<>(new ResponseMessage("Aucun vol présent"), HttpStatus.OK );

        return new ResponseEntity<>(vols, HttpStatus.OK );
    }


    //la liste de vols déja rejetté par l'admin avec pour état 2
    @GetMapping(value = "vols/reject")
    public ResponseEntity<?> getListeVolRejette(){

        List<Vol> vols = (List<Vol>) volService.getAllVolValid(2);

        if(vols.isEmpty()) throw new VolNotFoundException("Aucun vol n'existe");

        return new ResponseEntity<>(vols, HttpStatus.OK);
    }


    //la liste de vols déja valider par l'admin avec pour état 1
    @GetMapping(value = "vols/noValid")
    public ResponseEntity<?> getListeVolPasValider(){

        List<Vol> vols = (List<Vol>) volService.getAllVolValid(0);

        if(vols.isEmpty()) throw new VolNotFoundException("Aucun vol n'existe");

        vols.removeIf(vol -> vol.getAnnuler() == 1);

        return new ResponseEntity<>(vols, HttpStatus.OK);
    }

    //Publier un vol
    @PostMapping(value = "vols/saveVol")
    public ResponseEntity<?> publishFlight(@RequestBody VolEscaleDto volEscaleDTO) throws SendByOpException {

        VolDto addVol = volService.saveVolWithEscales(volEscaleDTO);

        if(addVol == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.INTERNAL_SERVER_ERROR);
        };

        return new ResponseEntity<VolDto>(addVol ,HttpStatus.CREATED);
    }

    //Chercher un vol
    @GetMapping(value = "api/v1/vols/{id}")
    public ResponseEntity<?> getVol(@PathVariable("id") int id){

        Optional<Vol> vol = volService.getVolById(id);

        if(!vol.isPresent()) return new ResponseEntity<>(new ResponseMessage("Le vol correspondant à l'id \"+id+\" n'existe pas"), HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<Vol>(vol.get() ,HttpStatus.OK);

    }

    //Annuler un vol
    @PostMapping(value = "vols/annuler/")
    public ResponseEntity<?> annulerVol(@RequestBody AnnulationTrajet annulationTrajet){
        annulationTrajet.setDate(new Date());
        Vol volUpdate = this.annulationTrajetService.annulerTrajet(annulationTrajet);

        if (volUpdate == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"),
                    HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(volUpdate, HttpStatus.OK);
    }

    //Modifier un vol
    @PutMapping(value = "vols/modifier/")
    public ResponseEntity<Vol> modifierVol(@RequestBody Vol vol){

        Vol volUpdate = volService.updateVol(vol);

        if (volUpdate == null) throw new ImpossibleDePublierVolException("Impossible de modifier le vol veuillez réssayer plus tard");

        return new ResponseEntity<Vol>(volUpdate, HttpStatus.OK);
    }

    //Valider ou rejetter un vol 1 pour valider 2 pour rejetter
    @PutMapping(value = "vols/validation/{id}")
    public ResponseEntity<?> modifierVol(@RequestBody int id, @PathVariable("id") int idVol){

        Vol vol = volService.getVolByIdVol(idVol);

        if (vol == null) {
            return new ResponseEntity<>(new ResponseMessage("Trajet inexistant"),
                    HttpStatus.NOT_FOUND);
        }

        vol.setEtatvalidation(id);

        Vol volUpdate = volService.updateVol(vol);

        if (volUpdate == null) {
            return new ResponseEntity<>(new ResponseMessage("Problème survenu"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new ResponseMessage("Validation réussie"), HttpStatus.OK);
    }


    //Nombre de vols d'un client pour déterminer s'il est débutant ou pas. pour débutant le nombre de vol est < 15
    @GetMapping(value = "api/v1/vols/nbVols/{id}")
    public ResponseEntity<?> nbVols(@PathVariable("id") String email) throws SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            return new ResponseEntity<>(volService.nbVolClient(client), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }

    //liste des vols d'un client
    @GetMapping(value = "api/v1/vols/client/{id}")
    public ResponseEntity<?> clientVol(@PathVariable("id") String email) throws SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            return new ResponseEntity<>(volService.getByIdClient(client), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }

    @PostMapping(value = "vols/annulation")
    public ResponseEntity<?> getDeatailsAnnulation(@RequestBody Vol vol) {
        return new ResponseEntity<>(annulationTrajetService.findByVol(vol), HttpStatus.OK);
    }


    //Modifier la consultation de l'annulation d'un trajet
    @PutMapping(value = "vols/annulation/update")
    public ResponseEntity<?> updateConsultation(@RequestBody AnnulationTrajet annulationTrajet) {
        AnnulationTrajet newAnnulationTrajet = annulationTrajetService.update(annulationTrajet);

        if(newAnnulationTrajet == null) return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(newAnnulationTrajet, HttpStatus.OK);
    }

}
