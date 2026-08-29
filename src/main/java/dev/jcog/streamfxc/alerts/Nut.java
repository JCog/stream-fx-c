package dev.jcog.streamfxc.alerts;

import dev.jcog.streamfxc.interfaces.obs.Filter;
import dev.jcog.streamfxc.interfaces.obs.Source;

public class Nut extends Alert {
    private static final String ID = "Nut";

    private final Source sourceNut = new Source("Alerts - Nut", "Nut");
    private final Filter filterDamaged = new Filter("DSLR", "Damaged");

    public Nut() {
        super(ID);
    }

    @Override
    protected void onTrigger() {
        sourceNut.enable();

        waitFromNow(896);
        filterDamaged.enable();
        waitFromNow(100);
        filterDamaged.disable();

        waitUntil(1500);
        sourceNut.disable();
        waitFromNow(500);
    }
}
