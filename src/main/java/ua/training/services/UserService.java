package ua.training.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.User;
import ua.training.model.dto.input.SearchUserDTO;
import ua.training.model.dto.output.UserDTO;
import ua.training.repositories.BookRepository;
import ua.training.repositories.RoleRepository;
import ua.training.repositories.UserRepository;
import ua.training.utils.Constants;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    private final RoleRepository roleRepository;

//    private final PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bookRepository = bookRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        System.out.println(username);
//        System.out.println(userRepository.findByUsername(username).orElseThrow(RuntimeException::new));
        return userRepository.findByUsername(username).orElseThrow(RuntimeException::new);
    }

    public void registerNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleRepository.findByName("ROLE_READER"));
        userRepository.save(user);
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
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    public UserDTO findUsersOrderedByName(SearchUserDTO searchUserDTO) {
        Page<User> usersBySurname = userRepository.findAll(PageRequest.of(searchUserDTO.getPageNumber(),
                Constants.NUMBER_OF_ITEMS_PER_PAGE,
                Sort.by(Sort.Direction.valueOf(searchUserDTO.getSortDirection()), searchUserDTO.getSortField())));

        return new UserDTO(usersBySurname, findUserAge(usersBySurname),
                countBooksInUsersAbonnement(usersBySurname));
    }

    private List<Integer> findUserAge(Page<User> users) {
//        return Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        return users.stream().map(user -> Period.between(user.getBirthDate(), LocalDate.now()).getYears()).collect(Collectors.toList());
    }

    private List<Integer> countBooksInUsersAbonnement(Page<User> users) {
//        return bookRepository.countBooksByUserId(user.getId());
        return users.stream().map(user -> bookRepository.countBooksByUserId(user.getId())).collect(Collectors.toList());
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((User)principal).getUsername();
        } else {
            username = principal.toString();
        }

        return (User)loadUserByUsername(username);
    }

}
