package com.ymylb.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Integrates with the mod with the ModMenu mod, providing an in-game configuration screen.
 */
public class ModMenuIntegration implements ModMenuApi {

    /**
     * Returns the configuration screen for ModMenu screen.
     *
     * @return A factory that creates the mod's configuration screen.
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YouMinedYourLastBlockConfigScreen::new;
    }
}
