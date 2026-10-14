package com.shushinestudio.servicios;

import com.shushinestudio.dtos.pago.FacturaSalida;
import com.shushinestudio.dtos.pago.PagoRegistrar;
import com.shushinestudio.dtos.pago.PagoSalida;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.modelos.Factura;
import com.shushinestudio.modelos.Pago;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IFacturaRepository;
import com.shushinestudio.repositorios.IPagoRepository;
import com.shushinestudio.servicios.implementaciones.FacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacturaServiceTest {

    private IPagoRepository pagoRepository;
    private IFacturaRepository facturaRepository;
    private ICitaRepository citaRepository;
    private FacturaService facturaService;

    private Cita cita;

    @BeforeEach
    void setUp() {
        pagoRepository = mock(IPagoRepository.class);
        facturaRepository = mock(IFacturaRepository.class);
        citaRepository = mock(ICitaRepository.class);

        facturaService = new FacturaService();
        ReflectionTestUtils.setField(facturaService, "pagoRepository", pagoRepository);
        ReflectionTestUtils.setField(facturaService, "facturaRepository", facturaRepository);
        ReflectionTestUtils.setField(facturaService, "citaRepository", citaRepository);

        cita = Cita.builder()
                .id(10)
                .codigoCita("SHU-2026-1001")
                .subtotal(new BigDecimal("50.00"))
                .iva(new BigDecimal("6.50"))
                .total(new BigDecimal("56.50"))
                .estado("Confirmed")
                .estadoPago("Pending")
                .metodoPagoPreferente("Tarjeta")
                .build();
    }

    @Test
    @DisplayName("Registrar pago exitoso actualiza estado de cita y emite factura fiscal")
    void testRegistrarPagoYFactura() {
        PagoRegistrar dto = PagoRegistrar.builder()
                .citaId(10)
                .monto(new BigDecimal("56.50"))
                .metodoPago("Tarjeta")
                .referenciaPos("POS-998822")
                .build();

        when(citaRepository.findById(10)).thenReturn(Optional.of(cita));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago p = invocation.getArgument(0);
            p.setId(1);
            p.setFechaPago(LocalDateTime.now());
            return p;
        });
        when(facturaRepository.findByCitaId(10)).thenReturn(Optional.empty());

        PagoSalida salida = facturaService.registrarPago(dto);

        assertNotNull(salida);
        assertEquals(10, salida.getCitaId());
        assertEquals(new BigDecimal("56.50"), salida.getMonto());
        assertEquals("Paid", cita.getEstadoPago());
        assertEquals("Completed", cita.getEstado());

        verify(facturaRepository, times(1)).save(any(Factura.class));
        verify(citaRepository, times(1)).save(cita);
    }

    @Test
    @DisplayName("Obtener factura por ID de cita existente retorna datos fiscales completos")
    void testObtenerFacturaPorCita() {
        Factura f = Factura.builder()
                .id(1)
                .cita(cita)
                .numeroFactura("FAC-2026-1001")
                .fechaEmision(LocalDateTime.now())
                .subtotal(new BigDecimal("50.00"))
                .iva(new BigDecimal("6.50"))
                .total(new BigDecimal("56.50"))
                .build();

        when(facturaRepository.findByCitaId(10)).thenReturn(Optional.of(f));

        FacturaSalida salida = facturaService.obtenerFacturaPorCita(10);

        assertNotNull(salida);
        assertEquals("FAC-2026-1001", salida.getNumeroFactura());
        assertEquals(new BigDecimal("56.50"), salida.getTotal());
    }
}
