package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.entities.Client;
import com.sendByOP.expedition.models.dto.EmailDto;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.models.entities.VerifyToken;
import com.sendByOP.expedition.services.iServices.IClientServivce;
import com.sendByOP.expedition.services.iServices.IUserService;
import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;
import com.sendByOP.expedition.reponse.ResponseMessage;
import com.sendByOP.expedition.services.servicesImpl.*;
import com.sendByOP.expedition.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.List;


@RestController
public class RegisterController {
    public static final String apiBase = "api/v1/";
    @Autowired
    IClientServivce clientservice;

    @Autowired
    IUserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleService roleService;

    @Autowired
    SendMailService sendMailService;

    @Autowired
    VerifyTokenService verifyTokenService;


    @GetMapping(value = "clients")
    public List<Client> listeClient(){
        return clientservice.getListClient();
    }



    @PostMapping(value = ""+apiBase+"customer/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Client client) throws MessagingException, UnsupportedEncodingException, SendByOpException {
        try {
            if (clientservice.customerIsExist(client.getEmail())) throw new SendByOpException("Adresse email déja utilisée!", HttpStatus.BAD_REQUEST);

            Client newClient = clientservice.saveClient(client);
            if (newClient == null) {
                throw new SendByOpException("Un problème est survenu veuillez réessayer!",HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                VerifyToken verifyToken = verifyTokenService.save(client.getEmail());
                String content = "Bonjour [[name]],<br>"
                        + "Nous devons vérifier votre adresse e-mail et numéro de téléphone avant que vous puissez accéder à <br>"
                        + "<h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>"
                        + "Vérifier votre adresse e-mail"
                        + "<h3><a href=\"[[URL]]\" target=\"_self\">Cliquer ici</a></h3>"
                        + "Ce lien vas expiré dans 24h <br>"
                        + "Cordialement,<br>"
                        + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>"
                        + "Email envoyé automatiquement<br>";
                try {
                    sendMailService.sendVerificationEmail(client, "http://localhost:4200/verification", verifyToken.getToken(), "/verify?code=", "Validation de compte", content);
                } catch (MessagingException e) {
                    return new ResponseEntity<>(new ResponseMessage("Une erreur lors de l'envoi du mail de confirmation de compte"), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                return new ResponseEntity<>(new ResponseMessage("Inscription enregistrée veuillez patientez que votre compte soit valider par l'administrateur vous allez recevoir un mail de validation"), HttpStatus.OK);
            }
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }

    @PutMapping("custumer/update")
    public ResponseEntity<?> updateCustomer(@RequestBody Client client) throws SendByOpException {
        try {
            Client newClient = clientservice.updateClient(client);
            if (newClient.equals(null)) throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
            return new ResponseEntity<>(newClient,HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }

    @PostMapping("api/v1/resend/email/{email}")
    public ResponseEntity<?> resendEmail(@PathVariable("email") String email) throws MessagingException, UnsupportedEncodingException {

        try {
            Client client = clientservice.getCustomerByEmail(email);
            VerifyToken verifyToken = verifyTokenService.save(email);
            String content = "Bonjour [[name]],<br>"
                    + "Nous devons vérifier votre adresse e-mail et numéro de téléphone avant que vous puissez accéder à <br>"
                    +"<h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>"
                    + "Vérifier votre adresse e-mail"
                    + "<h3><a href=\"[[URL]]\" target=\"_self\">Cliquer ici</a></h3>"
                    + "Ce lien vas expiré dans 24h <br>"
                    + "Cordialement,<br>"
                    + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>"
                    + "Email envoyé automatiquement<br>";
            sendMailService.sendVerificationEmail(client, "http://localhost:4200/verification", verifyToken.getToken(), "/verify?code=", "Validation de compte", content);
            return new ResponseEntity<>(verifyToken, HttpStatus.OK);
        }catch (MessagingException | SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("api/v1/verify/getcustomer/{token}")
    public ResponseEntity<?> getCustomerByToken(@PathVariable("token") String token) throws SendByOpException {
        try {
            String result = verifyTokenService.verifyToken(token);

            VerifyToken verifyToken = verifyTokenService.getByTokent(token);
            Client client = clientservice.getCustomerByEmail(verifyToken.getEmail());
            return new ResponseEntity<>(client, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping("api/v1/verify/email/{token}")
    public ResponseEntity<?> verifyEmail(@PathVariable("token") String token) throws SendByOpException {
        try {
            String result = verifyTokenService.verifyToken(token);

            if(result == AppConstants.TOKEN_EXPIRED) throw new SendByOpException("Lien expiréd", HttpStatus.BAD_REQUEST);
            if(result == AppConstants.TOKEN_INVALID) throw new SendByOpException(AppConstants.TOKEN_INVALID, HttpStatus.BAD_REQUEST);
            if(result == AppConstants.TOKEN_VALID) {
                VerifyToken verifyToken = verifyTokenService.getByTokent(token);
                Client client = clientservice.getCustomerByEmail(verifyToken.getEmail());
                client.setValidEmail(1);
                Client newClient = clientservice.saveClient(client);
                if(newClient == null) {
                    return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return new ResponseEntity<>(new ResponseMessage(AppConstants.TOKEN_VALID), HttpStatus.OK);
            }
            throw new SendByOpException(ErrorInfo.INTRERNAL_ERROR);
        }catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }


    }


    //***** mot de passe ****//
    //envoi de l'email du mot de passe oublié
    @Transactional
    @PostMapping("api/v1/forgotpw/sendemail/")
    public ResponseEntity<?> sendEmailToForgotPw(@RequestBody String email) throws MessagingException, UnsupportedEncodingException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            if(client == null) {
                throw new SendByOpException("Aucun utilisateur ne correspond à cette adresse email", HttpStatus.NOT_FOUND);
            }
            String content = "Bonjour [[name]],<br>"

                    + "Cliquer ici pour changer de mot de passe"
                    + "<h3><a href=\"[[URL]]\" target=\"_self\">Cliquer ici</a></h3>"
                    + "Ce lien vas expiré dans 24h <br>"
                    + "Cordialement,<br>"
                    + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>"
                    + "Email envoyé automatiquement<br>";
            VerifyToken verifyToken = verifyTokenService.save(email);
            try {
                sendMailService.sendVerificationEmail(client, "http://localhost:4200/forgot-pw", verifyToken.getToken(), "/changepw?code=", "Changer de mot de passe", content);
            }catch (MessagingException e) {
                return new ResponseEntity<>(new ResponseMessage("Une erreur lors de l'envoi du mail de confirmation de compte"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(verifyToken, HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping("api/v1/verify/forgot-pw/verif-token/")
    public ResponseEntity<?> verifyTokenToForgotPw(@RequestBody String token){

        String result = verifyTokenService.verifyToken(token);

        if(result == AppConstants.TOKEN_EXPIRED) {
            return new ResponseEntity<>(new ResponseMessage("Lien expiréd"), HttpStatus.OK);
        }
        if(result == AppConstants.TOKEN_INVALID) {
            return new ResponseEntity<>(new ResponseMessage(AppConstants.TOKEN_INVALID), HttpStatus.OK);
        }
        if(result == AppConstants.TOKEN_VALID) {
            return new ResponseEntity<>(new ResponseMessage(AppConstants.TOKEN_VALID), HttpStatus.OK);
        }

        return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping("api/v1/verify/forgot-pw/change/{email}")
    public ResponseEntity<?> changePw(@PathVariable("email") String email, @RequestBody String pw) throws SendByOpException {
        try {
            User user = userService.findByEmail(email);

            user.setPw(encoder.encode(pw));

            User newUser = userService.saveUser(user);

            if (newUser == null) {
                return new ResponseEntity<>(new ResponseMessage("Un problème est survenu veuillez réessayer!"),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return new ResponseEntity<>(new ResponseMessage("Mot de passe modifié avec success !"),
                    HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }


    @GetMapping("api/v1/verify/forgot-pw/change/getemail/{code}")
    public ResponseEntity<?> getemail(@PathVariable("code") String code) {
        String email = verifyTokenService.getByTokent(code).getEmail();

        return new ResponseEntity<>(new ResponseMessage(email),
                HttpStatus.OK);
    }

    //***** mot de passe ****//


    //Récupérer l'état d'inscription qui sera utiliser lors de la connexion
    @PostMapping(value = apiBase+"customer/etatinscription")
    public ResponseEntity<?> findCustomer(@RequestBody String email) throws SendByOpException {

        try {
            Client client = clientservice.getCustomerByEmail(email);
            return new ResponseEntity<>(client,
                    HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    //valider l'inscription d'un client 1 pour valider
    @PostMapping(value = "/register/valider/inscription")
    public ResponseEntity<?> validerInscription(@RequestBody String email) throws SendByOpException{
       try {
           Client newClient = clientservice.getCustomerByEmail(email);

           //Vérification de l'état d'inscription
           if(newClient.getEtatInscription() == 1){
               return new ResponseEntity<>(new ResponseMessage("L'inscription a déja été validée!"),
                       HttpStatus.OK);
           }

           newClient.setPw(null);
           newClient.setEtatInscription(1);
           Client updateClient = clientservice.updateClient(newClient);

           if (updateClient == null) return new ResponseEntity<>(new ResponseMessage("Un problème est survenu"), HttpStatus.INTERNAL_SERVER_ERROR);
           EmailDto newemail = new EmailDto();
           newemail.setTo(email);
           newemail.setTopic("Compte valider");
           newemail.setBody("Bonjour Mr/Mme "+newClient.getNom()+" Votre compte a été valider par l'admin veuillez vous connecter si votre adresse email et mot de passe ont été vérifié");
           sendMailService.sendEmail(newemail);

           return new ResponseEntity<>(new ResponseMessage("Validation réussie!"), HttpStatus.OK);
       } catch (SendByOpException e) {
           return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
       }
    }

    //valider l'inscription d'un client 1 pour valider
    @PostMapping(value = "/register/rejetter/inscription")
    public ResponseEntity<?> rejetterInscription(@RequestBody String email){
        try {
            Client newClient = clientservice.getCustomerByEmail(email);
            //Vérification de l'état d'inscription
            if(newClient.getEtatInscription() == 1){
                return new ResponseEntity<>(new ResponseMessage("L'inscription a déja été validée!"),
                        HttpStatus.OK);
            }

            newClient.setPw(null);
            newClient.setEtatInscription(1);

            EmailDto newemail = new EmailDto();
            newemail.setTo(email);
            newemail.setTopic("Inscription rejettée");
            newemail.setBody("Bonjour Mr/Mme "+newClient.getNom()+" Votre compte a été rejetter par l'admin inscrivez-vous à nouveau et entrer des informations correctes");
            sendMailService.sendEmail(newemail);

            clientservice.deleteClient(newClient);

            User user = userService.findByEmail(email);
            userService.deleteuser(user);
            return new ResponseEntity<>(new ResponseMessage("Suppresion"), HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    //Liste des clients
    @GetMapping(value = "api/v1/client/list/")
    public ResponseEntity<?> getNbRegister(){
        return new ResponseEntity<>(clientservice.getAllRegister(), HttpStatus.OK);
    }

    @PostMapping(value = "api/v1/customer/details")
    public ResponseEntity<?> getDetailsCustomer(@RequestBody String email) throws SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            return new ResponseEntity<>(client,
                    HttpStatus.OK);
        }catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), e.getHttpStatus());
        }
    }

    @PostMapping(value = apiBase+"customer/details-any")
    public ResponseEntity<?> getDetailsCustomerWithMoveAnyDetails(@RequestBody String email) throws SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmailRemoveAnyDetails(email);
            return new ResponseEntity<>(client,
                    HttpStatus.OK);
        } catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }

    @GetMapping(value = "api/v1/customer/details-any/{id}")
    public ResponseEntity<?> getDetailsCustomerWithMoveAnyDetailsWithId(@PathVariable int id) throws SendByOpException {
      try {
          Client client = clientservice.getCustomerById(id);
          return new ResponseEntity<>(client,
                  HttpStatus.OK);
      } catch (SendByOpException e) {
          return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
      }
    }

    @PostMapping(value = "api/v1/customer/validate/email")
    public ResponseEntity<?> validerEmail(@RequestBody String email) throws SendByOpException{
       try {
           Client client = clientservice.getCustomerByEmail(email);
           //Vérification de l'état d'inscription
           if(client.getValidEmail() == 1){
               return new ResponseEntity<>(new ResponseMessage("L'email est déja validé!"),
                       HttpStatus.NOT_FOUND);
           }

           client.setValidEmail(1);
           Client newClient = clientservice.updateClient(client);
           return new ResponseEntity<>(newClient, HttpStatus.OK);
       } catch (SendByOpException e) {
           return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
       }
    }

    @PostMapping(value = "customer/validate/number")
    public ResponseEntity<?> validerNumber(@RequestBody String email) throws SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            //Vérification de l'état d'inscription
            if(client.getValidEmail() == 1){
                return new ResponseEntity<>(new ResponseMessage("L'email est déja validé!"),
                        HttpStatus.NOT_FOUND);
            }

            client.setValidEmail(1);
            Client newClient = clientservice.updateClient(client);

            if (newClient == null) return new ResponseEntity<>(new ResponseMessage("Veuillez réesayer plutard"), HttpStatus.INTERNAL_SERVER_ERROR);

            return new ResponseEntity<>(newClient, HttpStatus.OK);
        }  catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }

    //enregistrer une photo de profil
    @PutMapping(value = "customer/update/image/{email}")
    public ResponseEntity<?> updateImgeProfil(@RequestBody String image, @PathVariable("email") String email) throws SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            client.setPhotoProfil(image);

            Client newClient = clientservice.updateClient(client);
            if (newClient == null) return new ResponseEntity<>(new ResponseMessage("Veuillez réesayer plutard"), HttpStatus.INTERNAL_SERVER_ERROR);

            return new ResponseEntity<>(newClient, HttpStatus.OK);
        }  catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }

    // Enregistrer une CNI pour faire valider
    @PutMapping(value = "customer/update/cni/{email}")
    public ResponseEntity<?> updateCNI(@RequestBody String image, @PathVariable("email") String email) throws SendByOpException {

        try {
            Client client = clientservice.getCustomerByEmail(email);
            client.setPieceid(image);
            client.setCniisupload(1);

            Client newClient = clientservice.updateClient(client);

            if (newClient == null) return new ResponseEntity<>(new ResponseMessage("Veuillez réesayer plutard"), HttpStatus.INTERNAL_SERVER_ERROR);

            return new ResponseEntity<>(newClient, HttpStatus.OK);
        }  catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }

    // Valider piece d'identité 1 pour valider et 2 rejetter
    @PutMapping(value = "customer/valid/cni/{value}")
    public ResponseEntity<?> validateCNI(@RequestBody int value, @PathVariable("value") String email) throws MessagingException, UnsupportedEncodingException, SendByOpException {
        try {
            Client client = clientservice.getCustomerByEmail(email);
            client.setPieceidisvalid(value);

            Client newClient = clientservice.updateClient(client);
            if (newClient == null) return new ResponseEntity<>(new ResponseMessage("Veuillez réesayer plutard"), HttpStatus.INTERNAL_SERVER_ERROR);

            String response = "";

            if(value == 1) {
                response = "votre pièce d'identité a été validée";
            } else {
                response = "votre pièce d'identité a été rejetté";
            }

            String content = "Bonjour [[name]],<br>"

                    + "Nous voudrons vous informez que "+response+", Merci pour votre confiance"
                    + "Cordialement,<br>"
                    + "L'équipe de <h3><a href=\"[[URLSENDBYOP]]\" target=\"_self\">SendByOp.</a></h3><br>";

            sendMailService.simpleHtmlMessage(client, content, "Validation de pièce d'identité");

            return new ResponseEntity<>(newClient, HttpStatus.OK);
        }catch (SendByOpException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()),e.getHttpStatus());
        }
    }



}
