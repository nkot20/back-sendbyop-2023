package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.repositories.RoleRepository;
import com.sendByOP.expedition.services.iServices.IRoleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RoleService implements IRoleService {

    @Autowired
    RoleRepository roleRepository;

    public Role findByIntitule(String intitule){
        return roleRepository.findByIntitule(intitule).get();
    }

    public Role getRoleInfo(String role) {

        if (role.equals("admin")) {
            return roleRepository.findByIntitule("admin").get();
        }

        if (role.equals("superadmin")) {
            return roleRepository.findByIntitule("superadmin").get();
        }

        return null;
    }

}
