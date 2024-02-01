package de.bsautermeister.jump.services;

public interface AdService {

    boolean isSupported();
    boolean isReady();
    void initialize();
    void load();
    boolean show();

    boolean isPrivacyOptionsRequired();

    void showPrivacyConsentForm();

    void resetConsentForTesting();
}
