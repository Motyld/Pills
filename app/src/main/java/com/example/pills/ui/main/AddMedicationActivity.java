package com.example.pills.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

public class AddMedicationActivity extends AppCompatActivity implements NavigationHost {

    public static final String EXTRA_EDIT_REMINDER_ID = "EDIT_REMINDER_ID";
    public static final String EXTRA_TEMPLATE_ID = "TEMPLATE_ID";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        if (savedInstanceState != null) return;

        long templateId = getIntent().getLongExtra(EXTRA_TEMPLATE_ID, -1);
        long editId = getIntent().getLongExtra(EXTRA_EDIT_REMINDER_ID, -1);

        if (templateId != -1) {
            DatabaseHelper db = new DatabaseHelper(this);
            ReminderDraft d = db.buildDraftFromTemplate(templateId);
            db.close();

            openFragment(TimeStepFragment.newInstance(d)); // дальше Date/Items/Save
            return;
        }

        if (editId != -1) {
            openFragment(ItemsStepFragment.newInstanceEdit(editId));
            return;
        }

        openFragment(new SearchMedicineFragment());
    }

    @Override
    public void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.add_med_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
