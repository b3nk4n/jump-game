package de.bsautermeister.jump.services;

public class NoopRateService implements RateService {
    @Override
    public void rateGame() {

    }

    @Override
    public boolean canRate() {
        return false;
    }
}
