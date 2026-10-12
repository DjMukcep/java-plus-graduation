package ru.practicum.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.entity.compilation.CompilationService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(CompilationAdminController.class)
class CompilationAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    private final long compId = 1L;
    private final long eventId = 2L;
    private final long categoryId = 3L;
    private final long userId = 4L;
    private final long views = 123L;
    private final String compTitle = "Compilation title";
    private final LocalDateTime eventDate = LocalDateTime.now();

    private NewCompilationDto newCompilation;
    private UpdateCompilationRequest updateCompilation;
    private CompilationDto compilation;
    private EventShortDto event;
    private CategoryDto category;
    private UserShortDto initiator;

    @BeforeEach
    public void setUp() {
        newCompilation = new NewCompilationDto(Set.of(eventId), false, compTitle);
        updateCompilation = UpdateCompilationRequest.builder()
                .title("New Compilation title")
                .events(Set.of())
                .pinned(true)
                .build();

        category = CategoryDto.builder()
                .id(categoryId)
                .name("Category")
                .build();

        initiator = UserShortDto.builder()
                .id(userId)
                .name("Name")
                .build();

        event = EventShortDto.builder()
                .id(eventId)
                .annotation("annotation")
                .title("event title")
                .initiator(initiator)
                .category(category)
                .eventDate(eventDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .paid(false)
                .views(views)
                .build();

        compilation = CompilationDto.builder()
                .id(compId)
                .events(Set.of(event))
                .title(compTitle)
                .pinned(false)
                .build();
    }

    @Test
    void create_success() throws Exception {
        when(compilationService.create(any(NewCompilationDto.class))).thenReturn(compilation);

        String requestBody = objectMapper.writeValueAsString(newCompilation);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(compId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(compTitle))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pinned").value(false));

        verify(compilationService).create(eq(newCompilation));
    }

    @Test
    void create_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        newCompilation.setTitle("   ");

        when(compilationService.create(any(NewCompilationDto.class))).thenReturn(compilation);

        String requestBody = objectMapper.writeValueAsString(newCompilation);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/admin/compilations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verifyNoInteractions(compilationService);
    }

    @Test
    void create_shouldReturnBadRequest_whenTitleIsLongerThan50() throws Exception {
        newCompilation.setTitle("L" + "o".repeat(50) + "ng title");

        when(compilationService.create(any(NewCompilationDto.class))).thenReturn(compilation);

        String requestBody = objectMapper.writeValueAsString(newCompilation);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/admin/compilations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verifyNoInteractions(compilationService);
    }

    @Test
    void delete_success() throws Exception {

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/admin/compilations/{compId}", compId))
        .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(compilationService).delete(eq(compId));
    }

    @Test
    void delete_shouldReturnNotFound_whenCompilationNotFound() throws Exception {

        long unExistingId = 999;

        doThrow(new NotFoundException("Подборка с id=" + unExistingId + " не найдена"))
                .when(compilationService)
                .delete(anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/admin/compilations/{compId}", unExistingId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(compilationService).delete(eq(unExistingId));
    }

    @Test
    void update_success() throws Exception {
        compilation.setTitle(updateCompilation.getTitle());
        compilation.setPinned(updateCompilation.getPinned());
        compilation.setEvents(Set.of());

        when(compilationService.update(eq(compId), any(UpdateCompilationRequest.class)))
                .thenReturn(compilation);

        String requestBody = objectMapper.writeValueAsString(updateCompilation);

        mockMvc.perform(
                MockMvcRequestBuilders.patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(compId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(updateCompilation.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pinned").value(updateCompilation.getPinned()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.events").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.events").isEmpty());

        verify(compilationService).update(eq(compId), eq(updateCompilation));
    }

    @Test
    void update_returnBadRequest_whenTitleIsBlank() throws Exception {
        updateCompilation.setTitle("   ");
        when(compilationService.update(eq(compId), any(UpdateCompilationRequest.class)))
                .thenThrow(new ValidationException("Title can't be empty!"));

        String requestBody = objectMapper.writeValueAsString(updateCompilation);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/admin/compilations/{compId}", compId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(compilationService).update(eq(compId), eq(updateCompilation));
    }

    @Test
    void update_returnBadRequest_whenTitleLengthIsLongerThan50() throws Exception {
        updateCompilation.setTitle("L" + "o".repeat(50) + "ng title");

        String requestBody = objectMapper.writeValueAsString(updateCompilation);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/admin/compilations/{compId}", compId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testDeleteCompilation_invalidId() throws Exception {
        long invalidId = -1L;
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/admin/compilations/{compId}", invalidId))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(compilationService, org.mockito.Mockito.never()).delete(any());
    }

}