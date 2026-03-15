package io.roa.secretmanger.Service;

import java.util.Map;
import java.util.UUID;

public interface ShamirService {
    void splitAndDistribute();

    boolean isInitialized();

    int getTotalShares();

    String reconstructMasterKey(Map<UUID, String> adminVerifiedIds);
}
