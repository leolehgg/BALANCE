package com.wellbeing.deviceusage.service.sync;

import com.wellbeing.deviceusage.dto.sync.SyncRequest;
import com.wellbeing.deviceusage.dto.sync.SyncResponse;

public interface SyncService {
    SyncResponse synchronizeData(SyncRequest request);
}