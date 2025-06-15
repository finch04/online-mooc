package com.tianji.learning.service;

import com.tianji.learning.domain.dto.ShareDetailDTO;
import com.tianji.learning.domain.dto.ShareLinkDTO;

/**
 * @author fsq
 * @date 2025/6/15 14:39
 */
public interface IShareService {
    ShareLinkDTO generateShareLink(Long userId, Long courseId);

    ShareDetailDTO parseShareLink(String shortCode);
}
