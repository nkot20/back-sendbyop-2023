package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.entities.Role;

public interface IRoleService {

    // Méthode pour récupérer un rôle par son intitulé
    Role findByIntitule(String intitule);

    // Méthode pour récupérer les informations sur un rôle selon un paramètre "role" (ex. admin, superadmin)
    Role getRoleInfo(String role);
}
