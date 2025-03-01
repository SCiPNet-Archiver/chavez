package com.cantomiletea.chavez.pending;

import com.cantomiletea.chavez.pending.dto.PendingDto;
import com.cantomiletea.chavez.user.UserInfoEntity;
import com.cantomiletea.chavez.user.UserInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PendingService {

    private final PendingRepo pendingRepo;
    private final UserInfoService userInfoService;

    public PendingService(PendingRepo pendingRepo, UserInfoService userInfoService) {
        this.pendingRepo = pendingRepo;
        this.userInfoService = userInfoService;
    }

    public void addPending(String authHeader, String slug) {
        UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);

        // Check if user already has this slug in pending
        pendingRepo.findByUserAndSlug(user, slug)
                .ifPresent(p -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "User already has article with slug '" + slug + "' in pending");
                });

        PendingEntity pending = new PendingEntity();
        pending.setSlug(slug);
        pending.setUser(user);
        pendingRepo.save(pending);
    }

    public void removePending(String authHeader, String slug) {
        UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);

        PendingEntity pending = pendingRepo.findByUserAndSlug(user, slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User does not have article with slug '" + slug + "' in pending"));

        pendingRepo.delete(pending);
    }

    public List<PendingDto> getUserPendings(String authHeader) {
        UserInfoEntity user = userInfoService.getUserInfoByJwt(authHeader);

        return pendingRepo.findAllByUser(user)
                .stream()
                .map(entity -> new PendingDto(entity.getSlug()))
                .toList();
    }
}
