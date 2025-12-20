package com.example.pills.ui.main;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.pills.R;

public class AddMedicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        if (savedInstanceState == null) {
            // стартуем с фрагмента поиска/выбора лекарства
            openFragment(new AddMedicineFragment());
        }
    }

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.add_med_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
