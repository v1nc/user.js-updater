package de.reckendrees.systems.userjsupdater;

public interface DownloadInterface<T> {
    public void onDownloadSuccess(T object);
    public void onDownloadFailure(Exception e);
}