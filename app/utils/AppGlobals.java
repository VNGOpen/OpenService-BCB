package utils;

import modules.registry.IRegistry;
import play.Configuration;

/**
 * Global instances for cases where DI is not visible.
 * 
 * @author ThanhNB
 * @since 0.1.0
 */
public class AppGlobals {
    public static IRegistry registry;

    public static Configuration appConfig;
}
