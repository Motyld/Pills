package com.example.pills.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;
import com.example.pills.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private Button btnLogin, btnGoRegister;
    private TextView tvForgot;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ---------- АВТОВХОД ----------
        SharedPreferences pref = getSharedPreferences("auth", MODE_PRIVATE);
        if (pref.getBoolean("logged", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);
        tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> loginUser());

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void loginUser() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE login = ? AND password = ?",
                new String[]{login, password}
        );

        if (cursor.moveToFirst()) {
            SharedPreferences pref = getSharedPreferences("auth", MODE_PRIVATE);
            pref.edit().putBoolean("logged", true).apply();

            Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }
}
