package com.ymylb;

public class PlayerData {
    private boolean isHardcoreDeath = false;
    private boolean tooManyBlocks = false;

    public boolean getIsHardcoreDeath() {
        return this.isHardcoreDeath;
    }

    public void setHardcoreDeath(boolean isHardcoreDeath) {
        this.isHardcoreDeath = isHardcoreDeath;
    }

    public boolean getTooManyBlocks() {
        return this.tooManyBlocks;
    }

    public void setTooManyBlocks(boolean tooManyBlocks) {
        this.tooManyBlocks = tooManyBlocks;
    }
}
