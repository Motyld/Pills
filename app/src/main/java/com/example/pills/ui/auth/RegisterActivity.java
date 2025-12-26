package com.example.pills.ui.auth;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText etLogin, etPassword, etPin, etFullName, etEmail, etPhone;
    private Button btnRegister;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etLogin = findViewById(R.id.etRegLogin);
        etPassword = findViewById(R.id.etRegPassword);
        etPin = findViewById(R.id.etRegPin);

        etFullName = findViewById(R.id.etRegFullName);
        etEmail = findViewById(R.id.etRegEmail);
        etPhone = findViewById(R.id.etRegPhone);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (login.length() < 4 || login.length() > 8) {
            Toast.makeText(this, "Логин должен быть 4–8 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4 || password.length() > 12) {
            Toast.makeText(this, "Пароль должен быть 4–12 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pin.length() != 4) {
            Toast.makeText(this, "PIN должен быть 4 цифры", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE login = ?",
                new String[]{login}
        );

        if (cursor.moveToFirst()) {
            Toast.makeText(this, "Такой логин уже существует", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
            return;
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("login", login);
        cv.put("password", password);
        cv.put("pin", pin);
        cv.put("fullName", fullName);
        cv.put("email", email);
        cv.put("phone", phone);

        long result = db.insert("users", null, cv);
        db.close();

        if (result > 0) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
        }
    }
}
