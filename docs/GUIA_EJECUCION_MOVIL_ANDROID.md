# Guía de Ejecución y Evidencias en Android (.NET MAUI)
**Especialista:** Cristian Jordan

## 1. Herramientas Necesarias
* **Android Debug Bridge (ADB):** Para inspección de logs y simulación de ciclo de vida.
* **Scrcpy:** Para proyectar la pantalla del dispositivo físico y capturar evidencias de alta resolución.

## 2. Comandos Clave de Verificación
```bash
# Verificar dispositivo conectado
adb devices

# Monitorear logs de la app en tiempo real (autenticación y tokens)
adb logcat -s ShushineStudio:V *:E

# Forzar muerte de proceso para probar Swipe-to-Close (CP-MOB-01)
adb shell am kill com.shushinestudio.clientapp
```

## 3. Captura de Evidencias
1. Realizar cada caso de prueba (CP-MOB-01 al CP-MOB-06).
2. Tomar captura de pantalla en el teléfono o con Scrcpy.
3. Guardar las capturas con nombres descriptivos: `CP-MOB-01_SwipeToClose.png`.
