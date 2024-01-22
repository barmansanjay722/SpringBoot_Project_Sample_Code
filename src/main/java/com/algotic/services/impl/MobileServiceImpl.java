package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.response.connectsms.MobileSmsResponse;
import com.algotic.services.MobileService;
import com.algotic.utils.AlgoticUtils;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MobileServiceImpl implements MobileService {

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Value("${baseUrlCommonNestUrl}")
    private String baseUrlCommonNestUrl;

    @Value("${commonNestMessageUrl}")
    private String commonNestMessageUrl;

    @Value("${commonNestEndUrl}")
    private String commonNestEndUrl;

    @Override
    public Object sendOTPMobile(int otp, String phone) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(" Send Otp to Phone number", "Send Otp to customer Mobile", null, null));
            String sentOtp = Integer.toString(otp);
            String url = baseUrlCommonNestUrl + phone + commonNestMessageUrl + sentOtp + commonNestEndUrl;
            log.info(logConfig.getLogHandler().getInfoLog(" Url", AlgoticUtils.objectToJsonString(url), null, null));
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<MobileSmsResponse> responseEntity =
                    restTemplate.getForEntity(new URI(url), MobileSmsResponse.class);
            MobileSmsResponse result = responseEntity.getBody();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(" Result", AlgoticUtils.objectToJsonString(result), null, null));
            if (result != null && result.getStatus().equalsIgnoreCase("success")) {
                return result;
            }
            throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);

        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }
}
