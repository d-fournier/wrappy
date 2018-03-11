package me.dfournier.versionupgrader;

import org.jetbrains.annotations.Nullable;

import me.dfournier.versionupgrader.annotations.UpdateSuite;

@UpdateSuite
public class A implements Updater {
    @Override
    public int getCurrentVersion() {
        return 0;
    }

    @Nullable
    @Override
    public Integer getVersionUpgraderData() {
        return null;
    }

    @Override
    public void setVersionUpgraderData(int data) {
    }
}
