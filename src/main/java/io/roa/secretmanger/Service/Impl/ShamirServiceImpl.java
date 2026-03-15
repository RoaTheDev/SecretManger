package io.roa.secretmanger.Service.Impl;

import com.codahale.shamir.Scheme;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Exception.ShamirAlreadyInitializedException;
import io.roa.secretmanger.Exception.ShamirReconstructionException;
import io.roa.secretmanger.Model.Entity.ShamirShare;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Repo.ShamirShareRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.CryptoService;
import io.roa.secretmanger.Service.ShamirService;
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
        if (shamirShareRepo.count() > 0) {
            throw new ShamirAlreadyInitializedException(
                    "Shamir shares have already been distributed");
        }

        List<User> admins = userRepo.findAllByRole((UserRole.ADMIN));
        if (admins.isEmpty()) {
            throw new ResourceNotFoundException("No admin users found to distribute shares to");
        }
        int n = admins.size();

        int k = admins.size() - (admins.size() / 2);
        Scheme scheme = new Scheme(new SecureRandom(), n, k);
        Map<Integer, byte[]> shares = scheme.split(
                Base64.getDecoder().decode(masterKeyBase64));

        int index = 1;
        for (User admin : admins) {
            String shareBase64 = Base64.getEncoder().encodeToString(shares.get(index));
            String encryptedShare = cryptoService.encrypt(shareBase64);

            ShamirShare row = new ShamirShare();
            row.setAdmin(admin);
            row.setShareIndex(index);
            row.setEncryptedShare(encryptedShare);

            shamirShareRepo.save(row);
            index++;
        }
    }


    @Transactional(readOnly = true)
    public String reconstructMasterKey(Map<UUID, String> adminVerifiedIds) {
        try {
            Map<Integer, byte[]> shares = new HashMap<>();

            for (UUID adminId : adminVerifiedIds.keySet()) {
                ShamirShare row = shamirShareRepo.findByAdminId(adminId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No share found for admin: " + adminId));

                String decryptedShare = cryptoService.decrypt(row.getEncryptedShare());
                shares.put(row.getShareIndex(),
                        Base64.getDecoder().decode(decryptedShare));
            }

            int totalShares = (int) shamirShareRepo.count();
            Scheme scheme = new Scheme(new SecureRandom(), totalShares, shares.size());

            return Base64.getEncoder().encodeToString(scheme.join(shares));
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