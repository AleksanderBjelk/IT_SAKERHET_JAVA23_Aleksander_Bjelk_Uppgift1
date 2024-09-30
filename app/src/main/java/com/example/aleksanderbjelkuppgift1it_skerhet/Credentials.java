package com.example.aleksanderbjelkuppgift1it_skerhet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Credentials extends AppCompatActivity {

    private Button saveCredentialsButton, deleteFirstNameButton, deleteSurnameButton, deletePhoneButton, deleteAdressButton;
    private ImageButton menuButton;
    private EditText firstNameEditText, surnameEditText, phoneEditText, addressEditText;
    private FirebaseFirestore db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_credentials);

        db = FirebaseFirestore.getInstance();
        userEmail = getIntent().getStringExtra("email");

        menuButton = findViewById(R.id.menuButton);
        saveCredentialsButton = findViewById(R.id.saveCredentialsButton);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        phoneEditText = findViewById(R.id.phonEditText);
        addressEditText = findViewById(R.id.adressEditText);
        deleteFirstNameButton = findViewById(R.id.deleteFirstNameButton);
        deleteSurnameButton = findViewById(R.id.deleteSurnameButton);
        deletePhoneButton = findViewById(R.id.deletePhoneButton);
        deleteAdressButton = findViewById(R.id.deleteAdressButton);


        loadUserData();

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(Credentials.this, Menu.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
        });

        saveCredentialsButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("firstName", firstName);
            updatedData.put("surname", surname);
            updatedData.put("phone", phone);
            updatedData.put("address", address);

            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String userId = task.getResult().getDocuments().get(0).getId();
                            db.collection("users").document(userId)
                                    .update(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Credentials.this, "uppgifternaa sparade!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Credentials.this, "misslyckades att spara uppgifter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("databas fel:", e.getMessage());
                                    });
                        } else {
                            Toast.makeText(Credentials.this, "anvädnare kunde inte hittas.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        deleteFirstNameButton.setOnClickListener(v -> deleteField("firstName"));

        deleteSurnameButton.setOnClickListener(v -> deleteField("surname"));


        deletePhoneButton.setOnClickListener(v -> deleteField("phone"));

        deleteAdressButton.setOnClickListener(v -> deleteField("address"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        String firstName = document.getString("firstName");
                        String surname = document.getString("surname");
                        String phone = document.getString("phone");
                        String address = document.getString("address");

                        firstNameEditText.setText(firstName);
                        surnameEditText.setText(surname);
                        phoneEditText.setText(phone);
                        addressEditText.setText(address);
                    } else {
                        Toast.makeText(Credentials.this, "ingen data hittades för användaren.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Credentials.this, "fel vid hämtning av datan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void deleteField(String fieldName) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String userId = task.getResult().getDocuments().get(0).getId();

                        db.collection("users").document(userId)
                                .update(fieldName, FieldValue.delete())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Credentials.this, fieldName + " raderad!", Toast.LENGTH_SHORT).show();
                                    switch (fieldName) {
                                        case "firstName":
                                            firstNameEditText.setText("");
                                            break;
                                        case "surname":
                                            surnameEditText.setText("");
                                            break;
                                        case "phone":
                                            phoneEditText.setText("");
                                            break;
                                        case "address":
                                            addressEditText.setText("");
                                            break;
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Credentials.this, "misslyckades att ta bort " + fieldName + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("databas fel:", e.getMessage());
                                });
                    } else {
                        Toast.makeText(Credentials.this, "användare kunde inte hittas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
