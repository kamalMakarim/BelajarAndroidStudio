package com.example.belajar.request;

import com.example.belajar.model.BaseResponse;
import com.example.belajar.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface BaseApiService {
    @FormUrlEncoded
    @POST("user/logins")
    Call<BaseResponse<User>> login(@Field("password") String password, @Field("username") String username);
}
