package dev.gether.getcustomitem.file.config;

import dev.gether.getutils.GetConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class LangConfig extends GetConfig {
    private String hasCooldown = "#ff2137 × Please wait {time} before you can do this again.";
    private String noLimit = "#4aff36No limits";
    private String cannotUseElytraWhileHooked = "&cCannot use elytra!";
    private String canFlyAgain = "&aYou can fly again!";
    private String cannotUseItemWhileHooked = "&cCannot use this item!";
    private String cannotUseSpecialItem = "&cCannot use this item! {time}";

    private String cannotUseElytraWhileTentacleEffect = "&cCannot use elytra!";

}
