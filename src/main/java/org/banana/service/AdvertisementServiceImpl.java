package org.banana.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.banana.repository.AdvertisementRepository;
import org.springframework.stereotype.Service;

/**
 * Created by Banana on 25.04.2025
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
}
