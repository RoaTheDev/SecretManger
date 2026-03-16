package io.roa.secretmanger.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ShamirService {
    void splitAndDistribute();

    boolean isInitialized();

    int getTotalShares();

    String reconstructMasterKey(Set<UUID> adminIds );
}
