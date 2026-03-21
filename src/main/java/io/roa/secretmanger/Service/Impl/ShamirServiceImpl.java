package io.roa.secretmanger.Service.Impl;

import com.codahale.shamir.Scheme;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Exception.ShamirReconstructionException;
import io.roa.secretmanger.Model.Entity.ShamirShare;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Repo.ShamirShareRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.CryptoService;
import io.roa.secretmanger.Service.ShamirService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShamirServiceImpl implements ShamirService {

    private final ShamirShareRepo shamirShareRepo;
    private final UserRepo userRepo;
    private final CryptoService cryptoService;
    @Value("${app.master-key}")
    private String masterKeyBase64;

    @Transactional
    public void splitAndDistribute() {
        List<User> admins = userRepo.findAllByRoleAndActiveTrue(UserRole.ADMIN);
        if (admins.isEmpty()) {
            throw new ResourceNotFoundException("No active admin users found");
        }

        shamirShareRepo.deleteAllInBatch();

        int n = admins.size();
        int k = (n / 2) + 1;

        Scheme scheme = new Scheme(new SecureRandom(), n, k);
        Map<Integer, byte[]> shares = scheme.split(
                Base64.getDecoder().decode(masterKeyBase64));

        List<ShamirShare> newShares = new ArrayList<>();
        int index = 1;
        for (User admin : admins) {
            String shareBase64 = Base64.getEncoder().encodeToString(shares.get(index));
            String encryptedShare = cryptoService.encrypt(shareBase64);

            ShamirShare row = new ShamirShare();
            row.setAdmin(admin);
            row.setShareIndex(index);
            row.setEncryptedShare(encryptedShare);
            newShares.add(row);
            index++;
        }

        shamirShareRepo.saveAll(newShares);
    }
    @Transactional(readOnly = true)
    public String reconstructMasterKey(Set<UUID> adminIds) {
        int n = (int) userRepo.countByRoleAndActiveTrue(UserRole.ADMIN);
        int k = (n / 2) + 1;

        if (adminIds.size() < k) {
            throw new ShamirReconstructionException(
                    "Not enough shares provided. Need at least " + k + " admins.");
        }

        try {
            Map<Integer, byte[]> shares = new HashMap<>();
            for (UUID adminId : adminIds) {
                ShamirShare row = shamirShareRepo.findByAdminId(adminId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No share found for admin: " + adminId));
                String decryptedShare = cryptoService.decrypt(row.getEncryptedShare());
                shares.put(row.getShareIndex(),
                        Base64.getDecoder().decode(decryptedShare));
            }

            Scheme scheme = new Scheme(new SecureRandom(), n, k);
            return Base64.getEncoder().encodeToString(scheme.join(shares));
        } catch (ShamirReconstructionException e) {
            throw e;
        } catch (Exception e) {
            throw new ShamirReconstructionException("Master key reconstruction failed", e);
        }
    }
    @Transactional(readOnly = true)
    public boolean isInitialized() {
        return shamirShareRepo.count() > 0;
    }

    @Transactional(readOnly = true)
    public int getTotalShares() {
        return (int) shamirShareRepo.count();
    }
}