package com.cantomiletea.chavez.pending;

import com.cantomiletea.chavez.pending.dto.PendingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pending")
@RequiredArgsConstructor
@Slf4j
public class PendingController {

    private final PendingService pendingService;

    @PostMapping("/{slug}")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> addPending(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                        @PathVariable String slug) {


        pendingService.addPending(authorizationHeader, slug);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<?> removePending(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                           @PathVariable String slug) {


        pendingService.removePending(authorizationHeader, slug);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ResponseEntity<List<PendingDto>> getUserPendings(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {


        return ResponseEntity.ok(pendingService.getUserPendings(authorizationHeader));
    }

}
