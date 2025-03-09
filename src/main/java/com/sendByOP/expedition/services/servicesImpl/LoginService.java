package com.sendByOP.expedition.services.servicesImpl;

import com.sendByOP.expedition.models.entities.Role;
import com.sendByOP.expedition.models.entities.User;
import com.sendByOP.expedition.repositories.RoleRepository;
import com.sendByOP.expedition.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository repo;
    private final RoleRepository reporole;


    public User saveUser(User user) {
        return repo.save(user);
    }

    public Optional<User> featchUserById(String Id) {
        return repo.findByUsername(Id);
    }

    public User featchUserByIdAndPw(String Id, String pw) {
        return repo.findByUsernameAndPw(Id, pw).get();
    }

    public Role searchRoleById(Long Id) {
        return reporole.findByIdrole(Id).get();
    }
}
