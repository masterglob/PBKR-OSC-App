package com.example.jchanddemo1.ui.projects;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jchanddemo1.PbkrContext;
import com.example.jchanddemo1.PbkrOSC;
import com.example.jchanddemo1.R;
import com.example.jchanddemo1.databinding.FragmentProjectsBinding;
import com.example.jchanddemo1.ui.home.HomeFragment;

public class ProjectsFragment extends Fragment {

    private FragmentProjectsBinding binding;
    private Handler mHandler;
    private ProjectChooser m_ProjectChooser;
    private TextView m_labelMenuL1;
    private TextView m_labelMenuL2;

    private class ProjectChooser {
        private final LinearLayout m_parent;
        private final ChooserButton m_buttons[];
        private class ChooserButton implements View.OnClickListener {
            Button m_button;
            int m_index;
            String m_command;

            public ChooserButton(Button button, int index){
                m_index = index;
                m_button = button;
                m_command = String.format("/pbkrmenu/selP%d", index+1);
                m_button.setClickable(false);
                m_button.setText("");
                m_button.setBackgroundColor(Color.GRAY);
                m_button.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                PbkrOSC.instance.send(m_command, 1.0f);
            }
        }

        public ProjectChooser(LinearLayout parent){
            m_parent = parent;
            m_buttons = new ChooserButton[m_parent.getChildCount()];
            for (int i = 0; i < m_parent.getChildCount(); i++) {
                m_buttons[i] = new ChooserButton((Button)m_parent.getChildAt(i), i);
            }
        }

        /**
         *
         * @param index Project index (first = 0)
         * @param title Project title
         */
        public void setProjectTitle(int index, String title){
            if (index >= 0 && index < m_parent.getChildCount() && title != null){
                Button button = m_buttons[index].m_button;
                if (button != null){
                    button.setText(title);
                }
            }
        }
        /**
         *
         * @param index Project index (first = 0)
         * @param isVisible True if is visible
         */
        public void setProjectVisible(int index, boolean isVisible){
            if (index >= 0 && index < m_parent.getChildCount()){
                Button button = m_buttons[index].m_button;
                if (button != null){
                    if (isVisible){
                        button.setClickable(true);
                    }else
                    {
                        button.setClickable(false);
                        button.setText(String.format(getString(R.string.no_project)));
                        button.setBackgroundColor(Color.GRAY);
                    }
                }
            }
        }

        /**
         *
         * @param index Project index (first = 0)
         * @param color Color name (supported: "gray" & "green")
         */
        public void setProjectColor(int index, String color){
            if (index >= 0 && index < m_parent.getChildCount() && color != null ){
                Button button = m_buttons[index].m_button;
                if (button != null){
                    if (color.equals("green")){
                        button.setBackgroundColor(Color.rgb(0,128,0));
                    }else if (color.equals("gray")){
                        button.setBackgroundColor(Color.GRAY);
                    }else
                    {
                        Log.e("setProjectColor","Unknown color:<" + color +">");
                    }
                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProjectsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(ProjectsViewModel.class);

        binding = FragmentProjectsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mHandler = new Handler();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        m_ProjectChooser = new ProjectChooser(root.findViewById(R.id.layoutProjects));
        m_labelMenuL1 = (TextView)root.findViewById(R.id.labelMenuL1);
        m_labelMenuL2 = (TextView)root.findViewById(R.id.labelMenuL2);
        ImageButton button;

        button = root.findViewById(R.id.btnKeyClose);
        button.setOnClickListener(v -> {PbkrOSC.instance.send("/pbkrmenu/menuCancel", 1.0f);});

        button = root.findViewById(R.id.btnKeyEnter);
        button.setOnClickListener(v -> {PbkrOSC.instance.send("/pbkrmenu/menuOk", 1.0f);});

        button = root.findViewById(R.id.btnKeyLeft);
        button.setOnClickListener(v -> {PbkrOSC.instance.send("/pbkrmenu/menuLeft", 1.0f);});

        button = root.findViewById(R.id.btnKeyRight);
        button.setOnClickListener(v -> {PbkrOSC.instance.send("/pbkrmenu/menuRight", 1.0f);});

        button = root.findViewById(R.id.btnKeyUp);
        button.setOnClickListener(v -> {PbkrOSC.instance.send("/pbkrmenu/menuUp", 1.0f);});
        button = root.findViewById(R.id.btnKeyDown);
        button.setOnClickListener(v -> {PbkrOSC.instance.send("/pbkrmenu/menuDown", 1.0f);});

        // refresh will cause the menu to be displayed
        PbkrOSC.instance.send("/pbkrctrl/pRefresh");

        for (int i = 0; i < PbkrContext.nbProjects; i++) {
            setProjectName(i, PbkrContext.instance.getProjectName(i));
            setProjectColor(i, PbkrContext.instance.getProjectColor(i));
            setProjectVisible(i, PbkrContext.instance.getProjectVisibility(i));
        }
        PbkrOSC.instance.setProjectPage(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        m_ProjectChooser = null;
        m_labelMenuL1 = null;
        m_labelMenuL2 = null;
        // PbkrOSC.instance.setProjectPage(null);
        binding = null;
    }

    public void setProjectName(int index, String name){
        mHandler.post(() ->{ if (m_ProjectChooser != null)  m_ProjectChooser.setProjectTitle(index, name);});
    }
    public void setProjectColor(int index, String color){
        mHandler.post(() ->{ if (m_ProjectChooser != null) m_ProjectChooser.setProjectColor(index, color);});
    }
    public void setProjectVisible(int index, boolean isVisible){
        mHandler.post(() ->{ if (m_ProjectChooser != null) m_ProjectChooser.setProjectVisible(index, isVisible);});
    }

    public void setMenuL1(String text){
        mHandler.post(() ->{ if (m_labelMenuL1 != null) m_labelMenuL1.setText(text);});
    }
    public void setMenuL2(String text){
        mHandler.post(() -> { if (m_labelMenuL2 != null)m_labelMenuL2.setText(text);});
    }
}