package mx.edu.cetys.software_quality_lab.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mx.edu.cetys.software_quality_lab.users.exceptions.DuplicateUsernameException;
import mx.edu.cetys.software_quality_lab.users.exceptions.InvalidUserDataException;
import mx.edu.cetys.software_quality_lab.users.exceptions.UserNotFoundException;
import mx.edu.cetys.software_quality_lab.validators.EmailValidatorService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    // EmailValidatorService debe ser mockeado — en pruebas unitarias no probamos dependencias externas
    @Mock
    EmailValidatorService emailValidatorService;

    @InjectMocks
    UserService userService;

    // ─── Caso exitoso ─────────────────────────────────────────────────────────

    // Helper — User simulado como si ya estuviera guardado en BD (con ID asignado)
    private User buildMockSavedUser(Long id, String username, String firstName, String lastName,
                                    String phone, String email, Integer age) {
        var user = new User(username, firstName, lastName, phone, email, age);
        user.setId(id);
        return user;
    }

    // Helper — request válido base; cada test de validación modifica solo el campo que le importa
    private UserController.UserRequest validRequest() {
        return new UserController.UserRequest("juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 25);
    }

    // ─── Caso exitoso ─────────────────────────────────────────────────────────

    @Test
    void shouldRegisterUserSuccessfully() {
        var request = validRequest();
        var mockSaved = buildMockSavedUser(1L, "juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 25);

        when(emailValidatorService.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByUsername("juan4_dev")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(mockSaved);

        var response = userService.registerUser(request);

        verify(userRepository, times(1)).save(any());
        assertEquals(1L, response.id());
        assertEquals("juan4_dev", response.username());
        assertEquals("ACTIVE", response.status());
    }
//ximena 
    @Test
    void shouldGetUserByIdSuccessfully() {
        var mockUser = buildMockSavedUser(1L, "juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 25);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        var response = userService.getUserById(1L);

        assertEquals(1L, response.id());
        assertEquals("juan4_dev", response.username());
        assertEquals("Juan", response.firstName());
        assertEquals("6641234567", response.phone());
        assertEquals("j4n#gmil.com", response.email());
        assertEquals(25, response.age());
        assertEquals("ACTIVE", response.status());

        // TODO: arrange — mockear userRepository.findById para que regrese un Optional<User> con datos
        // TODO: act — llamar a userService.getUserById(1L)
        // TODO: assert — verificar que los campos del response coincidan con el mock
    }

    @Test
    void shouldSuspendActiveUserSuccessfully() {
        // TODO: arrange — mockear findById con un usuario ACTIVE
        // TODO: act — llamar a userService.suspendUser(id)
        // TODO: assert — verificar que el status regresado sea "SUSPENDED"; confirmar que save fue llamado
    }

    // ─── Validaciones de Username ─────────────────────────────────────────────

    @Test
    void shouldThrowWhenUsernameTooShort() {
        // 4 caracteres — mínimo es 5 (caso límite)
        var request = new UserController.UserRequest("ab4c", "Juan", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUsernameTooLong() {
        // 21 caracteres — máximo es 20 (caso límite)
        var request = new UserController.UserRequest("a".repeat(21), "Juan", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUsernameHasInvalidChars() {
        // mayúsculas y @ no son permitidos
        var request = new UserController.UserRequest("User@Name4", "Juan", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUsernameStartsWithUnderscore() {
        var request = new UserController.UserRequest("_juan4dev", "Juan", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUsernameEndsWithUnderscore() {
        var request = new UserController.UserRequest("juan4dev_", "Juan", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    // ─── Validaciones de Nombre ───────────────────────────────────────────────

    @Test
    void shouldThrowWhenFirstNameTooShort() {
        // 1 carácter — mínimo es 2 (caso límite)
        var request = new UserController.UserRequest("juan4_dev", "J", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenFirstNameContainsNumbers() {
        var request = new UserController.UserRequest("juan4_dev", "Juan5", "López", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenLastNameTooShort() {
        // 1 carácter — mínimo es 2 (caso límite)
        var request = new UserController.UserRequest("juan4_dev", "Juan", "L", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenLastNameContainsNumbers() {
        var request = new UserController.UserRequest("juan4_dev", "Juan", "Perez2", "6641234567", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    // ─── Validaciones de Age ─────────────────────────────────────────────────

    @Test
    void shouldThrowWhenAgeIsExactlyTwelve() {
        // 12 no es válido — debe ser MAYOR a 12, no mayor o igual (caso límite)
        var request = new UserController.UserRequest("juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 12);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAgeIsBelowTwelve() {
        var request = new UserController.UserRequest("juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 5);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAgeExceedsMaximum() {
        // 121 — máximo permitido es 120 (caso límite)
        var request = new UserController.UserRequest("juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 121);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    // ─── Validaciones de Phone ───────────────────────────────────────────────

    @Test
    void shouldThrowWhenPhoneHasWrongLength() {
        // 9 dígitos — debe tener exactamente 10 (caso límite)
        var request = new UserController.UserRequest("juan4_dev", "Juan", "López", "664123456", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPhoneContainsLetters() {
        var request = new UserController.UserRequest("juan4_dev", "Juan", "López", "123456789a", "j4n#gmil.com", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    // ─── Validación de Email ──────────────────────────────────────────────────

    @Test
    void shouldThrowWhenEmailIsInvalid() {
        when(emailValidatorService.isValid(anyString())).thenReturn(false);

        var request = new UserController.UserRequest("juan4_dev", "Juan", "López", "6641234567", "invalidemail", 25);
        assertThrows(InvalidUserDataException.class, () -> userService.registerUser(request));

        verify(emailValidatorService, times(1)).isValid(anyString());
        verify(userRepository, never()).save(any());
    }

    // ─── Unicidad de Username ─────────────────────────────────────────────────

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        when(emailValidatorService.isValid(anyString())).thenReturn(true);
        when(userRepository.existsByUsername("juan4_dev")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> userService.registerUser(validRequest()));

        verify(userRepository, never()).save(any());
    }

    // ─── Not found ───────────────────────────────────────────────────────────
//ximena
    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
        
        // TODO: mockear userRepository.findById para que regrese Optional.empty()
        // TODO: assertThrows UserNotFoundException
    }

    @Test
    void shouldThrowWhenSuspendingAlreadySuspendedUser() {
        // TODO: mockear findById con un usuario SUSPENDED
        // TODO: assertThrows InvalidUserDataException
    }
}
