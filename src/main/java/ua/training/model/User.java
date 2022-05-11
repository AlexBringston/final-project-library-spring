package ua.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

/**
 * Entity used to store data from corresponding table in database
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    @NotNull
    @Pattern(regexp = "^[A-Z][a-z]+|[А-ЯЯЇЄЁ][а-яюєїё']+$")
    private String name;

    @NotBlank(message = "Surname is mandatory")
    @NotNull
    @Pattern(regexp = "^[A-Z][a-z]+|[А-ЯЯЇЄЁ][а-яюєїё']+$")
    private String surname;

    @NotBlank(message = "Username is mandatory")
    @NotNull
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{1,20}$", message = "Username should match a pattern")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @NotNull
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).*$")
    private String password;

    @NotNull
    @Column(name = "is_account_non_blocked")
    private boolean isAccountNonLocked = true;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Transient
    private final boolean isAccountNonExpired = true;

    @Transient
    private final boolean isCredentialsNonExpired = true;

    @Transient
    private final boolean isEnabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(getRole());
    }

}



