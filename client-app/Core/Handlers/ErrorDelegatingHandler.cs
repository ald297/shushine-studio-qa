using System.Net;
using System.Net.Http.Headers;
using System.Text.Json;
using ShushineStudio.Mobile.Core.Constants;
using ShushineStudio.Mobile.Core.Models;

namespace ShushineStudio.Mobile.Core.Handlers;

/// <summary>
/// Interceptor HTTP centralizado para:
/// 1. Adjuntar el token JWT en la cabecera Authorization: Bearer <token>.
/// 2. Capturar y deserializar errores bajo el estándar RFC 7807 (Problem Details).
/// 3. Manejar 401 (expiración de sesión) redirigiendo a la pantalla de Login.
/// 4. Manejar 403 (falta de permisos) y 409 (conflicto de reservas).
/// </summary>
public class ErrorDelegatingHandler : DelegatingHandler
{
    private static readonly SemaphoreSlim _dialogLock = new(1, 1);

    protected override async Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request, 
        CancellationToken cancellationToken)
    {
        // 1. Inyectar token de autenticación si existe en SecureStorage (excepto en login y registro)
        var isLoginOrRegister = request.RequestUri?.AbsolutePath.EndsWith("/auth/login") == true 
                             || request.RequestUri?.AbsolutePath.EndsWith("/auth/registro") == true
                             || request.RequestUri?.AbsolutePath.EndsWith("/auth/authenticate") == true;

        if (!isLoginOrRegister)
        {
            var token = await SecureStorage.Default.GetAsync(ApiConstants.AuthTokenKey);
            if (!string.IsNullOrWhiteSpace(token))
            {
                request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }
            else
            {
                request.Headers.Authorization = null;
            }
        }
        else
        {
            request.Headers.Authorization = null;
        }

        // 2. Ejecutar la petición HTTP
        HttpResponseMessage response;
        try
        {
            response = await base.SendAsync(request, cancellationToken);
        }
        catch (TaskCanceledException)
        {
            if (!isLoginOrRegister)
            {
                await MainThread.InvokeOnMainThreadAsync(async () =>
                {
                    if (!await _dialogLock.WaitAsync(0)) return;
                    try
                    {
                        if (Application.Current?.MainPage != null)
                        {
                            await Application.Current.MainPage.DisplayAlert(
                                "Tiempo de Espera Agotado", 
                                "El servidor del salón está tardando demasiado en responder. Por favor intente de nuevo en unos momentos.", 
                                "Aceptar"
                            );
                        }
                    }
                    catch { }
                    finally
                    {
                        _dialogLock.Release();
                    }
                });
            }
            throw;
        }
        catch (HttpRequestException)
        {
            if (!isLoginOrRegister)
            {
                await MainThread.InvokeOnMainThreadAsync(async () =>
                {
                    if (!await _dialogLock.WaitAsync(0)) return;
                    try
                    {
                        if (Application.Current?.MainPage != null)
                        {
                            await Application.Current.MainPage.DisplayAlert(
                                "Sin Conexión", 
                                "No se pudo establecer comunicación con el servidor del salón. Verifique su conexión a internet.", 
                                "Aceptar"
                            );
                        }
                    }
                    catch { }
                    finally
                    {
                        _dialogLock.Release();
                    }
                });
            }
            throw;
        }
        catch (Exception ex) when (ex is System.IO.IOException 
                                || ex.GetType().Name.Contains("Socket", StringComparison.OrdinalIgnoreCase)
                                || ex.Message.Contains("Socket", StringComparison.OrdinalIgnoreCase) 
                                || ex.Message.Contains("closed", StringComparison.OrdinalIgnoreCase)
                                || ex.Message.Contains("reset", StringComparison.OrdinalIgnoreCase))
        {
            if (!isLoginOrRegister)
            {
                await MainThread.InvokeOnMainThreadAsync(async () =>
                {
                    if (!await _dialogLock.WaitAsync(0)) return;
                    try
                    {
                        if (Application.Current?.MainPage != null)
                        {
                            await Application.Current.MainPage.DisplayAlert(
                                "Conexión Interrumpida", 
                                "La comunicación con el salón fue interrumpida o el servidor está iniciando. Por favor intente de nuevo en unos segundos.", 
                                "Aceptar"
                            );
                        }
                    }
                    catch { }
                    finally
                    {
                        _dialogLock.Release();
                    }
                });
            }
            throw new HttpRequestException("La conexión con el salón fue interrumpida o el servidor está iniciando.", ex);
        }

        // 3. Procesar códigos de error
        if (!response.IsSuccessStatusCode)
        {
            var content = await response.Content.ReadAsStringAsync(cancellationToken);
            ProblemDetailsDto? problem = null;

            try
            {
                if (!string.IsNullOrWhiteSpace(content))
                {
                    problem = JsonSerializer.Deserialize<ProblemDetailsDto>(content, new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    });
                }
            }
            catch
            {
                // Si la respuesta no es JSON válido (ej. 502/504 en HTML desde un proxy)
            }

            var isAuthEndpoint = request.RequestUri?.AbsolutePath.Contains("/auth/") == true;

            await MainThread.InvokeOnMainThreadAsync(async () =>
            {
                if (!await _dialogLock.WaitAsync(0)) return;
                try
                {
                    var mainPage = Application.Current?.MainPage;
                    if (mainPage == null) return;

                    switch (response.StatusCode)
                    {
                        case HttpStatusCode.Unauthorized:
                            // 401: Sesión expirada o no autorizada (solo peticiones protegidas de negocio)
                            if (!isAuthEndpoint)
                            {
                                SecureStorage.Default.Remove(ApiConstants.AuthTokenKey);
                                SecureStorage.Default.Remove(ApiConstants.RefreshTokenKey);
                                SecureStorage.Default.Remove(ApiConstants.UserRoleKey);
                                SecureStorage.Default.Remove(ApiConstants.UserIdKey);
                                await mainPage.DisplayAlert(
                                    "Sesión Finalizada", 
                                    "Tu sesión ha expirado. Por favor, ingresa de nuevo con tus credenciales.", 
                                    "Iniciar Sesión"
                                );
                                if (Shell.Current is AppShell appShell)
                                {
                                    appShell.SwitchToLogin();
                                }
                                else
                                {
                                    await Shell.Current.GoToAsync("//LoginPage");
                                }
                            }
                            break;

                        case HttpStatusCode.Forbidden:
                            // 403: Rol sin permisos (ignorar en intentos de login)
                            if (!isAuthEndpoint)
                            {
                                await mainPage.DisplayAlert(
                                    "Acceso Denegado", 
                                    "No posees los privilegios requeridos para realizar esta operación.", 
                                    "Entendido"
                                );
                            }
                            break;

                        case HttpStatusCode.Conflict:
                            // 409: Conflicto de concurrencia / Doble reserva
                            var conflictMsg = problem?.GetPrimaryErrorMessage() 
                                ?? "El horario seleccionado ya no se encuentra disponible. Por favor, elija otro turno.";
                            await mainPage.DisplayAlert("Horario No Disponible", conflictMsg, "Elegir Otro");
                            break;

                        case HttpStatusCode.BadRequest:
                        case HttpStatusCode.UnprocessableEntity:
                            // 400 / 422: Validaciones de negocio fallidas (solo si no es endpoint de auth)
                            if (!isAuthEndpoint)
                            {
                                var validationMsg = problem?.GetPrimaryErrorMessage() 
                                    ?? "Los datos ingresados no son válidos. Por favor, revíselos.";
                                await mainPage.DisplayAlert("Aviso de Validación", validationMsg, "Corregir");
                            }
                            break;

                        case HttpStatusCode.InternalServerError:
                        case HttpStatusCode.BadGateway:
                        case HttpStatusCode.ServiceUnavailable:
                        case HttpStatusCode.GatewayTimeout:
                            // 5xx: Fallas en servidor (solo peticiones protegidas de negocio; login y subidas manejan su propio mensaje)
                            var isUploadEndpoint = request.RequestUri?.AbsolutePath.Contains("/archivos/") == true;
                            if (!isAuthEndpoint && !isLoginOrRegister && !isUploadEndpoint)
                            {
                                var serverMessage = problem?.GetPrimaryErrorMessage();
                                if (!string.IsNullOrWhiteSpace(serverMessage) && !serverMessage.Contains("Internal Server Error", StringComparison.OrdinalIgnoreCase))
                                {
                                    await mainPage.DisplayAlert("Aviso del Salón", serverMessage, "Cerrar");
                                }
                                else
                                {
                                    await mainPage.DisplayAlert(
                                        "Aviso del Salón", 
                                        "El sistema del salón experimenta una interrupción momentánea. Intente nuevamente en unos minutos.", 
                                        "Cerrar"
                                    );
                                }
                            }
                            break;
                    }
                }
                catch { }
                finally
                {
                    _dialogLock.Release();
                }
            });
        }

        return response;
    }
}
