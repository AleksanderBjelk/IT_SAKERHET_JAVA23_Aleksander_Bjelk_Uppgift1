package com.example.aleksanderbjelkuppgift1it_skerhet;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

public class Register extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button createUserButton, backToLoginButton;
    private EditText emailRegisterEditText, passwordRegisterEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        createUserButton = findViewById(R.id.createUserButton);
        emailRegisterEditText = findViewById(R.id.emailRegisterEditText);
        passwordRegisterEditText = findViewById(R.id.passwordRegisterEditText );
        backToLoginButton = findViewById(R.id.backToLogInButton);


        createUserButton.setOnClickListener(v -> {
            String email = emailRegisterEditText.getText().toString().trim();
            String password = passwordRegisterEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Register.this, "fyll i både email och lösenord!", Toast.LENGTH_SHORT).show();
                return;
            }

            //Kollar om emailen redan finns
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            Toast.makeText(Register.this, "den här emailen är upptagen!", Toast.LENGTH_SHORT).show();
                        } else {
                            //hashing lösenord
                            String hashedPassword = hashPassword(password);
                            Log.d("Register", "Hashed Password: " + hashedPassword);



                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            user.put("password", hashedPassword);

                            db.collection("users").add(user)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(Register.this, "Användare registrerad framgångsrikt!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Register.this, "Fel vid registrering av användare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        });

        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}