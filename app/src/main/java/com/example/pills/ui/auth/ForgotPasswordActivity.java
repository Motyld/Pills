package com.example.pills.ui.auth;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etLogin, etPin, etNewPass, etNewPass2;
    private Button btnReset;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        etLogin = findViewById(R.id.etFpLogin);
        etPin = findViewById(R.id.etFpPin);
        etNewPass = findViewById(R.id.etFpNewPass);
        etNewPass2 = findViewById(R.id.etFpNewPass2);

        btnReset = findViewById(R.id.btnFpReset);

        btnReset.setOnClickListener(v -> reset());
    }

    private void reset() {
        String login = etLogin.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String p1 = etNewPass.getText().toString().trim();
        String p2 = etNewPass2.getText().toString().trim();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(pin) || TextUtils.isEmpty(p1) || TextUtils.isEmpty(p2)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pin.length() != 4) {
            Toast.makeText(this, "PIN должен быть 4 цифры", Toast.LENGTH_SHORT).show();
            return;
        }

        if (p1.length() < 4 || p1.length() > 12) {
            Toast.makeText(this, "Пароль должен быть 4–12 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!p1.equals(p2)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery(
                "SELECT id FROM users WHERE login = ? AND pin = ?",
                new String[]{login, pin}
        );

        if (!c.moveToFirst()) {
            Toast.makeText(this, "Неверный логин или PIN", Toast.LENGTH_SHORT).show();
            c.close();
            db.close();
            return;
        }

        long userId = c.getLong(0);
        c.close();

        db.execSQL("UPDATE users SET password = ? WHERE id = ?",
                new Object[]{p1, userId});

        db.close();

        Toast.makeText(this, "Пароль обновлён", Toast.LENGTH_SHORT).show();
        finish();
    }
}
