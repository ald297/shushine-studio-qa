package com.shushinestudio.controladores;

import com.shushinestudio.dtos.pago.FacturaSalida;
import com.shushinestudio.dtos.pago.PagoRegistrar;
import com.shushinestudio.dtos.pago.PagoSalida;
import com.shushinestudio.servicios.interfaces.IFacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos y Facturación", description = "Endpoints para registro de liquidaciones de caja, POS y emisión de facturas correlativas con IVA 13%")
@SecurityRequirement(name = "Bearer Authentication")
public class PagoController {

    @Autowired
    private IFacturaService facturaService;

    @PostMapping
    @Operation(summary = "Registrar liquidación de pago de cita", description = "Registra el cobro en caja o terminal POS, actualiza el estado de pago de la cita y emite la factura correlativa correspondiente.")
    public ResponseEntity<PagoSalida> registrarPago(@Valid @RequestBody PagoRegistrar pagoRegistrar) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.registrarPago(pagoRegistrar));
    }

    @GetMapping("/factura/cita/{citaId}")
    @Operation(summary = "Consultar factura por ID de cita", description = "Retorna el comprobante fiscal y desglose de IVA de una cita atendida.")
    public ResponseEntity<FacturaSalida> obtenerFacturaPorCita(@PathVariable Integer citaId) {
        return ResponseEntity.ok(facturaService.obtenerFacturaPorCita(citaId));
    }

    @GetMapping("/factura/{numeroFactura}")
    @Operation(summary = "Consultar factura por número de comprobante", description = "Retorna el detalle fiscal a partir del correlativo fiscal oficial (ej. FAC-2026-1045).")
    public ResponseEntity<FacturaSalida> obtenerFacturaPorNumero(@PathVariable String numeroFactura) {
        return ResponseEntity.ok(facturaService.obtenerFacturaPorNumero(numeroFactura));
    }
}
