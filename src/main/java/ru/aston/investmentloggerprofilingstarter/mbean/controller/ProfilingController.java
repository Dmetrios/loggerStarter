package ru.aston.investmentloggerprofilingstarter.mbean.controller;

import ru.aston.investmentloggerprofilingstarter.mbean.ProfilingControllerMBean;

public class ProfilingController implements ProfilingControllerMBean {
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
