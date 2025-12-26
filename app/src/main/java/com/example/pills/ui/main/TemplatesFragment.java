package com.example.pills.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pills.R;
import com.example.pills.db.DatabaseHelper;

import java.util.ArrayList;

public class TemplatesFragment extends Fragment implements TemplatesAdapter.OnTemplateAction {

    public TemplatesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_templates, container, false);

        RecyclerView rv = v.findViewById(R.id.rvTemplates);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        DatabaseHelper db = new DatabaseHelper(requireContext());
        ArrayList<TemplateRecord> list = db.getAllTemplates();
        db.close();

        rv.setAdapter(new TemplatesAdapter(list, this));
        return v;
    }

    @Override
    public void onRepeat(long templateId) {
        Intent i = new Intent(requireContext(), AddMedicationActivity.class);
        i.putExtra(AddMedicationActivity.EXTRA_TEMPLATE_ID, templateId);
        startActivity(i);
    }

    @Override
    public void onDelete(long templateId) {
        DatabaseHelper db = new DatabaseHelper(requireContext());
        db.deleteTemplate(templateId);
        db.close();
        Toast.makeText(requireContext(), "Шаблон удалён", Toast.LENGTH_SHORT).show();

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new TemplatesFragment())
                .commit();
    }
}
