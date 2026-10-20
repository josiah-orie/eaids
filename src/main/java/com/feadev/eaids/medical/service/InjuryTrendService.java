package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.request.InjuryTrendQueryParams;
import com.feadev.eaids.medical.dto.response.InjuryTrendResponse;

public interface InjuryTrendService {

    /**
     * Returns aggregated injury trend data for the given query window and optional filters.
     * Defaults: startDate = 1 year ago, endDate = today.
     */
    InjuryTrendResponse getTrends(InjuryTrendQueryParams params);
}
