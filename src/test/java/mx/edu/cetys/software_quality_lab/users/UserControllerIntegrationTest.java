package mx.edu.cetys.software_quality_lab.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // Limpiar la BD antes de cada prueba para garantizar un estado independiente
    @BeforeEach
    public void limpiarBD() {
        userRepository.deleteAll();
    }

    // Helper — JSON válido base para reutilizar en cada test con un campo modificado
    private String validBody() {
        return """
                {
                    "username": "juan4_dev",
                    "firstName": "Juan",
                    "lastName": "López",
                    "phone": "6641234567",
                    "email": "j4n#gmil.com",
                    "age": 25
                }""";
    }

    // ─── POST /users ──────────────────────────────────────────────────────────

    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.info").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$.response.user.username").value("juan4_dev"))
                .andExpect(jsonPath("$.response.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.response.user.id").isNumber())
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void shouldReturn400WhenUsernameIsTooShort() throws Exception {
        // 4 caracteres — mínimo es 5 (caso límite)
        String body = """
                { "username": "ab4c", "firstName": "Juan", "lastName": "López",
                  "phone": "6641234567", "email": "j4n#gmil.com", "age": 25 }""";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void shouldReturn400WhenAgeIsExactlyTwelve() throws Exception {
        // 12 no es válido — debe ser MAYOR a 12 (caso límite)
        String body = """
                { "username": "juan4_dev", "firstName": "Juan", "lastName": "López",
                  "phone": "6641234567", "email": "j4n#gmil.com", "age": 12 }""";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPhoneIsInvalid() throws Exception {
        // 3 dígitos — debe tener exactamente 10
        String body = """
                { "username": "juan4_dev", "firstName": "Juan", "lastName": "López",
                  "phone": "123", "email": "j4n#gmil.com", "age": 25 }""";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // formato estándar con @ no pasa el validador del lab
        String body = """
                { "username": "juan4_dev", "firstName": "Juan", "lastName": "López",
                  "phone": "6641234567", "email": "user@gmail.com", "age": 25 }""";

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409WhenUsernameIsDuplicated() throws Exception {
        // Guardar un usuario con el mismo username directo en BD
        userRepository.save(new User("juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 25));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    // ─── GET /users/{id} ─────────────────────────────────────────────────────
//ximena
    @Test
    void shouldReturn200AndUserWhenFound() throws Exception {
        User saved = userRepository.save(new User("juan4_dev", "Juan", "López", "6641234567", "j4n#gmil.com", 25));

        mockMvc.perform(get("/users/" + saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.response.user.id").value(saved.getId()))
            .andExpect(jsonPath("$.response.user.username").value("juan4_dev"))
            .andExpect(jsonPath("$.response.user.firstName").value("Juan"))
            .andExpect(jsonPath("$.response.user.lastName").value("López"))
            .andExpect(jsonPath("$.response.user.phone").value("6641234567"))
            .andExpect(jsonPath("$.response.user.status").value("ACTIVE"))
            .andExpect(jsonPath("$.error").isEmpty());

        // TODO: guardar un usuario via repository, obtener su id generado
        // TODO: realizar GET /users/{id}
        // TODO: andExpect status 200
        // TODO: andExpect jsonPath campos coincidan con el usuario guardado
    }
//ximena
    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
    mockMvc.perform(get("/users/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").isNotEmpty());
}

    // ─── PATCH /users/{id}/suspend ────────────────────────────────────────────

    @Test
    void shouldSuspendUserAndReturn200() throws Exception {
        // TODO: guardar un usuario ACTIVE via repository
        // TODO: realizar PATCH /users/{id}/suspend
        // TODO: andExpect status 200
        // TODO: andExpect jsonPath("$.response.user.status") == "SUSPENDED"
    }

    @Test
    void shouldReturn400WhenSuspendingAlreadySuspendedUser() throws Exception {
        // TODO: guardar un usuario con status SUSPENDED via repository
        // TODO: realizar PATCH /users/{id}/suspend
        // TODO: andExpect status 400
    }
}
