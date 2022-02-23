package ua.training;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import ua.training.model.Role;
import ua.training.model.User;
import ua.training.model.dto.input.LibrarianSearchDTO;
import ua.training.model.dto.input.SearchUserDTO;
import ua.training.model.dto.output.UserDTO;
import ua.training.repositories.RoleRepository;
import ua.training.repositories.UserRepository;
import ua.training.services.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;


    @MockBean
    private RoleRepository roleRepository;


    @Test
    @DisplayName("Test loadUserByUserName")
    public void testLoadUserByUserName() {
        UserDetails testUser = getLoggedUser();
        doReturn(Optional.of(testUser)).when(userRepository).findByUsername(anyString());
        UserDetails returnedUser = userService.loadUserByUsername("login");
        assertEquals(testUser, returnedUser, "User is not the same as expected");

    }

    @Test
    @DisplayName("Test registerUser")
    public void testRegisterUser() {
        UserDetails testUser = getLoggedUser();
        doReturn(testUser).when(userRepository).save(any(User.class));
        UserDetails returnedUser = userService.registerNewUser(getUser(), "ROLE_READER");
        assertEquals(testUser, returnedUser, "User is not the same as expected");

    }

    @Test
    @DisplayName("Test findUserById")
    public void testFindUserById() {
        UserDetails testUser = getLoggedUser();
        doReturn(Optional.of(testUser)).when(userRepository).findById(anyLong());
        UserDetails returnedUser = userService.findUserById(1L);
        assertEquals(testUser, returnedUser, "User is not the same as expected");
    }

    @Test
    @DisplayName("Test setLibrarianRole")
    public void testSetLibrarianRole() {
        UserDetails testUser = getLibrarian();
        doReturn(testUser).when(userRepository).save(any(User.class));
        UserDetails returnedUser = userService.registerNewUser(getUser(), "ROLE_LIBRARIAN");
        assertEquals(testUser, returnedUser, "User role was not changed");
    }

    @Test
    @DisplayName("Test getUsersOnPage")
    public void testGetUsersOnPage() {
        Page<User> users = new PageImpl<>(new ArrayList<>(Arrays.asList(getUser(),getUser())));
        doReturn(new Role(2L, "ROLE_LIBRARIAN")).when(roleRepository).findByName(anyString());
        doReturn(users).when(userRepository).findAllByRole(any(Role.class), any(Pageable.class));
        Page<User> returnedUsers = userService.getUsersByRolePerPage(new LibrarianSearchDTO(), "ROLE_LIBRARIAN");
        assertEquals(users, returnedUsers, "Users on the page are not those as expected");
    }

    @Test
    @DisplayName("Test findUsers")
    public void testFindUsers() {
        Page<User> users = new PageImpl<>(new ArrayList<>(Arrays.asList(getUser(), getUser())));
        List<Integer> ages = new ArrayList<>(Arrays.asList(31,31));
        List<Integer> numberOfBooksTaken = new ArrayList<>(Arrays.asList(0,0));
        doReturn(users).when(userRepository).findAll(any(Pageable.class));
        UserDTO userDTO = new UserDTO(users,ages, numberOfBooksTaken);
        UserDTO returnedUserDTO = userService.findUsersOrderedByName(new SearchUserDTO());
        assertEquals(userDTO, returnedUserDTO, "Users and their data are not those as expected");
    }
    private User getLoggedUser() {
        return User.builder()
                .id(1L)
                .name("John")
                .surname("Hopkins")
                .username("login")
                .role(new Role(3L, "ROLE_READER"))
                .password("$2a$10$.JmitmzYt./HkfzZaYgm3ec2VbZrIyyXQiQOKddYyc3JXvCExakam")
                .isAccountNonLocked(true)
                .birthDate(LocalDate.of(1990, 12, 12))
                .build();
    }

    private User getLibrarian() {
        return User.builder()
                .id(1L)
                .name("John")
                .surname("Hopkins")
                .username("login")
                .role(new Role(2L, "ROLE_LIBRARIAN"))
                .password("password")
                .isAccountNonLocked(true)
                .birthDate(LocalDate.of(1990, 12, 12))
                .build();
    }

    private User getUser() {
        return User.builder()
                .id(1L)
                .name("John")
                .surname("Hopkins")
                .username("login")
                .password("password")
                .isAccountNonLocked(true)
                .birthDate(LocalDate.of(1990, 12, 12))
                .build();
    }
}
