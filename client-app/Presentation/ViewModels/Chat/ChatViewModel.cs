using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using System.Collections.ObjectModel;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Presentation.ViewModels.Chat;

public partial class ChatViewModel : ObservableObject
{
    private readonly IChatRepository _chatRepository;
    private CancellationTokenSource? _pollingCts;

    [ObservableProperty]
    private bool _isLoading = false;

    [ObservableProperty]
    private bool _isSending = false;

    [ObservableProperty]
    private string _messageText = string.Empty;

    [ObservableProperty]
    private bool _hasError = false;

    [ObservableProperty]
    private string _errorMessage = string.Empty;

    public ObservableCollection<ChatMessageItem> Messages { get; } = new();

    public bool HasMessages => Messages.Count > 0;
    public bool ShowEmptyState => !IsLoading && !HasError && !HasMessages;
    public bool ShowUnavailableBanner => false; // Backend real activo y conectado
    public bool IsNotSending => !IsSending;

    public ChatViewModel(IChatRepository chatRepository)
    {
        _chatRepository = chatRepository;
    }

    public async Task IniciarChatAsync()
    {
        await CargarMensajesAsync();
        IniciarPolling();
    }

    public async Task CargarMensajesAsync()
    {
        if (IsLoading) return;

        try
        {
            IsLoading = true;
            HasError = false;
            ErrorMessage = string.Empty;

            var items = await _chatRepository.ObtenerMensajesClienteAsync(0, 50);

            Messages.Clear();
            // Orden cronológico: los más antiguos primero
            var ordenados = items.OrderBy(m => m.Timestamp).ToList();
            foreach (var m in ordenados)
            {
                Messages.Add(new ChatMessageItem
                {
                    Id = m.Id,
                    ConversacionId = m.ConversacionId,
                    Content = m.Content,
                    ImagenUrl = m.ImagenUrl,
                    IdRemitente = m.IdRemitente,
                    NombreRemitente = m.NombreRemitente,
                    RolRemitente = m.RolRemitente,
                    EsMio = m.EsMio,
                    Timestamp = m.Timestamp,
                    Leido = m.Leido
                });
            }

            NotificarCambiosColeccion();

            // Marcar mensajes como leídos en backend de forma segura
            _ = _chatRepository.MarcarMensajesLeidosClienteAsync();
        }
        catch (Exception ex)
        {
            if (ex.Message.Contains("conversaci", StringComparison.OrdinalIgnoreCase) || 
                ex.Message.Contains("not found", StringComparison.OrdinalIgnoreCase))
            {
                // Si el cliente aún no tiene conversación, es un estado vacío normal y no un error
                HasError = false;
                ErrorMessage = string.Empty;
            }
            else
            {
                HasError = true;
                ErrorMessage = "No se pudieron cargar los mensajes. " + ex.Message;
            }
        }
        finally
        {
            IsLoading = false;
            NotificarCambiosColeccion();
        }
    }

    [RelayCommand]
    private void IniciarConversacion()
    {
        MessageText = "¡Hola! Quisiera información sobre sus servicios.";
    }

    public void IniciarPolling()
    {
        DetenerPolling();
        _pollingCts = new CancellationTokenSource();
        var token = _pollingCts.Token;

        _ = Task.Run(async () =>
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(4000, token);
                    if (token.IsCancellationRequested) break;

                    var nuevos = await _chatRepository.ObtenerMensajesClienteAsync(0, 50);
                    if (nuevos != null && nuevos.Count > 0)
                    {
                        var ordenados = nuevos.OrderBy(m => m.Timestamp).ToList();

                        MainThread.BeginInvokeOnMainThread(() =>
                        {
                            var idsExistentes = Messages.Select(x => x.Id).ToHashSet();
                            bool huboNuevos = false;

                            foreach (var item in ordenados)
                            {
                                if (!idsExistentes.Contains(item.Id))
                                {
                                    Messages.Add(new ChatMessageItem
                                    {
                                        Id = item.Id,
                                        ConversacionId = item.ConversacionId,
                                        Content = item.Content,
                                        ImagenUrl = item.ImagenUrl,
                                        IdRemitente = item.IdRemitente,
                                        NombreRemitente = item.NombreRemitente,
                                        RolRemitente = item.RolRemitente,
                                        EsMio = item.EsMio,
                                        Timestamp = item.Timestamp,
                                        Leido = item.Leido
                                    });
                                    huboNuevos = true;
                                }
                            }

                            if (huboNuevos)
                            {
                                NotificarCambiosColeccion();
                                _ = _chatRepository.MarcarMensajesLeidosClienteAsync();
                            }
                        });
                    }
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch (ObjectDisposedException)
                {
                    break;
                }
                catch
                {
                    // Error transitorio en sondeo silencioso
                }
            }
        }, token);
    }

    public void DetenerPolling()
    {
        try
        {
            _pollingCts?.Cancel();
        }
        catch
        {
            // Ignorar excepciones de cancelación
        }
        finally
        {
            _pollingCts = null;
        }
    }

    [RelayCommand]
    private async Task AttachImageAsync()
    {
        if (IsSending) return;

        try
        {
            var accion = await Shell.Current.DisplayActionSheet(
                "Enviar foto",
                "Cancelar",
                null,
                "Tomar foto con cámara",
                "Elegir de galería");

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
                        "Para capturar una foto se requiere acceso a la cámara. Puedes habilitar el permiso en la configuración de tu teléfono o elegir una imagen desde tu galería.",
                        "Entendido"
                    );
                    return;
                }

                if (MediaPicker.Default.IsCaptureSupported)
                {
                    result = await MediaPicker.Default.CapturePhotoAsync();
                }
            }
            else if (accion == "Elegir de galería")
            {
                result = await MediaPicker.Default.PickPhotoAsync();
            }

            if (result == null) return;

            IsSending = true;
            OnPropertyChanged(nameof(IsNotSending));

            using var stream = await result.OpenReadAsync();
            var imageUrl = await _chatRepository.SubirImagenChatAsync(stream, result.FileName);

            if (!string.IsNullOrEmpty(imageUrl))
            {
                var enviado = await _chatRepository.EnviarMensajeClienteAsync(string.Empty, imageUrl);
                if (enviado != null)
                {
                    Messages.Add(new ChatMessageItem
                    {
                        Id = enviado.Id,
                        ConversacionId = enviado.ConversacionId,
                        Content = enviado.Content,
                        ImagenUrl = enviado.ImagenUrl,
                        IdRemitente = enviado.IdRemitente,
                        NombreRemitente = enviado.NombreRemitente,
                        RolRemitente = enviado.RolRemitente,
                        EsMio = true,
                        Timestamp = enviado.Timestamp,
                        Leido = enviado.Leido
                    });
                    NotificarCambiosColeccion();
                }
            }
            else
            {
                await Shell.Current.DisplayAlert("Error", "No se pudo subir la imagen al servidor.", "OK");
            }
        }
        catch (Exception ex)
        {
            await Shell.Current.DisplayAlert("Error", "Error al procesar la foto: " + ex.Message, "OK");
        }
        finally
        {
            IsSending = false;
            OnPropertyChanged(nameof(IsNotSending));
        }
    }

    [RelayCommand]
    private async Task SendMessageAsync()
    {
        var texto = MessageText?.Trim();
        if (string.IsNullOrWhiteSpace(texto)) return;
        if (IsSending) return;

        try
        {
            IsSending = true;
            OnPropertyChanged(nameof(IsNotSending));
            HasError = false;
            ErrorMessage = string.Empty;

            var enviado = await _chatRepository.EnviarMensajeClienteAsync(texto);
            if (enviado != null)
            {
                Messages.Add(new ChatMessageItem
                {
                    Id = enviado.Id,
                    ConversacionId = enviado.ConversacionId,
                    Content = enviado.Content,
                    ImagenUrl = enviado.ImagenUrl,
                    IdRemitente = enviado.IdRemitente,
                    NombreRemitente = enviado.NombreRemitente,
                    RolRemitente = enviado.RolRemitente,
                    EsMio = true,
                    Timestamp = enviado.Timestamp,
                    Leido = enviado.Leido
                });

                MessageText = string.Empty;
                NotificarCambiosColeccion();
            }
            else
            {
                HasError = true;
                ErrorMessage = "No fue posible enviar el mensaje al servidor del salón. Por favor verifique su conexión o intente nuevamente.";
            }
        }
        catch (Exception ex)
        {
            ErrorMessage = "Error al enviar mensaje: " + ex.Message;
            HasError = true;
        }
        finally
        {
            IsSending = false;
            OnPropertyChanged(nameof(IsNotSending));
        }
    }

    private void NotificarCambiosColeccion()
    {
        OnPropertyChanged(nameof(HasMessages));
        OnPropertyChanged(nameof(ShowEmptyState));
    }

    [RelayCommand]
    private async Task NavigateToCustomRequestAsync()
    {
        await Shell.Current.GoToAsync("CustomRequestPage");
    }

    [RelayCommand]
    private async Task GoBackAsync()
    {
        DetenerPolling();
        try
        {
            if (Shell.Current?.Navigation?.NavigationStack?.Count > 1)
            {
                await Shell.Current.GoToAsync("..");
            }
            else
            {
                await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
            }
        }
        catch
        {
            try
            {
                if (Shell.Current != null)
                {
                    await Shell.Current.GoToAsync("//MainTabs/CatalogPage");
                }
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

/// <summary>
/// Alias / subclase de ChatMessageItem en el namespace de ViewModels para enlace directo con XAML.
/// </summary>
public class ChatMessageItem : ShushineStudio.Mobile.Domain.Entities.ChatMessageItem
{
}
