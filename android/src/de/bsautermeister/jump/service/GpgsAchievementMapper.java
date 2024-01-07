package de.bsautermeister.jump.service;

import de.bsautermeister.jump.services.AchievementKeys;
import de.golfgl.gdxgamesvcs.IGameServiceIdMapper;

public class GpgsAchievementMapper implements IGameServiceIdMapper<String> {
    @Override
    public String mapToGsId(String key) {
        if (AchievementKeys.BEERS_25.equals(key)) {
            return "CgkIldPb1IEUEAIQAQ";
        }
        if (AchievementKeys.BEERS_50.equals(key)) {
            return "CgkIldPb1IEUEAIQAg";
        }
        if (AchievementKeys.BEERS_100.equals(key)) {
            return "CgkIldPb1IEUEAIQAw";
        }
        if (AchievementKeys.KILL_3.equals(key)) {
            return "CgkIldPb1IEUEAIQBA";
        }
        if (AchievementKeys.KILL_5.equals(key)) {
            return "CgkIldPb1IEUEAIQBQ";
        }

        return null;
    }
}
