package com.example.scoutingapp.ui.home;

import com.example.scoutingapp.ui.scout.Alliance;

public class ManualScoutState {
    public String matchId = "";
    public String teamNumber = "";
    public String scouterName = "";
    /** Must be picked by the scouter for every match; there is no device default in BUILT. */
    public Alliance alliance = null;
    public boolean canSubmit = false;
}
