package com.example.scoutingapp.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.scoutingapp.data.scout.MatchStateStore;
import com.example.scoutingapp.data.scout.PendingUploadStore;
import com.example.scoutingapp.data.scout.SavedMatchState;
import com.example.scoutingapp.sync.SyncManager;
import com.example.scoutingapp.util.Callback;

public class HomeViewModel extends ViewModel {

    private final MatchStateStore matchStateStore;

    /** Count of scouted matches saved locally but not yet confirmed uploaded to Supabase. */
    public final LiveData<Integer> pendingUploadCount = PendingUploadStore.getCountLiveData();

    // ── Resume ────────────────────────────────────────────────────────────────

    /** A match that was in progress when the app last closed, or null. */
    public final LiveData<SavedMatchState> pendingResume;

    public HomeViewModel(MatchStateStore matchStateStore) {
        this.matchStateStore = matchStateStore;
        this.pendingResume = matchStateStore.getSavedStateLiveData();
    }

    public void discardResume() {
        matchStateStore.clear();
    }

    /** Manual "Sync Now" action - uploads any locally-queued matches, over any connection type. */
    public void syncNow(Callback<SyncManager.SyncResult> callback) {
        SyncManager.flushNow(callback);
    }

    public static ViewModelProvider.Factory factory(MatchStateStore matchStateStore) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new HomeViewModel(matchStateStore);
            }
        };
    }
}
