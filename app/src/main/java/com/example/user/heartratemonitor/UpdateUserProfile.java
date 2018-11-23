package com.example.user.heartratemonitor;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateUserProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText firstNameField;
    private EditText lastNameField;
    private RadioGroup genderGroupField;
    private RadioButton genderField;

    private EditText ageField;
    private EditText weightField;
    private Button submitButton;
    private DatabaseReference userDetsilsDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_profile);

        firstNameField = findViewById(R.id.firstName);
        lastNameField = findViewById(R.id.lastName);
        genderGroupField = findViewById(R.id.gender);
        ageField = findViewById(R.id.age);
        weightField = findViewById(R.id.weight);

        mAuth = FirebaseAuth.getInstance();
        submitButton = (Button) findViewById(R.id.submit_user_details);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDetails();
            }
        });
        userDetsilsDB = FirebaseDatabase.getInstance().getReference("UserDetails");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UserDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                if(userDetails != null){
                    firstNameField.setText(userDetails.firstName);
                    lastNameField.setText(userDetails.lastName);
                    if(userDetails.gender.equals("Male")){
                        genderGroupField.check(R.id.male);
                    }
                    else {
                        genderGroupField.check(R.id.female);
                    }
                    ageField.setText(String.valueOf(userDetails.age));
                    weightField.setText(String.valueOf(userDetails.weight));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            //handle login
        }
    }

    public  void updateUserDetails(){
        String firstName = firstNameField.getText().toString().trim();
        String lastName = lastNameField.getText().toString().trim();
        genderField = (RadioButton)findViewById(genderGroupField.getCheckedRadioButtonId());
        String gender = genderField.getText().toString();
        int age = Integer.parseInt(ageField.getText().toString());
        float weight = Float.parseFloat(weightField.getText().toString());

        UserDetails user = new UserDetails(firstName,lastName,gender,age,weight);

        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference("UserDetails")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>(){

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                        }else{
                            //un successful message
                            Toast.makeText(UpdateUserProfile.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
