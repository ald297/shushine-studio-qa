namespace ShushineStudio.Mobile.Core.Constants;

public static class ApiConstants
{
    // 10.0.2.2 es la dirección IP especial para acceder al localhost de la máquina host desde el emulador de Android.
    // Para dispositivos físicos en la misma red local, usar la IP LAN de la máquina de desarrollo (ej. http://192.168.1.50:8080/api).
    public const string DefaultAndroidEmulatorBaseUrl = "http://10.0.2.2:8080/api";
    public const string DefaultIosSimulatorBaseUrl = "http://localhost:8080/api";
    public const string ProductionBaseUrl = "https://shushine-studio.onrender.com/api";

    public static string BaseUrl => ProductionBaseUrl;

    // Supabase
    public const string SupabaseUrl = "https://acikahicfjtojuvqcvxv.supabase.co";
    public const string SupabaseAnonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    // Storage Keys
    public const string AuthTokenKey = "shushine_jwt_token";
    public const string RefreshTokenKey = "shushine_refresh_token";
    public const string UserRoleKey = "shushine_user_role";
    public const string UserIdKey = "shushine_user_id";
}
