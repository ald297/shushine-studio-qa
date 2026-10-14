using ShushineStudio.Mobile.Domain.Entities;

namespace ShushineStudio.Mobile.Domain.Repositories;

public interface IChatRepository
{
    // === Operaciones para CLIENTE ===
    Task<ConversacionChat?> ObtenerOCrearConversacionClienteAsync();
    Task<List<ChatMessageItem>> ObtenerMensajesClienteAsync(int page = 0, int size = 50);
    Task<ChatMessageItem?> EnviarMensajeClienteAsync(string contenido, string? imagenUrl = null);
    Task<bool> MarcarMensajesLeidosClienteAsync();

    // === Operaciones para ADMINISTRADOR ===
    Task<List<ConversacionChat>> ObtenerConversacionesAdminAsync(int page = 0, int size = 50);
    Task<ConversacionChat?> ObtenerConversacionPorIdAdminAsync(int conversacionId);
    Task<List<ChatMessageItem>> ObtenerMensajesAdminAsync(int conversacionId, int page = 0, int size = 50);
    Task<ChatMessageItem?> EnviarMensajeAdminAsync(int conversacionId, string contenido, string? imagenUrl = null);
    Task<bool> MarcarMensajesLeidosAdminAsync(int conversacionId);

    // === Subida de Multimedia ===
    Task<string?> SubirImagenChatAsync(Stream stream, string nombreArchivo);
}
