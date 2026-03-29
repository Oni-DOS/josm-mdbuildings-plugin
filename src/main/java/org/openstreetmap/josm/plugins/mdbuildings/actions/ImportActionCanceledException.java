package org.openstreetmap.josm.plugins.mdbuildings.actions;

import org.openstreetmap.josm.plugins.mdbuildings.ImportStatus;

public class ImportActionCanceledException  extends Exception {
    private final ImportStatus status;

    public ImportActionCanceledException(String message, ImportStatus status) {
        super(message);
        this.status = status;
    }

    public ImportStatus getStatus() {
        return status;
    }
}
