package com.example.aleksanderbjelkuppgift1it_skerhet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Menu extends AppCompatActivity {

    private Button formMenuButton, registerButton, logOutMenuButton, deleteAccountMenuButton;
    private ImageButton menuButton;
    private FirebaseFirestore db;
    private String userEmail; // To hold the user's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);


        db = FirebaseFirestore.getInstance();


        userEmail = getIntent().getStringExtra("email");

        menuButton = findViewById(R.id.menuButton);
        registerButton = findViewById(R.id.registerMenuButton);
        formMenuButton = findViewById(R.id.formMenuButton);
        logOutMenuButton = findViewById(R.id.logOutMenuButton);
        deleteAccountMenuButton = findViewById(R.id.deleteAccountMenuButton);

        menuButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, Register.class);
            startActivity(intent);
        });

        formMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, Credentials.class);
            intent.putExtra("email", userEmail); //för över den till credentials för att kunna radera kontot
            startActivity(intent);
        });

        logOutMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        deleteAccountMenuButton.setOnClickListener(v -> {
            deleteUserAccount();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void deleteUserAccount() {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String userId = task.getResult().getDocuments().get(0).getId();
                        db.collection("users").document(userId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Menu.this, "ditt konto har raderats!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Menu.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Menu.this, "misslyckades med att radera kontot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(Menu.this, "konto kunde inte hittas.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Menu.this, "fel vid hämtning av data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
