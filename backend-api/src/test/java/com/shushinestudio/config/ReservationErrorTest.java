package com.shushinestudio.config;

import com.shushinestudio.controladores.CitaController;
import com.shushinestudio.servicios.interfaces.ICitaService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

class ReservationErrorTest {
    private MockMvc controller(RuntimeException failure) {
        ICitaService service = mock(ICitaService.class);
        when(service.crearCita(any(), eq("test"))).thenThrow(failure);
        when(service.crearCitaWalkin(any())).thenThrow(failure);
        CitaController controller = new CitaController();
        ReflectionTestUtils.setField(controller, "citaService", service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private void expectState(String state, int status) throws Exception {
        var mvc = controller(new DataIntegrityViolationException("internal-secret", new SQLException("internal-secret", state)));
        mvc.perform(post("/api/citas")
                .principal(new UsernamePasswordAuthenticationToken("test", null, List.of()))
                .contentType("application/json")
                .content("{\"estilistaId\":1,\"fechaCita\":\"2027-01-01\",\"horaInicio\":\"10:00\",\"servicioIds\":[1]}"))
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(content().string(not(containsString("internal-secret"))));
    }

    @Test void overlapIs409() throws Exception { expectState("23P01",409); }
    @Test void serializationFailureIsNotBookingConflict() throws Exception { expectState("40001",503); }
    @Test void unrelatedConstraintIsNotBookingConflict() throws Exception { expectState("23503",500); }
    @Test void walkinDoesNotHidePersistenceFailureAs409() throws Exception {
        controller(new DataIntegrityViolationException("duplicate lock internal-secret",new SQLException("internal-secret","23503")))
                .perform(post("/api/citas/walkin").contentType("application/json").content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(not(containsString("internal-secret"))));
    }
    @Test void concurrentUpdateKeeps409WithoutPretendingSlotConflict() throws Exception {
        controller(new org.springframework.dao.OptimisticLockingFailureException("internal-secret"))
                .perform(post("/api/citas").principal(new UsernamePasswordAuthenticationToken("test",null,List.of()))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("modificada")))
                .andExpect(content().string(not(containsString("internal-secret"))));
    }
    @Test void wrappedPoolTimeoutWithoutSQLStateIs503() throws Exception {
        controller(new org.springframework.transaction.CannotCreateTransactionException("internal-secret",
                new java.sql.SQLTransientConnectionException("internal-secret")))
                .perform(post("/api/citas").principal(new UsernamePasswordAuthenticationToken("test",null,List.of()))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(not(containsString("internal-secret"))));
    }
    @Test void connectionFailureIs503() throws Exception {
        var mvc=controller(new DataAccessResourceFailureException("internal-secret"));
        mvc.perform(post("/api/citas")
                .principal(new UsernamePasswordAuthenticationToken("test",null,List.of()))
                .contentType("application/json").content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(not(containsString("internal-secret"))));
    }
}
