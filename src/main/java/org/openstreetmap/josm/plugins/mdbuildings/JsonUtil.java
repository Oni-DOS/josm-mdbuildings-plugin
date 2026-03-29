package org.openstreetmap.josm.plugins.mdbuildings;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.spi.JsonProvider;

public final class JsonUtil {

    private JsonUtil() {
    }
    /**
     * Check: https://github.com/jakartaee/jsonp-api/issues/154 – Jakarta is slow
     */
    public static final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(null);
    public static final JsonProvider provider = JsonProvider.provider();
}
