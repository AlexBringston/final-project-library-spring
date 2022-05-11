package ua.training.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.User;
import ua.training.model.dto.input.AbstractSearchDTO;
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

@Log4j2
@Service
public class UserService implements UserDetailsService {

    /**
     * User repository instance
     */
    private final UserRepository userRepository;

    /**
     * Book repository instance
     */
    private final BookRepository bookRepository;

    /**
     * Role repository instance
     */
    private final RoleRepository roleRepository;

    /**
     * PasswordEncoder instance used to hash password
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Constructor used
     * @param userRepository - UserRepository instance
     * @param roleRepository - RoleRepository instance
     * @param bookRepository - BookRepository instance
     */
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Method used to find a user by given username
     * @param username - username of user who is being searched
     * @return - User instance
     * @throws UsernameNotFoundException - if no user with such username was found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading a user with given username");
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with " +
                "given username was not found"));
    }

    /**
     * Method used to register a new user with hashed password
     * @param user - user instance with data
     * @param role - role instance with info about user's role
     * @return - created user instance
     */
    public User registerNewUser(User user, String role) {
        log.info("Creating a user with role: " + role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleRepository.findByName(role));
        return userRepository.save(user);
    }

    /**
     * Method used to remove librarian role from user with given id
     * @param userId - id of user
     * @return - updated user instance
     */
    @Transactional
    public User removeLibrarianRole(Long userId) {
        log.info("Removing librarian role from user");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Could not find such user"));
        user.setRole(roleRepository.findByName("ROLE_READER"));
        return userRepository.save(user);
    }

    /**
     * Method used to find users ordered by their surname
     * @param searchUserDTO - DTO used to implement pagination
     * @return - Page object with list of users that were found
     */
    public UserDTO findUsersOrderedByName(SearchUserDTO searchUserDTO) {
        Page<User> usersBySurname = userRepository.findAll(PageRequest.of(searchUserDTO.getPageNumber(),
                Constants.NUMBER_OF_ITEMS_PER_PAGE,
                Sort.by(Sort.Direction.valueOf(searchUserDTO.getSortDirection()), searchUserDTO.getSortField())));
        return new UserDTO(usersBySurname, findUsersAges(usersBySurname),
                countBooksInUsersAbonnements(usersBySurname));
    }

    /**
     * Method used to get a list of users' ages
     * @param users - list of users on current page
     * @return - list of ages of users
     */
    private List<Integer> findUsersAges(Page<User> users) {
        log.info("Counting ages of each user is a list");
        return users.stream()
                .map(user -> Period.between(user.getBirthDate(), LocalDate.now()).getYears())
                .collect(Collectors.toList());
    }

    /**
     * Method used to count how many books were taken by each user in list
     * @param users - list of users on current page
     * @return - list of integers which correspond to the number of books that were taken by the particular user
     */
    private List<Integer> countBooksInUsersAbonnements(Page<User> users) {
        log.info("Counting books taken by each user in a list");
        return users.stream()
                .map(user -> bookRepository.countBooksByUserId(user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Method used to retrieve current user logged in system
     * @return - user instance
     */
    public User getCurrentUser() {
        log.info("Retrieving current logged user info");
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((User) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return (User) loadUserByUsername(username);
    }

    /**
     * Method used to get a page of users by given role
     * @param searchDTO - DTO used to implement pagination
     * @param role - String with role name
     * @return - page object with list of users
     */
    public Page<User> getUsersByRolePerPage(AbstractSearchDTO searchDTO, String role) {
        log.info("Retrieving users for a current page");
        return userRepository.findAllByRole(roleRepository.findByName(role),
                PageRequest.of(searchDTO.getPageNumber(),
                        Constants.NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.valueOf(searchDTO.getSortDirection()), searchDTO.getSortField())));
    }

    /**
     * Method used to find a user by id
     * @param userId - id of user
     * @return - found user instance or exception is thrown
     */
    public User findUserById(Long userId) {
        log.info("Looking for a user with id: " + userId);
        return userRepository
                .findById(userId
                ).orElseThrow(() -> new RuntimeException("User was not found"));
    }

    /**
     * Method used to update user status (block or unblock)
     * @param action - string which contains a name of the action to be performed
     * @param userId - id of user
     */
    public void updateUserStatus(String action, Long userId) {
        log.info("Changing user status");
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User was not found"));
        if (action.equals("block")) {
            user.setAccountNonLocked(false);
        }
        if (action.equals("unblock")) {
            user.setAccountNonLocked(true);
        }
        userRepository.save(user);
    }
}
