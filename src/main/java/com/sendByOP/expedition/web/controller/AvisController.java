package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Review;
import com.sendByOP.expedition.reponse.ResponseMessages;
import com.sendByOP.expedition.services.iServices.IAvisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opinion")
@RequiredArgsConstructor
public class AvisController {

    private final IAvisService avisService;

    @PostMapping("/save")
    public ResponseEntity<?> saveOpinion(@RequestBody Review avis) {
        Review newOpinion = avisService.saveOpinion(avis);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseEntity<>(ResponseMessages.OPINION_SAVED_SUCCESSFULLY.getMessage(), HttpStatus.CREATED));
    }

    @GetMapping("/transporteur/{id}")
    public ResponseEntity<?> getByTransporter(@PathVariable("id") int idTransporter) {
        List<Review> avisList = avisService.getByTransporter(idTransporter);
        if (avisList.isEmpty()) {
            return ResponseEntity.ok(ResponseMessages.NO_OPINIONS_FOR_TRANSPORTER.getMessage());
        }
        return ResponseEntity.ok(avisList);
    }

    @GetMapping("/expediteur/{id}")
    public ResponseEntity<?> getByExpeditor(@PathVariable("id") int idExpeditor) {
        List<Review> avisList = avisService.getByExpeditor(idExpeditor);
        if (avisList.isEmpty()) {
            return ResponseEntity.ok(ResponseMessages.NO_OPINIONS_FOR_EXPEDITOR.getMessage());
        }
        return ResponseEntity.ok(avisList);
    }
}
