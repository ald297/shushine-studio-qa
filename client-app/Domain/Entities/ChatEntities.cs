using System.Text.Json.Serialization;

namespace ShushineStudio.Mobile.Domain.Entities;

public class ChatMessageItem
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("conversacionId")]
    public int ConversacionId { get; set; }

    [JsonPropertyName("contenido")]
    public string Content { get; set; } = string.Empty;

    [JsonPropertyName("imagenUrl")]
    public string? ImagenUrl { get; set; }

    [JsonPropertyName("idRemitente")]
    public string? IdRemitente { get; set; }

    [JsonPropertyName("nombreRemitente")]
    public string NombreRemitente { get; set; } = string.Empty;

    [JsonPropertyName("rolRemitente")]
    public string RolRemitente { get; set; } = string.Empty;

    [JsonPropertyName("esMio")]
    public bool EsMio { get; set; }

    [JsonPropertyName("fechaEnvio")]
    public DateTime Timestamp { get; set; } = DateTime.Now;

    [JsonPropertyName("leido")]
    public bool Leido { get; set; }

    // Propiedades de ayuda para bindings de la UI (.NET MAUI)
    public bool TieneImagen => !string.IsNullOrWhiteSpace(ImagenUrl);
    public bool TieneTexto => !string.IsNullOrWhiteSpace(Content) && Content != "[Foto adjunta]";
    public bool IsFromClient => EsMio;
    public bool IsNotFromClient => !IsFromClient;
    public bool NoEsMio => !EsMio;
    public string TimeDisplay => Timestamp.ToString("HH:mm");
}

public class ConversacionChat
{
    [JsonPropertyName("id")]
    public int Id { get; set; }

    [JsonPropertyName("idCliente")]
    public int IdCliente { get; set; }

    [JsonPropertyName("nombreCliente")]
    public string NombreCliente { get; set; } = string.Empty;

    [JsonPropertyName("telefonoCliente")]
    public string? TelefonoCliente { get; set; }

    [JsonPropertyName("ultimoMensaje")]
    public string UltimoMensaje { get; set; } = string.Empty;

    [JsonPropertyName("fechaUltimoMensaje")]
    public DateTime FechaUltimoMensaje { get; set; } = DateTime.Now;

    [JsonPropertyName("mensajesNoLeidos")]
    public long MensajesNoLeidos { get; set; }

    [JsonPropertyName("activa")]
    public bool Activa { get; set; } = true;

    [JsonPropertyName("mensajes")]
    public List<ChatMessageItem> Mensajes { get; set; } = new();

    public string FechaRelativaDisplay => FechaUltimoMensaje.ToString("HH:mm");
    public bool TieneMensajesNoLeidos => MensajesNoLeidos > 0;
}
