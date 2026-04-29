package com.chupchia.network;

import com.chupchia.network.request.LoginRequest;
import com.chupchia.network.request.RegisterRequest;
import com.chupchia.network.response.LoginResponse;
import com.chupchia.network.response.RegisterResponse;
import com.chupchia.network.response.BillResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("groups/{groupId}/bills")
    Call<BillResponse> getBills(@Path("groupId") String groupId);
}
