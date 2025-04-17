package com.sendByOP.expedition.utils;

import com.sendByOP.expedition.exception.ErrorInfo;
import com.sendByOP.expedition.exception.SendByOpException;

import java.util.Objects;

public class CHeckNull {

    public static void checkNumero(Integer numero) throws SendByOpException {
        if (Objects.isNull(numero)) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkEmail(String email) throws SendByOpException {
        if (Objects.isNull(email)) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkLibelle(String libelle) throws SendByOpException {
        if (Objects.isNull(libelle)) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkIntitule(String intitule) throws SendByOpException {
        if (Objects.isNull(intitule)) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkNomPays(String nompays) throws SendByOpException {
        if (Objects.isNull(nompays)) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkNomVille(String nomVille) throws SendByOpException {
        if (Objects.isNull(nomVille)) {
            throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkNomClient(String nomClient) throws SendByOpException {
        if ( (Objects.isNull(nomClient)))
        {throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkNomEmploye(String nomEmploye) throws SendByOpException {
        if ( (Objects.isNull(nomEmploye)))
        {throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkString(String  value) throws SendByOpException {
        if ( (Objects.isNull(value)))
        {throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }

    public static void checkInt(String  value) throws SendByOpException {
        if ( (Objects.isNull(value)))
        {throw new SendByOpException(ErrorInfo.REFERENCE_RESOURCE_REQUIRED);
        }
    }
}
