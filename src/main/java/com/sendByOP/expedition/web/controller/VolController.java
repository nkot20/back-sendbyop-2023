package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.models.dto.CustomerDto;
import com.sendByOP.expedition.models.dto.FlightDto;
import com.sendByOP.expedition.models.dto.VolEscaleDto;
import com.sendByOP.expedition.models.entities.CancellationTrip;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.iServices.IVolService;
import com.sendByOP.expedition.services.servicesImpl.*;
import com.sendByOP.expedition.web.exceptions.vol.ImpossibleDePublierVolException;
import com.sendByOP.expedition.web.exceptions.vol.VolNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/trips")
public class VolController {

    private final IVolService volService;
    private final CustomerService clientservice;
    private final AnnulationTrajetService annulationTrajetService;

    @GetMapping
    public ResponseEntity<?> getListeVol(){
        List<FlightDto> vols = (List<FlightDto>) volService.getAllVol();

        if(vols.isEmpty()) return new ResponseEntity<>(new ResponseMessage("Aucun vol présent"), HttpStatus.OK );

        return new ResponseEntity<>(vols, HttpStatus.OK );
    }


    //la liste de vols déja valider par l'admin avec pour état 1
    @GetMapping(value = "/valid")
    public ResponseEntity<?> getListeVolValider(){

        List<FlightDto> vols = (List<FlightDto>) volService.getAllVolValid(1);

        //if(vols.isEmpty()) return new ResponseEntity<>(new ResponseMessage("Aucun vol présent"), HttpStatus.OK );

        return new ResponseEntity<>(vols, HttpStatus.OK );
    }


    //la liste de vols déja rejetté par l'admin avec pour état 2
    @GetMapping(value = "/reject")
    public ResponseEntity<?> getListeVolRejette(){

        List<Flight> vols = (List<Flight>) volService.getAllVolValid(2);

        if(vols.isEmpty()) throw new VolNotFoundException("Aucun vol n'existe");

        return new ResponseEntity<>(vols, HttpStatus.OK);
    }


    //la liste de vols déja valider par l'admin avec pour état 1
    @GetMapping(value = "/noValid")
    public ResponseEntity<?> getListeVolPasValider(){

        List<FlightDto> vols = (List<FlightDto>) volService.getAllVolValid(0);

        if(vols.isEmpty()) throw new VolNotFoundException("Aucun vol n'existe");

        vols.removeIf(vol -> vol.getAnnuler() == 1);

        return new ResponseEntity<>(vols, HttpStatus.OK);
    }

    //Publier un vol
    @PostMapping(value = "/saveVol")
    public ResponseEntity<?> publishFlight(@RequestBody VolEscaleDto volEscaleDTO) throws SendByOpException {

        FlightDto addVol = volService.saveVolWithEscales(volEscaleDTO);

        if(addVol == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.INTERNAL_SERVER_ERROR);
        };

        return new ResponseEntity<FlightDto>(addVol ,HttpStatus.CREATED);
    }

    //Chercher un vol
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getVol(@PathVariable("id") int id){

        FlightDto vol = volService.getVolById(id);
        return new ResponseEntity<FlightDto>(vol ,HttpStatus.OK);

    }

    //Annuler un vol
    @PostMapping(value = "/annuler/")
    public ResponseEntity<?> annulerVol(@RequestBody CancellationTrip annulationTrajet){
        annulationTrajet.setDate(new Date());
        Flight volUpdate = this.annulationTrajetService.annulerTrajet(annulationTrajet);

        if (volUpdate == null) {
            return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"),
                    HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(volUpdate, HttpStatus.OK);
    }

    //Modifier un vol
    @PutMapping(value = "/modifier/")
    public ResponseEntity<FlightDto> modifierVol(@RequestBody FlightDto vol){

        FlightDto volUpdate = volService.updateVol(vol);

        if (volUpdate == null) throw new ImpossibleDePublierVolException("Impossible de modifier le vol veuillez réssayer plus tard");

        return new ResponseEntity<FlightDto>(volUpdate, HttpStatus.OK);
    }

    //Valider ou rejetter un vol 1 pour valider 2 pour rejetter
    @PutMapping(value = "/validation/{id}")
    public ResponseEntity<?> modifierVol(@RequestBody int id, @PathVariable("id") int idVol){

        FlightDto vol = volService.getVolByIdVol(idVol);

        vol.setEtatvalidation(id);

        FlightDto volUpdate = volService.updateVol(vol);

        if (volUpdate == null) {
            return new ResponseEntity<>(new ResponseMessage("Problème survenu"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new ResponseMessage("Validation réussie"), HttpStatus.OK);
    }


    //Nombre de vols d'un client pour déterminer s'il est débutant ou pas. pour débutant le nombre de vol est < 15
    @GetMapping(value = "/nbVols/{id}")
    public ResponseEntity<?> nbVols(@PathVariable("id") String email) throws SendByOpException {
        try {
            CustomerDto client = clientservice.getCustomerByEmail(email);
            return new ResponseEntity<>(volService.nbVolClient(client), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }

    //liste des vols d'un client
    @GetMapping(value = "/client/{id}")
    public ResponseEntity<?> clientVol(@PathVariable("id") String email) throws SendByOpException {
        try {
            CustomerDto client = clientservice.getCustomerByEmail(email);
            return new ResponseEntity<>(volService.getByIdClient(client), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());

        }
    }

    @PostMapping(value = "/annulation")
    public ResponseEntity<?> getDeatailsAnnulation(@RequestBody Flight vol) {
        return new ResponseEntity<>(annulationTrajetService.findByVol(vol), HttpStatus.OK);
    }


    //Modifier la consultation de l'annulation d'un trajet
    @PutMapping(value = "/annulation/update")
    public ResponseEntity<?> updateConsultation(@RequestBody CancellationTrip annulationTrajet) {
        CancellationTrip newAnnulationTrajet = annulationTrajetService.update(annulationTrajet);

        if(newAnnulationTrajet == null) return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(newAnnulationTrajet, HttpStatus.OK);
    }

}
