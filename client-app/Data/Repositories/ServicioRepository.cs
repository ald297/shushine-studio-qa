using System.Net.Http.Json;
using ShushineStudio.Mobile.Data.Dtos;
using ShushineStudio.Mobile.Domain.Entities;
using ShushineStudio.Mobile.Domain.Repositories;

namespace ShushineStudio.Mobile.Data.Repositories;

/// <summary>
/// Repositorio de servicios con soporte para Web API en Render y catálogo local de contingencia (Offline Fallback).
/// </summary>
public class ServicioRepository : IServicioRepository
{
    private readonly HttpClient _httpClient;

    // Catálogo oficial de belleza de Shushine Studio para garantizar funcionamiento continuo
    private static readonly List<Servicio> FallbackServicios = new()
    {
        new Servicio
        {
            Id = 1,
            Nombre = "Balayage Iluminador & Gloss",
            Descripcion = "Técnica francesa de aclarado degradado a mano alzada con baño de brillo nutritivo.",
            Precio = 65.00m,
            DuracionMinutos = 120,
            CategoriaNombre = "Cabello",
            Protocolo = "1. Diagnóstico capilar y prueba de mecha.\n2. Aclarado degradado profesional.\n3. Lavado con champú neutralizante.\n4. Matizado gloss hidratante.\n5. Peinado y sellado con aceite de argán.",
            Activo = true
        },
        new Servicio
        {
            Id = 2,
            Nombre = "Corte de Autor & Cepillado",
            Descripcion = "Diseño de corte personalizado según morfología facial con lavado dermocalmante.",
            Precio = 25.00m,
            DuracionMinutos = 45,
            CategoriaNombre = "Cabello",
            Protocolo = "1. Asesoría de visagismo.\n2. Lavado relajante con masaje capilar.\n3. Corte de precisión en húmedo.\n4. Brushing y peinado con protector térmico.",
            Activo = true
        },
        new Servicio
        {
            Id = 3,
            Nombre = "Manicura Rusa & Esmaltado Semi",
            Descripcion = "Limpieza profunda de cutículas con torno y esmaltado de alta duración gelish.",
            Precio = 22.00m,
            DuracionMinutos = 60,
            CategoriaNombre = "Uñas",
            Protocolo = "1. Higienización de manos.\n2. Tratamiento de cutícula con fresas de diamante.\n3. Nivelación de placa ungueal con base rubber.\n4. Aplicación de color y top coat ultraviolento.\n5. Hidratación con aceite de jojoba.",
            Activo = true
        },
        new Servicio
        {
            Id = 4,
            Nombre = "Pedicura Spa Rejuvenecedora",
            Descripcion = "Exfoliación con sales minerales, mascarilla de parafina y esmaltado profesional.",
            Precio = 28.00m,
            DuracionMinutos = 60,
            CategoriaNombre = "Uñas",
            Protocolo = "1. Inmersión en sales marinas y esencias florales.\n2. Exfoliación dermo-renovadora.\n3. Tratamiento intensivo de talones.\n4. Esmaltado y masaje podal relajante.",
            Activo = true
        },
        new Servicio
        {
            Id = 5,
            Nombre = "Lifting de Pestañas & Keratina",
            Descripcion = "Curvatura natural de pestañas con nutrición intensiva de keratina y tinte negro profundo.",
            Precio = 30.00m,
            DuracionMinutos = 50,
            CategoriaNombre = "Maquillaje",
            Protocolo = "1. Limpieza y desengrasado de la zona ocular.\n2. Colocación de moldes de silicona.\n3. Aplicación de loción moldeadora y fijadora.\n4. Tinte de pestañas.\n5. Baño de nutrición con botox capilar y keratina.",
            Activo = true
        },
        new Servicio
        {
            Id = 6,
            Nombre = "Diseño & Laminado de Cejas",
            Descripcion = "Depilación con hilo orgánico, laminado y perfilado de mirada.",
            Precio = 20.00m,
            DuracionMinutos = 40,
            CategoriaNombre = "Maquillaje",
            Protocolo = "1. Visagismo y diseño según proporciones faciales.\n2. Alisado y laminado de cejas.\n3. Depilación de precisión con hilo.\n4. Tinte híbrido opcional y sérum fijador.",
            Activo = true
        },
        new Servicio
        {
            Id = 7,
            Nombre = "Masaje Relajante Aromaterapia",
            Descripcion = "Sesión corporal completa con aceites esenciales de lavanda y piedras calientes.",
            Precio = 45.00m,
            DuracionMinutos = 60,
            CategoriaNombre = "Spa",
            Protocolo = "1. Ritual de respiración con aromaterapia.\n2. Masaje descontracturante suave en espalda y cuello.\n3. Aplicación de piedras volcánicas calientes.\n4. Té relajante de cortesía.",
            Activo = true
        }
    };

    public ServicioRepository(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<Servicio>> GetServiciosAsync(long? categoriaId = null)
    {
        try
        {
            // Intentar endpoint de listado rápido optimizado de Render: servicios/lista
            var url = categoriaId.HasValue 
                ? $"servicios/lista?categoriaId={categoriaId.Value}" 
                : "servicios/lista";

            var dtos = await _httpClient.GetFromJsonAsync<List<ServicioDto>>(url);
            if (dtos != null && dtos.Count > 0)
            {
                return dtos.Select(d => d.ToEntity());
            }
        }
        catch
        {
            // Fallback a endpoint estándar servicios
            try
            {
                var url = categoriaId.HasValue 
                    ? $"servicios?categoriaId={categoriaId.Value}" 
                    : "servicios";

                var dtos = await _httpClient.GetFromJsonAsync<List<ServicioDto>>(url);
                if (dtos != null && dtos.Count > 0)
                {
                    return dtos.Select(d => d.ToEntity());
                }
            }
            catch
            {
                // Silencioso para contingencia offline
            }
        }

        // Contingencia offline
        return categoriaId.HasValue 
            ? FallbackServicios.Where(s => s.CategoriaId == categoriaId.Value) 
            : FallbackServicios;
    }

    public async Task<Servicio?> GetServicioByIdAsync(long id)
    {
        try
        {
            var dto = await _httpClient.GetFromJsonAsync<ServicioDto>($"servicios/{id}");
            if (dto != null)
            {
                return dto.ToEntity();
            }
        }
        catch
        {
            // Contingencia offline
        }

        return FallbackServicios.FirstOrDefault(s => s.Id == id) ?? FallbackServicios.First();
    }

    public async Task<IEnumerable<string>> GetCategoriasAsync()
    {
        try
        {
            var categorias = await _httpClient.GetFromJsonAsync<List<CategoriaDto>>("categorias/lista");
            if (categorias != null && categorias.Count > 0)
            {
                var nombres = new List<string> { "Todos" };
                nombres.AddRange(categorias.Where(c => c.Activo && !string.IsNullOrWhiteSpace(c.Nombre)).Select(c => c.Nombre));
                return nombres;
            }
        }
        catch
        {
            // Contingencia offline si no hay red
        }

        return new List<string> { "Todos", "Cabello", "Uñas", "Maquillaje", "Spa" };
    }

    public async Task<Servicio?> CrearServicioAsync(Servicio servicio)
    {
        try
        {
            var guardarDto = new ServicioGuardarDto
            {
                CodigoServicio = !string.IsNullOrWhiteSpace(servicio.CodigoServicio) ? servicio.CodigoServicio : $"SRV-{Random.Shared.Next(100, 999)}",
                CategoriaId = (int)(servicio.CategoriaId ?? 1),
                Nombre = servicio.Nombre,
                Descripcion = servicio.Descripcion ?? string.Empty,
                PrecioBase = servicio.Precio,
                EsPrecioVariable = false,
                DuracionMinutos = servicio.DuracionMinutos > 0 ? servicio.DuracionMinutos : 45,
                IntervaloSeguimientoDias = 30,
                ImagenUrl = SanitizarImagenUrl(servicio.ImagenUrl),
                CostoInsumos = 0
            };

            var response = await _httpClient.PostAsJsonAsync("servicios", guardarDto);
            if (response.IsSuccessStatusCode)
            {
                var createdDto = await response.Content.ReadFromJsonAsync<ServicioDto>();
                if (createdDto != null)
                {
                    return createdDto.ToEntity();
                }
            }
        }
        catch
        {
            // Silencioso o log
        }
        return null;
    }

    public async Task<bool> ActualizarServicioAsync(Servicio servicio)
    {
        try
        {
            var modificarDto = new ServicioModificarDto
            {
                Id = (int)servicio.Id,
                CodigoServicio = servicio.CodigoServicio,
                CategoriaId = (int)(servicio.CategoriaId ?? 1),
                Nombre = servicio.Nombre,
                Descripcion = servicio.Descripcion,
                PrecioBase = servicio.Precio,
                EsPrecioVariable = false,
                DuracionMinutos = servicio.DuracionMinutos > 0 ? servicio.DuracionMinutos : 45,
                IntervaloSeguimientoDias = 30,
                ImagenUrl = SanitizarImagenUrl(servicio.ImagenUrl),
                CostoInsumos = 0,
                Activo = servicio.Activo
            };

            var response = await _httpClient.PutAsJsonAsync($"servicios/{servicio.Id}", modificarDto);
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }

    public async Task<bool> EliminarServicioAsync(long id)
    {
        try
        {
            var response = await _httpClient.DeleteAsync($"servicios/{id}");
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }

    public async Task<string?> SubirImagenAsync(Stream stream, string nombreArchivo)
    {
        try
        {
            using var content = new MultipartFormDataContent();
            var streamContent = new StreamContent(stream);
            var extension = Path.GetExtension(nombreArchivo).ToLowerInvariant();
            var mime = extension switch
            {
                ".png" => "image/png",
                ".webp" => "image/webp",
                _ => "image/jpeg"
            };
            streamContent.Headers.ContentType = new System.Net.Http.Headers.MediaTypeHeaderValue(mime);
            content.Add(streamContent, "archivo", nombreArchivo);

            var response = await _httpClient.PostAsync("archivos/subir", content);
            if (!response.IsSuccessStatusCode) return null;

            var json = await response.Content.ReadAsStringAsync();
            using var doc = System.Text.Json.JsonDocument.Parse(json);
            if (doc.RootElement.TryGetProperty("url", out var urlProp))
            {
                return urlProp.GetString();
            }
            return null;
        }
        catch
        {
            return null;
        }
    }

    private static string? SanitizarImagenUrl(string? url)
    {
        if (string.IsNullOrWhiteSpace(url)) return null;
        var lower = url.Trim().ToLowerInvariant();
        if (lower.StartsWith("/data/") || lower.StartsWith("file:") || lower.Contains("/cache/") 
            || lower.StartsWith("c:\\") || lower.StartsWith("/storage/emulated/") || lower.StartsWith("/sdcard/"))
        {
            return null;
        }
        return url.Trim();
    }
}

