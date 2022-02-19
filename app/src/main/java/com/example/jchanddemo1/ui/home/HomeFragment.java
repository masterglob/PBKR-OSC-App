package com.example.jchanddemo1.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jchanddemo1.PbkrContext;
import com.example.jchanddemo1.PbkrOSC;
import com.example.jchanddemo1.R;
import com.example.jchanddemo1.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TextView m_wgtProjectName = null;
    private TextView m_wgtTrackName = null;
    private TextView m_wgtTimeCode = null;
    private ImageButton m_wgtPlayBtn = null;
    private ImageButton m_wgtPauseBtn = null;
    private ImageButton m_wgtStopBtn = null;
    private ImageButton m_wgtBtnRefresh = null;
    private ImageButton m_wgtBtnFF = null;
    private ImageButton m_wgtBtnRew = null;
    private Handler mHandler;
    private int mStopActive = -1;
    private int mPlayActive = -1;
    private int mPauseActive = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        mHandler = new Handler();
        m_wgtProjectName = root.findViewById(R.id.text_projName);
        if (m_wgtProjectName != null)
        {
            m_wgtProjectName.setText(PbkrContext.instance.currentProject);
        }
        m_wgtTrackName = root.findViewById(R.id.tv_currTrack);
        if (m_wgtTrackName == null){
            m_wgtProjectName.setText (PbkrContext.instance.currentTrack);
            Toast toast = Toast.makeText(null, "Failed to build 'tv_currTrack", Toast.LENGTH_LONG);
            toast.show();
        }
        m_wgtPlayBtn = root.findViewById(R.id.imageBtnPlay);
        m_wgtPlayBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PbkrContext.instance.playStatus = -1;
                PbkrContext.instance.stopStatus = 1;
                PbkrOSC.instance.send("/pbkrctrl/pPlay", 1.0f);
                refreshPlayStatus();
            }
        });

        m_wgtStopBtn = root.findViewById(R.id.imageBtnStop);
        m_wgtStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PbkrContext.instance.playStatus = -1;
                PbkrContext.instance.stopStatus = -1;
                PbkrOSC.instance.send("/pbkrctrl/pStop", 1.0f);
                refreshPlayStatus();
            }
        });

        m_wgtPauseBtn = root.findViewById(R.id.imageBtnPause);
        m_wgtPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PbkrContext.instance.playStatus = -1;
                PbkrContext.instance.stopStatus = 1;
                PbkrOSC.instance.send("/pbkrctrl/pPause", 1.0f);
                refreshPlayStatus();
            }
        });

        m_wgtTimeCode = root.findViewById(R.id.labelTimeCode);

        m_wgtBtnRefresh = root.findViewById((R.id.btnSync));
        m_wgtBtnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PbkrContext.instance.playStatus = -1;
                PbkrContext.instance.stopStatus = 1;
                PbkrOSC.instance.send("/pbkrctrl/pPause", 1.0f);
                refreshPlayStatus();
            }
        });

        m_wgtBtnFF = root.findViewById(R.id.imageBtnFastForward);
        m_wgtBtnFF.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PbkrOSC.instance.send("/pbkrctrl/pFastForward", 1.0f);
            }
        });
        m_wgtBtnRew = root.findViewById(R.id.imageBtnRewind);
        m_wgtBtnRew.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PbkrOSC.instance.send("/pbkrctrl/pBackward", 1.0f);
            }
        });

        PbkrOSC.instance.setMainHomePage(this);

        refreshAll();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void refreshAll() {
        refreshCurrentTimeCode();
        refreshCurrentProjectName();
        refreshCurrentTrackName();
        refreshPlayStatus();
    }
    public void refreshCurrentTimeCode() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                m_wgtTimeCode.setText(PbkrContext.instance.currentTimeCode);
            }
        });
    }

    public void refreshCurrentProjectName() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                m_wgtProjectName.setText(PbkrContext.instance.currentProject);
            }
        });
    }

    public void refreshCurrentTrackName() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                m_wgtTrackName.setText("Track : " +PbkrContext.instance.currentTrack);
            }
        });
    }

    public void refreshPlayStatus() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int BLUE2 = 0xFF0099CC;
                final int bPlayActive = PbkrContext.instance.playStatus;
                final int bPauseActive = PbkrContext.instance.pauseStatus;
                final int bStopActive = PbkrContext.instance.stopStatus;
                if (bStopActive != mStopActive){
                    mStopActive = bStopActive;
                    m_wgtStopBtn.setClickable(bStopActive > 0);
                    m_wgtStopBtn.setBackgroundColor(bStopActive > 0? Color.RED : Color.GRAY);
                    m_wgtBtnFF.setBackgroundColor(bStopActive == 0? Color.GRAY : BLUE2);
                    m_wgtBtnRew.setBackgroundColor(bStopActive == 0? Color.GRAY : BLUE2);
                    m_wgtBtnFF.setClickable(bStopActive > 0);
                    m_wgtBtnRew.setClickable(bStopActive > 0);
                }

                if (bPlayActive != mPlayActive){
                    mPlayActive = bPlayActive;
                    m_wgtPlayBtn.setClickable(bPlayActive > 0);
                    m_wgtPlayBtn.setBackgroundColor(bPlayActive > 0 ? Color.GREEN : Color.GRAY);
                }

                if (bPauseActive != mPauseActive){
                    mPauseActive = bPauseActive;
                    m_wgtPauseBtn.setClickable(bPauseActive > 0);
                    m_wgtPauseBtn.setBackgroundColor(bPauseActive > 0 ? 0xFFFF8800 : Color.GRAY);
                }
            }
        });
    }
}