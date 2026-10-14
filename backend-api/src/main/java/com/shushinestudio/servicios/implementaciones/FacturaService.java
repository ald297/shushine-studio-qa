package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.pago.FacturaSalida;
import com.shushinestudio.dtos.pago.PagoRegistrar;
import com.shushinestudio.dtos.pago.PagoSalida;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.modelos.Factura;
import com.shushinestudio.modelos.Pago;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IFacturaRepository;
import com.shushinestudio.repositorios.IPagoRepository;
import com.shushinestudio.servicios.interfaces.IFacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Service
public class FacturaService implements IFacturaService {

    @Autowired
    private IPagoRepository pagoRepository;

    @Autowired
    private IFacturaRepository facturaRepository;

    @Autowired
    private ICitaRepository citaRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public PagoSalida registrarPago(PagoRegistrar pagoRegistrar) {
        Cita cita = citaRepository.findById(pagoRegistrar.getCitaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada con ID: " + pagoRegistrar.getCitaId()));

        Pago nuevoPago = Pago.builder()
                .cita(cita)
                .monto(pagoRegistrar.getMonto())
                .metodoPago(pagoRegistrar.getMetodoPago())
                .referenciaPos(pagoRegistrar.getReferenciaPos())
                .motivoAjustePrecio(pagoRegistrar.getMotivoAjustePrecio())
                .estado("Aprobado")
                .build();

        Pago pagoGuardado = pagoRepository.save(nuevoPago);

        // Actualizar estado de pago en la cita
        cita.setEstadoPago("Paid");
        if ("Confirmed".equalsIgnoreCase(cita.getEstado()) || "InProgress".equalsIgnoreCase(cita.getEstado())) {
            cita.setEstado("Completed");
        }
        citaRepository.save(cita);

        // Emitir Factura correlativa si aún no existe para esta cita
        if (facturaRepository.findByCitaId(cita.getId()).isEmpty()) {
            String numFactura = String.format("FAC-%d-%04d", LocalDate.now().getYear(), 1000 + random.nextInt(9000));
            Factura factura = Factura.builder()
                    .cita(cita)
                    .numeroFactura(numFactura)
                    .subtotal(cita.getSubtotal())
                    .iva(cita.getIva())
                    .total(cita.getTotal())
                    .datosEmisorReceptor("Shushine Studio S.A. de C.V. - IVA 13%")
                    .build();
            facturaRepository.save(factura);
        }

        return PagoSalida.builder()
                .id(pagoGuardado.getId())
                .citaId(cita.getId())
                .monto(pagoGuardado.getMonto())
                .metodoPago(pagoGuardado.getMetodoPago())
                .estado(pagoGuardado.getEstado())
                .referenciaPos(pagoGuardado.getReferenciaPos())
                .fechaPago(pagoGuardado.getFechaPago())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaSalida obtenerFacturaPorCita(Integer citaId) {
        Factura f = facturaRepository.findByCitaId(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada para la cita ID: " + citaId));
        return mapToFacturaSalida(f);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaSalida obtenerFacturaPorNumero(String numeroFactura) {
        Factura f = facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con número: " + numeroFactura));
        return mapToFacturaSalida(f);
    }

    private FacturaSalida mapToFacturaSalida(Factura f) {
        Cita c = f.getCita();
        String clienteNombre = c.getCliente() != null && c.getCliente().getUsuario() != null
                ? c.getCliente().getUsuario().getNombre() + " " + (c.getCliente().getUsuario().getApellido() != null ? c.getCliente().getUsuario().getApellido() : "")
                : (c.getCliente() != null ? c.getCliente().getNombreWalkin() : "Consumidor Final");

        return FacturaSalida.builder()
                .id(f.getId())
                .citaId(c.getId())
                .codigoCita(c.getCodigoCita())
                .numeroFactura(f.getNumeroFactura())
                .fechaEmision(f.getFechaEmision())
                .clienteNombre(clienteNombre)
                .subtotal(f.getSubtotal())
                .iva(f.getIva())
                .total(f.getTotal())
                .metodoPago(c.getMetodoPagoPreferente())
                .build();
    }
}
