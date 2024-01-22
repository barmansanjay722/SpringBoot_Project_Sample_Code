package com.algotic.base;

import com.algotic.model.logs.ErrorLogData;
import com.algotic.model.logs.InfoLogData;
import com.algotic.utils.AlgoticUtils;

public class LogHandler {
    private final InfoLogData infoLogData = new InfoLogData();
    private final ErrorLogData errorLogData = new ErrorLogData();

    public String getInfoLog(String infoMessage, String infoContext, String userId, String httpCode) {
        infoLogData.setInfoMessage(infoMessage);
        infoLogData.setInfoContext(infoContext);
        infoLogData.setUserId(userId);
        infoLogData.setHttpCode(httpCode);
        return AlgoticUtils.objectToJsonString(infoLogData);
    }

    public String getErrorLog(String errorMessage, String errorContext, String userId, String httpCode) {
        errorLogData.setErrorMessage(errorMessage);
        errorLogData.setErrorContext(errorContext);
        errorLogData.setUserId(userId);
        errorLogData.setHttpCode(httpCode);
        return AlgoticUtils.objectToJsonString(errorLogData);
    }
}
