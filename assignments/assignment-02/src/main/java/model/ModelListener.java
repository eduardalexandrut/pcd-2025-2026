package model;

import lib.FSReport;

public interface ModelListener {
    void onUpdate(FSReport report);
    void onComplete(FSReport report);
}
