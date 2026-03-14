package io.roa.secretmanger.Service;

import java.util.Map;
import java.util.UUID;

public interface ShamirService {
    void splitAndDistribute();

    String reconstructMasterKey(Map<UUID, String> adminVerifiedIds);
}
