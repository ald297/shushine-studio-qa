using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using System.Collections.ObjectModel;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Chat;

/// <summary>
/// ViewModel para Solicitudes Personalizadas del cliente conectadas a Spring Boot y PostgreSQL.
/// </summary>
public partial class CustomRequestViewModel : ObservableObject
{
    private readonly ISolicitudRepository _solicitudRepository;

    [ObservableProperty]
    private bool _isLoading = false;

    [ObservableProperty]
    private bool _isSubmitting = false;

    [ObservableProperty]
    private bool _isUploadingImage = false;

    [ObservableProperty]
    private string _description = string.Empty;

    [ObservableProperty]
    private string _servicioDeseado = string.Empty;

    [ObservableProperty]
    private string? _attachedImageUrl;

    [ObservableProperty]
    private bool _hasAttachedImage = false;

    [ObservableProperty]
    private bool _hasError = false;

    [ObservableProperty]
    private string _errorMessage = string.Empty;

    [ObservableProperty]
    private bool _hasSuccessMessage = false;

    [ObservableProperty]
    private string _successMessage = string.Empty;

    public ObservableCollection<SolicitudDiseno> Requests { get; } = new();

    public bool HasRequests => Requests.Count > 0;
    public bool ShowEmptyState => !IsLoading && !HasError && !HasRequests;
    public bool IsNotSubmitting => !IsSubmitting && !IsUploadingImage;

    public CustomRequestViewModel(ISolicitudRepository solicitudRepository)
    {
        _solicitudRepository = solicitudRepository;
        _ = CargarSolicitudesAsync();
    }

    [RelayCommand]
    public async Task CargarSolicitudesAsync()
    {
        if (IsLoading) return;

        try
        {
            IsLoading = true;
            HasError = false;
            ErrorMessage = string.Empty;

            var list = await _solicitudRepository.GetMisSolicitudesAsync();
            Requests.Clear();
            foreach (var item in list)
            {
                Requests.Add(item);
            }

            OnPropertyChanged(nameof(HasRequests));
            OnPropertyChanged(nameof(ShowEmptyState));
        }
        catch (Exception ex)
        {
            HasError = true;
            ErrorMessage = "Error al cargar tus solicitudes: " + ex.Message;
        }
        finally
        {
            IsLoading = false;
            OnPropertyChanged(nameof(ShowEmptyState));
        }
    }

    [RelayCommand]
    private async Task SubmitRequestAsync()
    {
        if (string.IsNullOrWhiteSpace(Description))
        {
            HasError = true;
            ErrorMessage = "Por favor escribe una descripción de tu diseño.";
            return;
        }

        if (IsSubmitting) return;

        try
        {
            IsSubmitting = true;
            HasError = false;
            ErrorMessage = string.Empty;

            var creada = await _solicitudRepository.CrearSolicitudAsync(
                Description.Trim(),
                string.IsNullOrWhiteSpace(ServicioDeseado) ? null : ServicioDeseado.Trim(),
                AttachedImageUrl);

            Description = string.Empty;
            ServicioDeseado = string.Empty;
            AttachedImageUrl = null;
            HasAttachedImage = false;

            SuccessMessage = "¡Tu solicitud ha sido enviada con éxito! La revisaremos pronto.";
            HasSuccessMessage = true;

            await CargarSolicitudesAsync();

            _ = Task.Run(async () =>
            {
                await Task.Delay(4000);
                HasSuccessMessage = false;
                SuccessMessage = string.Empty;
            });
        }
        catch (Exception ex)
        {
            HasError = true;
            ErrorMessage = "No se pudo enviar la solicitud: " + ex.Message;
        }
        finally
        {
            IsSubmitting = false;
        }
    }

    [RelayCommand]
    private async Task AttachImageAsync()
    {
        if (IsUploadingImage) return;

        try
        {
            var accion = await Shell.Current.DisplayActionSheet(
                "Adjuntar imagen de referencia",
                "Cancelar",
                null,
                "Tomar foto con cámara",
                "Elegir de la galería");

            FileResult? result = null;

            if (accion == "Tomar foto con cámara")
            {
                var cameraStatus = await Permissions.CheckStatusAsync<Permissions.Camera>();
                if (cameraStatus != PermissionStatus.Granted)
                {
                    cameraStatus = await Permissions.RequestAsync<Permissions.Camera>();
                }

                if (cameraStatus != PermissionStatus.Granted)
                {
                    await Shell.Current.DisplayAlert(
                        "Permiso de Cámara",
                        "Para capturar una foto de referencia se requiere acceso a la cámara. Puedes habilitar el permiso en la configuración de tu teléfono o elegir una imagen desde tu galería.",
                        "Entendido"
                    );
                    return;
                }

                if (MediaPicker.Default.IsCaptureSupported)
                {
                    result = await MediaPicker.Default.CapturePhotoAsync();
                }
            }
            else if (accion == "Elegir de la galería")
            {
                result = await MediaPicker.Default.PickPhotoAsync();
            }

            if (result == null) return;

            IsUploadingImage = true;
            using var stream = await result.OpenReadAsync();
            var url = await _solicitudRepository.SubirImagenAsync(stream, result.FileName);

            if (!string.IsNullOrEmpty(url))
            {
                AttachedImageUrl = url;
                HasAttachedImage = true;
            }
            else
            {
                await Shell.Current.DisplayAlert("Error", "No se pudo subir la imagen al servidor.", "OK");
            }
        }
        catch (Exception ex)
        {
            await Shell.Current.DisplayAlert("Error", "Error al procesar la imagen: " + ex.Message, "OK");
        }
        finally
        {
            IsUploadingImage = false;
        }
    }

    [RelayCommand]
    private void QuitarImagen()
    {
        AttachedImageUrl = null;
        HasAttachedImage = false;
    }

    [RelayCommand]
    private async Task AceptarCotizacionAsync(SolicitudDiseno? solicitud)
    {
        await ResponderCotizacionInternoAsync(solicitud, true);
    }

    [RelayCommand]
    private async Task RechazarCotizacionAsync(SolicitudDiseno? solicitud)
    {
        await ResponderCotizacionInternoAsync(solicitud, false);
    }

    private async Task ResponderCotizacionInternoAsync(SolicitudDiseno? solicitud, bool aceptar)
    {
        if (solicitud == null || !solicitud.TieneCotizacion) return;

        var accion = aceptar ? "aceptar" : "rechazar";
        var confirm = await Shell.Current.DisplayAlert(
            "Confirmación",
            $"¿Estás segura de {accion} la cotización de ${solicitud.Cotizacion?.PrecioPropuesto:F2}?",
            "Sí, confirmar", "Cancelar");

        if (!confirm) return;

        try
        {
            IsLoading = true;
            await _solicitudRepository.ResponderCotizacionAsync(solicitud.Id, aceptar);
            await CargarSolicitudesAsync();

            if (aceptar)
            {
                var agendar = await Shell.Current.DisplayAlert(
                    "¡Cotización Aceptada!",
                    $"Has aceptado la propuesta de ${solicitud.Cotizacion?.PrecioPropuesto:F2}.\n\n¿Deseas agendar tu cita ahora para realizar este diseño en el salón?",
                    "Sí, Agendar Cita",
                    "Más tarde");

                if (agendar)
                {
                    await Shell.Current.GoToAsync("StylistSelectionPage");
                }
            }
            else
            {
                await Shell.Current.DisplayAlert("Cotización Rechazada", "Has rechazado la cotización. La solicitud ha quedado actualizada.", "Entendido");
            }
        }
        catch (Exception ex)
        {
            await Shell.Current.DisplayAlert("Error", "No se pudo actualizar la cotización: " + ex.Message, "OK");
        }
        finally
        {
            IsLoading = false;
        }
    }

    [RelayCommand]
    private async Task GoBackAsync()
    {
        try
        {
            if (Shell.Current?.Navigation?.NavigationStack?.Count > 1)
            {
                await Shell.Current.GoToAsync("..");
            }
            else
            {
                await Shell.Current.GoToAsync("//MainTabs/ChatPage");
            }
        }
        catch
        {
            try
            {
                await Shell.Current.GoToAsync("//MainTabs/ChatPage");
            }
            catch { }
        }
    }

    [RelayCommand]
    private void DismissError()
    {
        HasError = false;
        ErrorMessage = string.Empty;
    }
}
