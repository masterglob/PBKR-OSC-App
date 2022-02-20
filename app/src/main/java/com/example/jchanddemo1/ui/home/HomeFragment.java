package com.example.jchanddemo1.ui.home;

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
    private ButtonXYMap m_trackButtons = null;
    private Handler mHandler;
    private int mStopActive = -1;
    private int mPlayActive = -1;
    private int mPauseActive = -1;

    private static class ButtonXYMap{
        final int PURPLE500 = 0xFF6200EE;
        ButtonXYMap(LinearLayout parent){
            m_currentTrackX = -1;
            m_currentTrackY = -1;
            m_parent = parent;
            m_nbLines = m_parent.getChildCount();
            LinearLayout subParent =  (LinearLayout) m_parent.getChildAt(0);
            m_nbColumn = subParent.getChildCount();
            Log.i("ButtonXYMap", "Nb buttons : " + (m_nbColumn * m_nbLines));

            for (int x = 0 ; x < m_nbColumn; x++){
                for (int y = 0 ; y < m_nbLines; y++){
                    Button button = getButtonAt(x,y);
                    if (button != null) {
                        button.setOnClickListener(new ButtonXYClickListener(x, y));
                        setButtonState(x, y, "");
                    }
                }
            }
            setActiveTrackId(PbkrContext.instance.getCurrentTrack());
        }

        public void setActiveTrackId(int trackId) {
            // Log.e("setActiveTrackId", "TrackId= "+ trackId);
            Button button = getButtonAt(m_currentTrackX,m_currentTrackY);
            if (button != null){
                button.setBackgroundColor(PURPLE500);
            }
            m_currentTrackX = trackId % m_nbColumn;
            m_currentTrackY = trackId / m_nbColumn;
            // Log.e("setActiveTrackId", "m_currentTrackX= "+ m_currentTrackX +",  m_currentTrackY="+m_currentTrackY);
            button = getButtonAt(m_currentTrackX,m_currentTrackY);
            if (button != null){
                button.setBackgroundColor(Color.RED);
            }
        }

        /**
         * @param trackId Track index (First track is 1)
         * @param title Track title
         */
        public void setTrackName(int trackId, String title) {
            trackId -= 1;
            int x = trackId % m_nbColumn;
            int y = trackId / m_nbColumn;
            setButtonState(x, y, title);
        }

        private class ButtonXYClickListener implements  View.OnClickListener{
            private final String m_cmd;

            ButtonXYClickListener(int x, int y){
                final int trackId = indexOf(x,y);
                m_cmd = "/pbkrctrl/mtTrackSel/"+ PbkrOSC.trackPadY(trackId)+"/"+ PbkrOSC.trackPadX(trackId);
            }
            @Override
            public void onClick(View view) {
                PbkrOSC.instance.send(m_cmd, 1.0f);
            }
        }

        private int indexOf(int x, int y){
            return x + (y * m_nbColumn);
        }

        private void setButtonState(int x, int y, String title){
            Button button = getButtonAt(x,y);
            final boolean isActive = (x == m_currentTrackX && y ==m_currentTrackY);
            if (button != null && title != null){
                if (title.isEmpty()){
                    if (button.isClickable()) {
                        // Clear (empty)
                        button.setClickable(false);
                        button.setText(String.format("No track %d", 1 + indexOf(x, y)));
                        button.setBackgroundColor(Color.GRAY);
                    }
                    if (isActive){
                        m_currentTrackX = -1;
                        m_currentTrackY = -1;
                    }
                }
                else{
                    // Button is activable
                    int bgColor = (isActive ? Color.RED : PURPLE500 );
                    if (!button.isClickable() || !button.getText().equals(title)) {
                        button.setClickable(true);
                        button.setBackgroundColor(bgColor);
                        button.setText(title);
                    }
                }
            }
        }

        public Button getButtonAt(int x, int y){
            if (x < 0 || y < 0 || y >= m_nbLines){
                return null;
            }
            LinearLayout subParent = (LinearLayout) m_parent.getChildAt(y);
            if (x >= subParent.getChildCount()){
                return null;
            }
            return (Button)subParent.getChildAt(x);
        }

        final private LinearLayout m_parent;
        private final int m_nbLines;
        private final int m_nbColumn;
        private int m_currentTrackX;
        private int m_currentTrackY;
    }

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
        m_wgtPlayBtn.setOnClickListener(v -> {
            PbkrContext.instance.playStatus = -1;
            PbkrContext.instance.stopStatus = 1;
            PbkrOSC.instance.send("/pbkrctrl/pPlay", 1.0f);
            refreshPlayStatus();
        });

        m_wgtStopBtn = root.findViewById(R.id.imageBtnStop);
        m_wgtStopBtn.setOnClickListener(v -> {
            PbkrContext.instance.playStatus = -1;
            PbkrContext.instance.stopStatus = -1;
            PbkrOSC.instance.send("/pbkrctrl/pStop", 1.0f);
            refreshPlayStatus();
        });

        m_wgtPauseBtn = root.findViewById(R.id.imageBtnPause);
        m_wgtPauseBtn.setOnClickListener(v -> {
            PbkrContext.instance.playStatus = -1;
            PbkrContext.instance.stopStatus = 1;
            PbkrOSC.instance.send("/pbkrctrl/pPause", 1.0f);
            refreshPlayStatus();
        });

        m_wgtTimeCode = root.findViewById(R.id.labelTimeCode);

        m_wgtBtnRefresh = root.findViewById((R.id.btnSync));
        m_wgtBtnRefresh.setOnClickListener(v -> {
            PbkrContext.instance.playStatus = -1;
            PbkrContext.instance.stopStatus = 1;
            PbkrOSC.instance.send("/pbkrctrl/pPause", 1.0f);
            refreshPlayStatus();
        });

        m_wgtBtnFF = root.findViewById(R.id.imageBtnFastForward);
        m_wgtBtnFF.setOnClickListener(v -> PbkrOSC.instance.send("/pbkrctrl/pFastForward", 1.0f));
        m_wgtBtnRew = root.findViewById(R.id.imageBtnRewind);
        m_wgtBtnRew.setOnClickListener(v -> PbkrOSC.instance.send("/pbkrctrl/pBackward", 1.0f));

        // Find all Track buttons
        m_trackButtons = new ButtonXYMap(root.findViewById(R.id.layoutTracks));


        PbkrOSC.instance.setMainHomePage(this);

        for (int tt = 0; tt < PbkrContext.nbTracks; tt++) {
            setTrackName(tt, PbkrContext.instance.getTrackName(tt));
        }
        
        refreshAll();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // PbkrOSC.instance.setMainHomePage(null);
        binding = null;
    }

    public void refreshAll() {
        refreshCurrentTimeCode();
        refreshCurrentProjectName();
        refreshCurrentTrackName();
        refreshPlayStatus();
    }
    public void refreshCurrentTimeCode() {
        mHandler.post(() -> m_wgtTimeCode.setText(PbkrContext.instance.currentTimeCode));
    }

    public void setTrackName(int trackId, String paramStr) {
        mHandler.post(() -> m_trackButtons.setTrackName(trackId, paramStr));
    }

    public void setCurrentTrackNum(int trackId){
        mHandler.post(() -> m_trackButtons.setActiveTrackId(trackId));
    }
    public void refreshCurrentProjectName() {
        mHandler.post(() -> m_wgtProjectName.setText(PbkrContext.instance.currentProject));
    }

    public void refreshCurrentTrackName() {
        mHandler.post(() -> m_wgtTrackName.setText("Track : " +PbkrContext.instance.currentTrack));
    }

    public void refreshPlayStatus() {
        mHandler.post(() -> {
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
        });
    }
}