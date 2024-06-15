package com.example.belajar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.belajar.model.BaseResponse;
import com.example.belajar.model.User;
import com.example.belajar.request.BaseApiService;
import com.example.belajar.request.UtilsApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static User loggedUser = null;
    private EditText login_username;
    private EditText login_password;
    private Button login_button;
    private BaseApiService mApiService;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mContext = this;
        mApiService = UtilsApi.getApiService();

        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);

        login_button.setOnClickListener(v -> handleLogin());
    }

    public void handleLogin() {
        String username = login_username.getText().toString();
        String password = login_password.getText().toString();
        mApiService.login(password, username).enqueue(new Callback<BaseResponse<User>>() {
            @Override
            public void onResponse(Call<BaseResponse<User>> call, Response<BaseResponse<User>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<User> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.payload != null && baseResponse.message.equals("Login success")) {
                        loggedUser = baseResponse.payload;
                        System.out.println("Login success");
                        Toast.makeText(mContext, "Login success", Toast.LENGTH_SHORT).show();
                        if (loggedUser.role.equals("admin")) {
                            moveActivity(mContext, AdminMainActivity.class);
                        } else {
                            moveActivity(mContext, UserMainActivity.class);
                        }
                    } else {
                        System.out.println("Login failed");
                        Toast.makeText(mContext, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    System.out.println("Login failed");
                    Toast.makeText(mContext, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<User>> call, Throwable t) {
                System.out.println(t.getMessage());
                System.out.println("Login failed");
            }
        });
    }

    private void moveActivity(Context ctx, Class<?> cls) {
        Intent intent = new Intent(ctx, cls);
        startActivity(intent);
    }
}