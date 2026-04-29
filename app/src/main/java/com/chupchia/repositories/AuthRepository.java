package com.chupchia.repositories;

import android.content.Context;
import com.chupchia.network.ApiClient;
import com.chupchia.network.ApiService;
import com.chupchia.network.request.LoginRequest;
import com.chupchia.network.request.RegisterRequest;
import com.chupchia.network.response.LoginResponse;
import com.chupchia.network.response.RegisterResponse;
import com.chupchia.models.User;
import com.chupchia.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    public AuthRepository(Context context) {
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
        this.sharedPrefManager = SharedPrefManager.getInstance(context);
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void login(String username, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(username, password);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        sharedPrefManager.saveToken(response.body().getToken());
                        User user = response.body().getUser();
                        if (user != null) {
                            sharedPrefManager.saveUser(
                                user.getId(),
                                user.getFullName(),
                                user.getPhone(),
                                user.getEmail(),
                                user.getAvatarUrl()
                            );
                        }
                        sharedPrefManager.setLoggedIn(true);
                        callback.onSuccess(response.body().getMessage());
                    } else {
                        callback.onError(response.body().getMessage());
                    }
                } else {
                    callback.onError("Đăng nhập thất bại");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void register(String fullName, String phone, String email, String password, AuthCallback callback) {
        RegisterRequest request = new RegisterRequest(fullName, phone, email, password);
        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        callback.onSuccess(response.body().getMessage());
                    } else {
                        callback.onError(response.body().getMessage());
                    }
                } else {
                    callback.onError("Đăng ký thất bại");
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void logout() {
        sharedPrefManager.logout();
    }

    public boolean isLoggedIn() {
        return sharedPrefManager.getAuthToken() != null;
    }
}
