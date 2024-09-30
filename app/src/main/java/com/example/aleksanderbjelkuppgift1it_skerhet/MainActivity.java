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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class MainActivity extends AppCompatActivity {

    private Button logInButton, registerButton;
    private EditText emailEditText, passwordEditText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        logInButton = findViewById(R.id.logInButton);
        registerButton = findViewById(R.id.registerButton);
        emailEditText = findViewById(R.id.emailEditTextText);
        passwordEditText = findViewById(R.id.passwordEditTextText);

        logInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            //kontrollerar att fälten inte är tomma
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "fyll i både email och lösenord!", Toast.LENGTH_SHORT).show();
                return;
            }


            //hashar det lösenordet här
            String hashedPassword = hashPassword(password);
            Log.d("Login", "Email: " + email);
            Log.d("Login", "Hashed Password: " + hashedPassword);

            //här hämtar jag data från Firestore
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean loginSuccessful = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String storedHashedPassword = document.getString("password");
                                Log.d("Login", "sparade hashen " + storedHashedPassword);

                                //jämför hashade lösenord
                                if (storedHashedPassword != null && storedHashedPassword.equals(hashedPassword)) {
                                    Toast.makeText(MainActivity.this, "Inloggning lyckades!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, Credentials.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    loginSuccessful = true;
                                    break;
                                }
                            }
                            if (!loginSuccessful) {
                                Toast.makeText(MainActivity.this, "Fel email eller lösenord.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "tyvärr ett fel vid inläsningen " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //metoden för att hasha lösenordet
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
