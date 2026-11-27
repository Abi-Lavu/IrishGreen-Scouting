package com.example.scoutingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.scoutingapp.R;
import com.example.scoutingapp.data.scout.MatchStateStore;
import com.example.scoutingapp.data.scout.SavedMatchState;
import com.example.scoutingapp.sync.SyncManager;
import com.example.scoutingapp.ui.scout.ScoutActivity;

import java.util.Locale;

/**
 * Home screen: start scouting a match, resume an interrupted match, and sync queued uploads.
 * (The REBUILT schedule, station/PIN and competition pickers were removed for BUILT.)
 */
public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private MatchStateStore matchStateStore;

    private DrawerLayout drawerLayout;

    private AlertDialog resumeDialog;

    private boolean resumeDialogHandled = false;
    private TextView drawerItemSyncNow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        matchStateStore = new MatchStateStore(this);
        viewModel = new ViewModelProvider(this, HomeViewModel.factory(matchStateStore))
                .get(HomeViewModel.class);

        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        addToolbarTitle(toolbar);

        findViewById(R.id.drawer_item_scout).setOnClickListener(v -> drawerLayout.closeDrawers());

        drawerItemSyncNow = findViewById(R.id.drawer_item_sync_now);
        drawerItemSyncNow.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            syncNow();
        });

        findViewById(R.id.btn_scout_manually).setOnClickListener(v -> {
            viewModel.discardResume();
            startActivity(new Intent(this, ManualScoutActivity.class));
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.pendingResume.observe(this, this::renderResumeDialog);

        viewModel.pendingUploadCount.observe(this, count -> {
            int n = count != null ? count : 0;
            drawerItemSyncNow.setText(n > 0 ? "Sync Now (" + n + " pending)" : "Sync Now");
        });
    }

    private void syncNow() {
        if (!SyncManager.hasAnyConnection(this)) {
            android.widget.Toast.makeText(this, "No internet connection right now.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        android.widget.Toast.makeText(this, "Syncing...", android.widget.Toast.LENGTH_SHORT).show();
        viewModel.syncNow(new com.example.scoutingapp.util.Callback<SyncManager.SyncResult>() {
            @Override
            public void onSuccess(SyncManager.SyncResult result) {
                String message;
                if (result.uploaded > 0 && result.remaining == 0) {
                    message = "Uploaded " + result.uploaded + " match" + (result.uploaded == 1 ? "" : "es") + ".";
                } else if (result.uploaded > 0) {
                    message = "Uploaded " + result.uploaded + ", " + result.remaining + " still pending.";
                } else if (result.remaining == 0) {
                    message = "Nothing to sync.";
                } else {
                    message = "Couldn't sync right now - " + result.remaining + " match(es) still pending.";
                }
                android.widget.Toast.makeText(HomeActivity.this, message, android.widget.Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                android.widget.Toast.makeText(HomeActivity.this, "Sync failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────

    private void addToolbarTitle(Toolbar toolbar) {
        TextView title = new TextView(this);
        title.setText("Scout");
        title.setTextColor(getColor(R.color.accent));
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        title.setTextSize(13);
        title.setPadding(dp(14), dp(6), dp(14), dp(6));
        toolbar.addView(title);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // ── Resume dialog ─────────────────────────────────────────────────────────

    private void renderResumeDialog(@Nullable SavedMatchState saved) {
        if (saved == null) {
            if (resumeDialog != null) resumeDialog.dismiss();
            return;
        }
        if (resumeDialog != null && resumeDialog.isShowing()) return;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_resume_match, null);
        bindInfoRow(view, R.id.row_match, "Match", "Qual " + saved.matchId);
        bindInfoRow(view, R.id.row_team, "Team", String.valueOf(saved.teamNumber));
        bindInfoRow(view, R.id.row_position, "Alliance", saved.positionLabel);

        String displayName = titleCase(saved.scouterName != null ? saved.scouterName.replace('_', ' ') : "");
        bindInfoRow(view, R.id.row_scouter, "Scouter", displayName.isEmpty() ? "\u2014" : displayName);

        View timeRow = view.findViewById(R.id.row_time_left);
        if (saved.clockWasRunning) {
            int remaining = Math.max(0, 160 - saved.elapsedSec);
            bindInfoRow(view, R.id.row_time_left, "Time left (approx)",
                    String.format(Locale.US, "%d:%02d", remaining / 60, remaining % 60));
            timeRow.setVisibility(View.VISIBLE);
        } else {
            timeRow.setVisibility(View.GONE);
        }

        resumeDialog = new AlertDialog.Builder(this)
                .setTitle("Resume Match?")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Resume", (d, w) -> navigateToResume(saved))
                .setNegativeButton("Discard", (d, w) -> viewModel.discardResume())
                .create();
        resumeDialog.show();
    }

    private void bindInfoRow(View parent, int rowId, String label, String value) {
        View row = parent.findViewById(rowId);
        ((TextView) row.findViewById(R.id.row_label)).setText(label);
        ((TextView) row.findViewById(R.id.row_value)).setText(value);
    }

    private static String titleCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (String word : s.split(" ")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    private void navigateToResume(SavedMatchState saved) {
        Intent intent = new Intent(this, ScoutActivity.class);
        intent.putExtra(ScoutActivity.EXTRA_MATCH_ID, saved.matchId);
        intent.putExtra(ScoutActivity.EXTRA_TEAM_NUMBER, saved.teamNumber);
        intent.putExtra(ScoutActivity.EXTRA_SCOUTER_NAME, saved.scouterName);
        intent.putExtra(ScoutActivity.EXTRA_POSITION, saved.positionLabel);
        intent.putExtra(ScoutActivity.EXTRA_COMPETITION, saved.competition);
        intent.putExtra(ScoutActivity.EXTRA_RESUME, true);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}
