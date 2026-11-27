package com.example.scoutingapp.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.scoutingapp.data.config.DeviceConfigStore;
import com.example.scoutingapp.ui.scout.Alliance;

/**
 * Match setup form. Works fully offline: there is no schedule and no online
 * "already scouted" check in BUILT, so the form is valid as soon as every field is filled.
 */
public class ManualScoutViewModel extends ViewModel {

    private final DeviceConfigStore configStore;

    private final MutableLiveData<ManualScoutState> _state = new MutableLiveData<>(new ManualScoutState());
    public final LiveData<ManualScoutState> state = _state;

    public ManualScoutViewModel(DeviceConfigStore configStore) {
        this.configStore = configStore;
        ManualScoutState s = current();
        s.scouterName = configStore.getScoutName();
        _state.setValue(s);
    }

    private ManualScoutState current() {
        ManualScoutState v = _state.getValue();
        return v != null ? v : new ManualScoutState();
    }

    public void onMatchIdChange(String value) {
        ManualScoutState s = current();
        s.matchId = value;
        publish(s);
    }

    public void onTeamNumberChange(String value) {
        ManualScoutState s = current();
        s.teamNumber = value;
        publish(s);
    }

    public void onScouterNameChange(String value) {
        ManualScoutState s = current();
        s.scouterName = value;
        publish(s);
    }

    public void onAllianceChange(Alliance alliance) {
        ManualScoutState s = current();
        s.alliance = alliance;
        publish(s);
    }

    /** Remember the scouter name on this tablet so the next match pre-fills it. */
    public void rememberScouterName() {
        configStore.updateScoutName(current().scouterName);
    }

    private void publish(ManualScoutState s) {
        s.canSubmit = parseIntOrNull(s.matchId) != null
                && parseIntOrNull(s.teamNumber) != null
                && s.scouterName != null && !s.scouterName.trim().isEmpty()
                && s.alliance != null;
        _state.setValue(s);
    }

    static Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static ViewModelProvider.Factory factory(DeviceConfigStore configStore) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ManualScoutViewModel(configStore);
            }
        };
    }
}
