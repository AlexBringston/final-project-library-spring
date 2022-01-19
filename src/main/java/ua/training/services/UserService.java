package ua.training.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.User;
import ua.training.repositories.RoleRepository;
import ua.training.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(RuntimeException::new);
    }

    public void registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_READER"));
    }

    @Transactional
    public void setLibrarianRole(User user) {
        user.setRole(roleRepository.findByName("ROLE_LIBRARIAN"));
        userRepository.save(user);
    }

    @Transactional
    public void removeLibrarianRole(User user) {
        user.setRole(roleRepository.findByName("ROLE_READER"));
        userRepository.save(user);
    }

    @Transactional
    public void blockUser(User user) {
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(User user) {
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }
}
