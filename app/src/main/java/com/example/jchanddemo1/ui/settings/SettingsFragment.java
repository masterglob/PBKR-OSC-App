package com.example.jchanddemo1.ui.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jchanddemo1.PbkrContext;
import com.example.jchanddemo1.PbkrOSC;
import com.example.jchanddemo1.R;
import com.example.jchanddemo1.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel dashboardViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        Log.d("SettingsFragment", "Creation");

        m_config = PbkrContext.instance;

        checkConfig();

        m_btnCheck = (Button) root.findViewById(R.id.btnConfigCheck);
        if (m_btnCheck != null)
        {
            m_btnCheck.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean result = PbkrOSC.instance.reconfigure();
                    if (result) {
                        m_btnCheck.setClickable(false);
                    }
                }
            });
        }

        return root;
    }

    public void checkConfig(){
        View root = binding.getRoot();
        m_etServer = (EditText) root.findViewById(R.id.editServerIp);
        if (m_etServer == null)
        {
            Log.e("SettingsFragment", "editServerIp is NULL");
        }
        else
        {
            m_etServer.setText(m_config.getServerIp());

            m_etServer.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    m_config.setServerIp(s.toString());
                    m_btnCheck.setClickable(true);
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }

        EditText edit = (EditText) root.findViewById(R.id.editServerPortIn);
        if (edit == null)
        {
            Log.e("SettingsFragment", "editServerPortIn is NULL");
        }
        else
        {
            edit.setText(String.valueOf(m_config.getServerPortIn()));

            edit.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        Integer v = Integer.parseInt(s.toString());
                        m_config.setServerPortIn(v);
                        m_btnCheck.setClickable(true);
                    }
                    catch (Exception e){}
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }

        edit = (EditText) root.findViewById(R.id.editServerPortOut);
        if (edit == null)
        {
            Log.e("SettingsFragment", "editServerPortOut is NULL");
        }
        else
        {
            edit.setText(String.valueOf(m_config.getServerPortOut()));

            edit.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try {
                        Integer v = Integer.parseInt(s.toString());
                        m_config.setServerPortOut(v);
                        m_btnCheck.setClickable(true);
                    }
                    catch (Exception e){}
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private EditText m_etServer = null;
    private PbkrContext m_config = null;
    private Button m_btnCheck = null;
}