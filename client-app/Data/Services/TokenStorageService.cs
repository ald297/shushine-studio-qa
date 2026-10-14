using ShushineStudio.Mobile.Core.Constants;

namespace ShushineStudio.Mobile.Data.Services;

public interface ITokenStorageService
{
    Task SaveTokenAsync(string token, string? refreshToken = null, string? role = null, string? userId = null);
    Task<string?> GetTokenAsync();
    Task<string?> GetRoleAsync();
    Task<string?> GetUserIdAsync();
    Task<bool> HasValidTokenAsync();
    Task ClearAsync();
}

public class TokenStorageService : ITokenStorageService
{
    public async Task SaveTokenAsync(string token, string? refreshToken = null, string? role = null, string? userId = null)
    {
        await SecureStorage.Default.SetAsync(ApiConstants.AuthTokenKey, token);
        if (!string.IsNullOrEmpty(refreshToken))
            await SecureStorage.Default.SetAsync(ApiConstants.RefreshTokenKey, refreshToken);
        if (!string.IsNullOrEmpty(role))
            await SecureStorage.Default.SetAsync(ApiConstants.UserRoleKey, role);
        if (!string.IsNullOrEmpty(userId))
            await SecureStorage.Default.SetAsync(ApiConstants.UserIdKey, userId);
    }

    public async Task<string?> GetTokenAsync()
    {
        return await SecureStorage.Default.GetAsync(ApiConstants.AuthTokenKey);
    }

    public async Task<string?> GetRoleAsync()
    {
        return await SecureStorage.Default.GetAsync(ApiConstants.UserRoleKey);
    }

    public async Task<string?> GetUserIdAsync()
    {
        return await SecureStorage.Default.GetAsync(ApiConstants.UserIdKey);
    }

    public async Task<bool> HasValidTokenAsync()
    {
        var token = await GetTokenAsync();
        return !string.IsNullOrWhiteSpace(token);
    }

    public Task ClearAsync()
    {
        SecureStorage.Default.Remove(ApiConstants.AuthTokenKey);
        SecureStorage.Default.Remove(ApiConstants.RefreshTokenKey);
        SecureStorage.Default.Remove(ApiConstants.UserRoleKey);
        SecureStorage.Default.Remove(ApiConstants.UserIdKey);
        return Task.CompletedTask;
    }
}
