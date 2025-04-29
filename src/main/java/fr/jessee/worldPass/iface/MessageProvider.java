package fr.jessee.worldPass.iface;

import java.util.Map;

public interface MessageProvider {
    String get(String key);
    String get(String key, Map<String, String> placeholders);
}


