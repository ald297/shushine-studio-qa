package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.pago.FacturaSalida;
import com.shushinestudio.dtos.pago.PagoRegistrar;
import com.shushinestudio.dtos.pago.PagoSalida;

public interface IFacturaService {
    PagoSalida registrarPago(PagoRegistrar pagoRegistrar);
    FacturaSalida obtenerFacturaPorCita(Integer citaId);
    FacturaSalida obtenerFacturaPorNumero(String numeroFactura);
}
