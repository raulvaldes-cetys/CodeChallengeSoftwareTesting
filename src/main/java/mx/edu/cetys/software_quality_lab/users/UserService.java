package mx.edu.cetys.software_quality_lab.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mx.edu.cetys.software_quality_lab.users.exceptions.DuplicateUsernameException;
import mx.edu.cetys.software_quality_lab.users.exceptions.InvalidUserDataException;
import mx.edu.cetys.software_quality_lab.users.exceptions.UserNotFoundException;
import mx.edu.cetys.software_quality_lab.validators.EmailValidatorService;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final EmailValidatorService emailValidatorService;

    public UserService(UserRepository userRepository, EmailValidatorService emailValidatorService) {
        this.userRepository = userRepository;
        this.emailValidatorService = emailValidatorService;
    }

    /**
     * Registrar un nuevo usuario aplicando todas las reglas de negocio.
     *
     * Reglas a implementar (lanzar InvalidUserDataException a menos que se indique):
     *  1. Username  — entre 5 y 20 caracteres, solo letras minúsculas, dígitos y guion bajo (_),
     *                 NO debe comenzar ni terminar con guion bajo
     *  2. First name — entre 2 y 50 caracteres, solo letras (se permiten acentos: á, é, ñ, etc.)
     *  3. Last name  — entre 2 y 50 caracteres, solo letras (se permiten acentos)
     *  4. Age        — debe ser mayor a 12 y menor o igual a 120
     *  5. Phone      — exactamente 10 dígitos, sin letras ni símbolos
     *  6. Email      — delegar a emailValidatorService.isValid(email);
     *                  lanzar InvalidUserDataException si regresa false
     *  7. Unicidad del username — si userRepository.existsByUsername regresa true,
     *                             lanzar DuplicateUsernameException
     */
    UserController.UserResponse registerUser(UserController.UserRequest request) {
        log.info("Iniciando registro de usuario, username={}", request.username());

        // 1. Username  — entre 5 y 20 caracteres, solo letras minúsculas, dígitos y guion bajo (_),
        //                NO debe comenzar ni terminar con guion bajo
        if (request.username() == null
                || request.username().length() < 5
                || request.username().length() > 20
                || !request.username().matches("[a-z0-9_]+")
                || request.username().startsWith("_")
                || request.username().endsWith("_")) {
            throw new InvalidUserDataException("El username es inválido: 5-20 chars, solo [a-z0-9_], sin _ al inicio ni al final");
        }

        // 2. First name — entre 2 y 50 caracteres, solo letras (se permiten acentos: á, é, ñ, etc.)
        if (request.firstName() == null
                || request.firstName().length() < 2
                || request.firstName().length() > 50
                || !request.firstName().matches("\\p{L}+")) {
            throw new InvalidUserDataException("El nombre es inválido: 2-50 chars, solo letras");
        }

        // 3. Last name  — entre 2 y 50 caracteres, solo letras (se permiten acentos)
        if (request.lastName() == null
                || request.lastName().length() < 2
                || request.lastName().length() > 50
                || !request.lastName().matches("\\p{L}+")) {
            throw new InvalidUserDataException("El apellido es inválido: 2-50 chars, solo letras");
        }

        // 4. Age — debe ser mayor a 12 y menor o igual a 120
        if (request.age() == null || request.age() <= 12 || request.age() > 120) {
            throw new InvalidUserDataException("La edad es inválida: debe ser mayor a 12 y máximo 120");
        }

        // 5. Phone — exactamente 10 dígitos, sin letras ni símbolos
        if (request.phone() == null || !request.phone().matches("\\d{10}")) {
            throw new InvalidUserDataException("El teléfono es inválido: exactamente 10 dígitos");
        }

        // 6. Email — delegar a emailValidatorService.isValid(email);
        if (!emailValidatorService.isValid(request.email())) {
            throw new InvalidUserDataException("El email es inválido");
        }

        // 7. Unicidad del username — si userRepository.existsByUsername regresa true,
        //                            lanzar DuplicateUsernameException
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUsernameException("El username '" + request.username() + "' ya está registrado");
        }

        var saved = userRepository.save(
                new User(request.username(), request.firstName(), request.lastName(),
                        request.phone(), request.email(), request.age())
        );

        log.info("Usuario registrado exitosamente, id={}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Buscar un usuario por ID.
     * Lanzar UserNotFoundException (HTTP 404) si el usuario no existe.
     */

    //ximena
    UserController.UserResponse getUserById(Long id) {
        log.info("Buscando usuario por ID, id={}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));
            return mapToResponse(user);
    }

    /**
     * Suspender un usuario ACTIVO.
     * Lanzar UserNotFoundException si el usuario no existe.
     * Lanzar InvalidUserDataException si el usuario ya está SUSPENDED.
     */
    UserController.UserResponse suspendUser(Long id) {
        log.info("Suspendiendo usuario, id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id=" + id));
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidUserDataException("El usuario con id=" + id + " ya está suspendido");
        }
        user.setStatus(UserStatus.SUSPENDED);
        User saved = userRepository.save(user);
        log.info("Usuario suspendido exitosamente, id={}", saved.getId());
        return mapToResponse(saved);
    }

    private UserController.UserResponse mapToResponse(User user) {
        return new UserController.UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getAge(),
                user.getStatus().name()
        );
    }
}
