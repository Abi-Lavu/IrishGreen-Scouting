package com.example.scoutingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.scoutingapp.R;
import com.example.scoutingapp.data.config.DeviceConfigStore;
import com.example.scoutingapp.ui.scout.Alliance;
import com.example.scoutingapp.ui.scout.ScoutActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ManualScoutActivity extends AppCompatActivity {

    private ManualScoutViewModel viewModel;
    /** Fixed event key stored with each match until step 9 replaces the upload format. */
    private static final String EVENT_KEY = "built";

    private LinearLayout allianceRow;
    private MaterialButton btnStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_scout);

        DeviceConfigStore configStore = new DeviceConfigStore(this);
        viewModel = new ViewModelProvider(this, ManualScoutViewModel.factory(configStore))
                .get(ManualScoutViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        allianceRow = findViewById(R.id.row_alliance);
        btnStart = findViewById(R.id.btn_start_scouting);

        TextInputEditText etMatch = findViewById(R.id.et_match_number);
        TextInputEditText etTeam = findViewById(R.id.et_team_number);
        TextInputEditText etScouter = findViewById(R.id.et_scouter_name);

        ManualScoutState initial = viewModel.state.getValue();
        if (initial != null && initial.scouterName != null) etScouter.setText(initial.scouterName);

        etMatch.addTextChangedListener(watcherFor(viewModel::onMatchIdChange));
        etTeam.addTextChangedListener(watcherFor(viewModel::onTeamNumberChange));
        etScouter.addTextChangedListener(watcherFor(viewModel::onScouterNameChange));

        btnStart.setOnClickListener(v -> {
            ManualScoutState s = viewModel.state.getValue();
            if (s == null || !s.canSubmit) return;
            Integer matchId = parseIntOrNull(s.matchId);
            Integer teamNumber = parseIntOrNull(s.teamNumber);
            if (matchId == null || teamNumber == null) return;
            viewModel.rememberScouterName();

            Intent intent = new Intent(this, ScoutActivity.class);
            intent.putExtra(ScoutActivity.EXTRA_MATCH_ID, matchId);
            intent.putExtra(ScoutActivity.EXTRA_TEAM_NUMBER, teamNumber);
            intent.putExtra(ScoutActivity.EXTRA_SCOUTER_NAME, s.scouterName.trim());
            intent.putExtra(ScoutActivity.EXTRA_POSITION, allianceLabel(s.alliance));
            intent.putExtra(ScoutActivity.EXTRA_COMPETITION, EVENT_KEY);
            intent.putExtra(ScoutActivity.EXTRA_RESUME, false);
            startActivity(intent);
        });

        viewModel.state.observe(this, this::render);
    }

    private void render(ManualScoutState s) {
        if (s == null) return;

        populateAlliances(s.alliance);
        btnStart.setEnabled(s.canSubmit);
    }

    private static String allianceLabel(Alliance alliance) {
        return alliance == Alliance.RED ? "Red" : "Blue";
    }

    private void populateAlliances(Alliance selected) {
        allianceRow.removeAllViews();
        for (Alliance alliance : Alliance.values()) {
            int accentColor = getColor(alliance == Alliance.RED ? R.color.alliance_red : R.color.alliance_blue);
            TextView chip = new TextView(this);
            chip.setText(allianceLabel(alliance));
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setTextSize(16);
            boolean isSelected = alliance == selected;
            chip.setTextColor(isSelected ? accentColor : getColor(R.color.text_dim));
            chip.setTypeface(null, isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(dp(8));
            bg.setColor(isSelected ? withAlpha(accentColor, 46) : getColor(R.color.surface));
            bg.setStroke(dp(isSelected ? 2 : 1), isSelected ? accentColor : 0xFF555555);
            chip.setBackground(bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            params.setMarginEnd(dp(10));
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> viewModel.onAllianceChange(alliance));
            allianceRow.addView(chip);
        }
    }

    private int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static Integer parseIntOrNull(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private interface StringConsumer { void accept(String value); }

    private static TextWatcher watcherFor(StringConsumer consumer) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { consumer.accept(s.toString()); }
        };
    }
}
