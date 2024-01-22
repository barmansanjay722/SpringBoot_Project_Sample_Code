package com.algotic.services.impl;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticStatus;
import com.algotic.constants.BrokerEnum;
import com.algotic.constants.CompanyType;
import com.algotic.constants.ResourceType;
import com.algotic.constants.StageType;
import com.algotic.data.entities.BrokerCustomerDetails;
import com.algotic.data.entities.BrokerSessionDetails;
import com.algotic.data.entities.Brokers;
import com.algotic.data.entities.CustomerManagements;
import com.algotic.data.entities.GSTDetails;
import com.algotic.data.entities.InActiveUsers;
import com.algotic.data.entities.SubscriptionTransactions;
import com.algotic.data.entities.UserActivities;
import com.algotic.data.entities.Users;
import com.algotic.data.entities.VerificationDetails;
import com.algotic.data.repositories.AccessTokensRepo;
import com.algotic.data.repositories.ActiveInactiveCustomerRepo;
import com.algotic.data.repositories.BrokerCustomerDetailsRepo;
import com.algotic.data.repositories.BrokerSessionDetailsRepo;
import com.algotic.data.repositories.BrokersRepo;
import com.algotic.data.repositories.CustomerRepo;
import com.algotic.data.repositories.GstDetailsRepo;
import com.algotic.data.repositories.SubscriptionTransactionsRepo;
import com.algotic.data.repositories.UserActivityRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.data.repositories.VerificationDetailsRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.ActiveUsersHistoryRequest;
import com.algotic.model.request.BrokerCustomerRequest;
import com.algotic.model.request.CreateCustomerRequest;
import com.algotic.model.request.EmailRequest;
import com.algotic.model.request.GstDetailsRequest;
import com.algotic.model.request.UpdateCustomerRequest;
import com.algotic.model.response.CreateCustomerResponse;
import com.algotic.model.response.CustomerManagementResponse;
import com.algotic.model.response.CustomerResponse;
import com.algotic.model.response.EmailVerificationResponse;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.GstResponse;
import com.algotic.model.response.InActiveUsersResponse;
import com.algotic.model.response.OtpResponse;
import com.algotic.model.response.ProfileDetailsResponse;
import com.algotic.model.response.RegistrationDataResponse;
import com.algotic.model.response.RegistrationResponse;
import com.algotic.model.response.RenewalHistoryResponse;
import com.algotic.model.response.RenewalResponse;
import com.algotic.services.EmailService;
import com.algotic.services.OtpService;
import com.algotic.services.UserService;
import com.algotic.utils.AlgoticUtils;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private ActiveInactiveCustomerRepo activeInactiveCustomerRepo;

    @Autowired
    OtpService otpService;

    @Value("${emailUrl}")
    private String emailUrl;

    @Autowired
    private BrokerCustomerDetailsRepo brokerCustomerDetailsRepo;

    @Autowired
    private BrokerSessionDetailsRepo brokerSessionDetailsRepo;

    @Autowired
    private BrokersRepo brokersRepo;

    @Autowired
    private GstDetailsRepo gstDetailsRepo;

    @Autowired
    private SubscriptionTransactionsRepo subscriptionTransactionsRepo;

    @Autowired
    private VerificationDetailsRepo verificationDetailsRepo;

    @Autowired
    private OtpServiceImpl otpServiceimpl;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private AccessTokensRepo accessTokensRepo;

    @Autowired
    private UserActivityRepo userActivityRepo;

    @Override
    public ResponseEntity<CreateCustomerResponse> createCustomer(CreateCustomerRequest createCustomerRequest) {
        CreateCustomerResponse createCustomerResponse = new CreateCustomerResponse();
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "new customer details",
                            AlgoticUtils.objectToJsonString(createCustomerRequest),
                            null,
                            null));
            validateCustomerRequest(createCustomerRequest);
            Users user = saveCustomer(createCustomerRequest);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("new customer created", AlgoticUtils.objectToJsonString(user), null, null));
            String refCode = otpService.randomRefCode();
            String generateOtp = otpService.generateAndSendOtp(
                    createCustomerRequest.getEmail(), createCustomerRequest.getPhoneNumber());
            otpService.saveOtp(generateOtp, refCode, 300, user.getId(), false, "Registers", null);

            createCustomerResponse.setUserId(user.getId());
            createCustomerResponse.setFirstName(user.getFirstName());
            createCustomerResponse.setLastName(user.getLastName());
            createCustomerResponse.setEmail(user.getEmail());
            createCustomerResponse.setPhoneNumber(user.getPhoneNumber());
            createCustomerResponse.setOtp(new OtpResponse(refCode));
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "create customer details",
                            AlgoticUtils.objectToJsonString(createCustomerResponse),
                            AlgoticUtils.objectToJsonString(createCustomerResponse.getUserId()),
                            String.valueOf(HttpStatus.CREATED.value())));

            return new ResponseEntity<>(createCustomerResponse, HttpStatus.CREATED);

        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    private Users saveCustomer(CreateCustomerRequest createCustomerRequest) {
        log.info(logConfig.getLogHandler().getInfoLog("Save Customer", "save the data of a user", null, null));
        Users user = new Users();
        user.setId(AlgoticUtils.generateUUID());
        user.setFirstName(createCustomerRequest.getFirstName());
        user.setLastName(createCustomerRequest.getLastName());
        user.setPhoneNumber(createCustomerRequest.getPhoneNumber());
        user.setTermsAccepted(false);
        user.setEmail(createCustomerRequest.getEmail());
        user.setStage(StageType.CREATED.name());
        user.setRole("customer");
        user.setStatus(AlgoticStatus.CREATED.name());
        user.setIsDeleted(false);
        user.setCreatedAt(new Date());
        user.setModifiedAt(new Date());
        user = usersRepo.save(user);
        log.info(logConfig.getLogHandler().getInfoLog("User data", AlgoticUtils.objectToJsonString(user), null, null));
        return user;
    }

    private void validateCustomerRequest(CreateCustomerRequest createCustomerRequest) {

        Users email = usersRepo.findByEmail(createCustomerRequest.getEmail());
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("new customer created", AlgoticUtils.objectToJsonString(email), null, null));
        Users phoneNumber = usersRepo.findByPhoneNumber(createCustomerRequest.getPhoneNumber());
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("new customer created", AlgoticUtils.objectToJsonString(phoneNumber), null, null));

        if (email != null) {
            CommonErrorCode errorCode = CommonErrorCode.EMAIL_ALREADY_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        } else if (phoneNumber != null) {
            CommonErrorCode errorCode = CommonErrorCode.PHONENUMBER_ALREADY_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> updateCustomer(
            String id, UpdateCustomerRequest updateCustomerRequest) {
        GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Upadte customer", "Update the customer", null, null));
            Users updateDetails = usersRepo.findByID(id);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("update customer details", AlgoticUtils.objectToJsonString(updateDetails), id, null));
            if (updateDetails == null) {
                CommonErrorCode errorCode = CommonErrorCode.TOKEN_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            updateDetails.setFirstName(updateCustomerRequest.getFirstName());
            updateDetails.setLastName(updateCustomerRequest.getLastName());
            Users user = usersRepo.save(updateDetails);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("User upadte and save", AlgoticUtils.objectToJsonString(user), id, null));
            globalMessageResponse.setMessage("Data Update Successfully");
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Updated Details of Customer",
                            AlgoticUtils.objectToJsonString(globalMessageResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public CustomerResponse getCustomerById(String userId) {
        try {

            CustomerResponse customerResponse = new CustomerResponse();
            Users customer = usersRepo.findByID(userId);
            if (customer != null) {
                customerResponse.setPhoneNumber(customer.getPhoneNumber());
                customerResponse.setId(customer.getId());
                customerResponse.setEmail(customer.getEmail());
                customerResponse.setLastName(customer.getLastName());
                customerResponse.setFirstName(customer.getFirstName());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Get Customer Details",
                                AlgoticUtils.objectToJsonString(customerResponse),
                                userId,
                                "200"));
                return customerResponse;
            }
            CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public String getAliceBlueToken(String userId) {
        log.info(logConfig.getLogHandler().getInfoLog("Alice blue Token", "Get Alice blue token ", userId, null));
        BrokerCustomerDetails brokerCustomerDetails = brokerCustomerDetailsRepo.findBrokerUserById(userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "brokerCustomer details",
                        AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                        userId,
                        null));
        BrokerSessionDetails brokerSessionDetails = brokerSessionDetailsRepo.getSessionId(userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker Session details", AlgoticUtils.objectToJsonString(brokerSessionDetails), userId, null));
        if (brokerCustomerDetails == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        String userID = brokerCustomerDetails.getReferenceID();
        if (brokerSessionDetails == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        String sessionId = brokerSessionDetails.getSessionId();

        if (sessionId == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "User Id + Session Id",
                        AlgoticUtils.objectToJsonString(userID + " " + sessionId),
                        userId,
                        null));
        return userID + " " + sessionId;
    }

    @Override
    public String getBrokerSessionId(String userId) {

        BrokerSessionDetails brokerSessionDetailsOptional = brokerSessionDetailsRepo.getSessionId(userId);

        if (brokerSessionDetailsOptional == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.SESSION_ID_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        String sessionId = brokerSessionDetailsOptional.getSessionId();

        if (sessionId == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.SESSION_ID_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        return sessionId;
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> saveGstDetails(GstDetailsRequest gstDetailsRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "save customer gst details",
                            AlgoticUtils.objectToJsonString(gstDetailsRequest),
                            null,
                            null));
            GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
            Users customer = usersRepo.findByID(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "customer gst details by its id",
                            AlgoticUtils.objectToJsonString(customer),
                            jwtHelper.getUserId(),
                            null));
            if (customer == null) {
                CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            GSTDetails gstDetails = gstDetailsRepo.findByUserId(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "customer gst details",
                            AlgoticUtils.objectToJsonString(gstDetails),
                            jwtHelper.getUserId(),
                            null));
            String type = gstDetailsRequest.getType().trim();
            if (gstDetails != null) {
                gstDetails.setType(type);
                if (type.equalsIgnoreCase("limited")) {
                    gstDetails.setType(CompanyType.LIMITED.name());
                } else if (type.equalsIgnoreCase("proprietorship")) {
                    gstDetails.setType(CompanyType.PROPRIETORSHIP.name());
                } else if (type.equalsIgnoreCase("private limited")) {
                    gstDetails.setType(CompanyType.PRIVATE_LIMITED.name());
                } else {
                    BusinessErrorCode errorCode = BusinessErrorCode.COMPANY_TYPE_NOT_EXIST;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }

                gstDetails.setCompanyName(gstDetailsRequest.getCompanyName());
                gstDetails.setAddress(gstDetailsRequest.getAddress());
                gstDetails.setGstIn(gstDetailsRequest.getGstIn());
                GSTDetails details = gstDetailsRepo.save(gstDetails);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Gst details", AlgoticUtils.objectToJsonString(details), jwtHelper.getUserId(), null));
                globalMessageResponse.setMessage("Gst Details Updated");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Gst Details Update",
                                AlgoticUtils.objectToJsonString(gstDetails),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            }
            GSTDetails gsTdetails = new GSTDetails();
            gsTdetails.setUserId(customer.getId());
            gsTdetails.setType(gstDetailsRequest.getType());
            if (type.equalsIgnoreCase("limited")) {
                gsTdetails.setType(CompanyType.LIMITED.name());
            } else if (type.equalsIgnoreCase("proprietorship")) {
                gsTdetails.setType(CompanyType.PROPRIETORSHIP.name());
            } else if (type.equalsIgnoreCase("private limited")) {
                gsTdetails.setType(CompanyType.PRIVATE_LIMITED.name());
            } else {
                BusinessErrorCode errorCode = BusinessErrorCode.COMPANY_TYPE_NOT_EXIST;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            gsTdetails.setGstIn(gstDetailsRequest.getGstIn());
            gsTdetails.setCompanyName(gstDetailsRequest.getCompanyName());
            gsTdetails.setAddress(gstDetailsRequest.getAddress());
            gsTdetails.setCreatedAt(new Date());
            GSTDetails details = gstDetailsRepo.save(gsTdetails);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Gst details save", AlgoticUtils.objectToJsonString(details), jwtHelper.getUserId(), null));
            globalMessageResponse.setMessage("Gst Details Saved");
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Gst Details Saved",
                            AlgoticUtils.objectToJsonString(gsTdetails),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.CREATED.value())));
            return new ResponseEntity<>(globalMessageResponse, HttpStatus.CREATED);

        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GstResponse> getGstDetails(String userId) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(" get customer gst details", "get customer gst details by its id", null, null));
            GSTDetails details = gstDetailsRepo.findByUserId(userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(" gst details of a customer", AlgoticUtils.objectToJsonString(details), userId, null));
            if (details != null) {
                GstResponse gstResponse = new GstResponse();
                gstResponse.setCompanyName(details.getCompanyName());
                gstResponse.setGstIn(details.getGstIn());
                gstResponse.setType(details.getType());
                if (CompanyType.LIMITED.name().equalsIgnoreCase(details.getType())) {
                    gstResponse.setType("Limited");
                } else if (CompanyType.PROPRIETORSHIP.name().equalsIgnoreCase(details.getType())) {
                    gstResponse.setType("Proprietorship");
                } else if (CompanyType.PRIVATE_LIMITED.name().equalsIgnoreCase(details.getType())) {
                    gstResponse.setType("Private Limited");
                }
                gstResponse.setUserId(details.getUserId());
                gstResponse.setAddress(details.getAddress());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Gst details",
                                AlgoticUtils.objectToJsonString(gstResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(gstResponse, HttpStatus.OK);
            }
            CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);

        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> getVerified(String verificationCode) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get email verified", "If verification code matches then the email update ", null, null));

            GlobalMessageResponse verificationResponse = new GlobalMessageResponse();
            VerificationDetails verificationDetails = verificationDetailsRepo.findByVerificationCode(verificationCode);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "verification details", AlgoticUtils.objectToJsonString(verificationDetails), null, null));
            if (verificationDetails == null) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                null,
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            verificationDetails.setIsVerified(true);
            verificationDetails.setVerifiedAt(new Date());
            VerificationDetails details = verificationDetailsRepo.save(verificationDetails);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("verification details save", AlgoticUtils.objectToJsonString(details), null, null));

            Users user = usersRepo.findByID(verificationDetails.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("get email verified by user id", AlgoticUtils.objectToJsonString(user), null, null));
            if (user != null) {
                user.setEmail(verificationDetails.getResource());
                usersRepo.save(user);
            }

            EmailVerificationResponse emailResponse = new EmailVerificationResponse();
            emailResponse.setId(verificationDetails.getUserId());
            emailResponse.setEmail(verificationDetails.getResource());
            verificationResponse.setMessage("Verification done");

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Get Verified",
                            AlgoticUtils.objectToJsonString(verificationResponse),
                            null,
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(verificationResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            null,
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> verification(EmailRequest emailRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "email verification",
                            AlgoticUtils.objectToJsonString(emailRequest),
                            jwtHelper.getUserId(),
                            null));

            GlobalMessageResponse verificationResponse = new GlobalMessageResponse();
            String verificationCode = verificationCode();
            String verificationUrl = emailUrl + verificationCode;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Verification Url",
                            AlgoticUtils.objectToJsonString(verificationUrl),
                            jwtHelper.getUserId(),
                            null));
            VerificationDetails verificationDetails = new VerificationDetails();
            verificationDetails.setResource(emailRequest.getEmail());
            verificationDetails.setResourceType(ResourceType.EMAIL.name());
            verificationDetails.setUserId(jwtHelper.getUserId());
            verificationDetails.setVerificationCode(verificationCode);
            verificationDetails.setIsVerified(false);
            verificationDetails.setVerifiedAt(null);
            verificationDetails.setCreatedAt(new Date());
            VerificationDetails details = verificationDetailsRepo.save(verificationDetails);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "verification Details",
                            AlgoticUtils.objectToJsonString(details),
                            jwtHelper.getUserId(),
                            null));
            emailService.sendVerificationEmail(emailRequest.getEmail(), verificationUrl);
            verificationResponse.setMessage("Verification Code Sent on your updated email");

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Email updated",
                            AlgoticUtils.objectToJsonString(verificationResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));

            return new ResponseEntity<>(verificationResponse, HttpStatus.OK);
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    private String verificationCode() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Verification Code", "Generate the verification code", null, null));
        String rand1 = RandomStringUtils.random(8, true, false).toLowerCase();
        String rand2 = RandomStringUtils.random(8, true, false).toLowerCase();
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("get Verification code", AlgoticUtils.objectToJsonString(rand1 + "" + rand2), null, null));
        return rand1 + "" + rand2;
    }

    @Override
    public ResponseEntity<ProfileDetailsResponse> getProfileDetails() {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("get customer profile details", "Get the details of the customer", null, null));

            ProfileDetailsResponse profileDetailsResponse = new ProfileDetailsResponse();
            VerificationDetails verificationDetails = verificationDetailsRepo.findByUserId(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get customer verification details",
                            AlgoticUtils.objectToJsonString(verificationDetails),
                            jwtHelper.getUserId(),
                            null));

            Users customerDetails = usersRepo.findByID(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "get customer details",
                            AlgoticUtils.objectToJsonString(customerDetails),
                            jwtHelper.getUserId(),
                            null));

            if (customerDetails != null) {
                profileDetailsResponse.setFirstName(customerDetails.getFirstName());
                profileDetailsResponse.setLastName(customerDetails.getLastName());
                profileDetailsResponse.setPhoneNumber(customerDetails.getPhoneNumber());
                if (verificationDetails != null) {
                    if (verificationDetails.getIsVerified().equals(true)) {
                        profileDetailsResponse.setEmail(customerDetails.getEmail());
                    } else {
                        profileDetailsResponse.setEmail(verificationDetails.getResource());
                    }
                } else {
                    profileDetailsResponse.setEmail(customerDetails.getEmail());
                }
                SubscriptionTransactions subscriptionTransaction =
                        subscriptionTransactionsRepo.getSubscription(jwtHelper.getUserId());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "get customer subscription transaction",
                                AlgoticUtils.objectToJsonString(subscriptionTransaction),
                                jwtHelper.getUserId(),
                                null));

                if (subscriptionTransaction != null) {

                    Date currentDate = new Date();
                    Date createdAt = subscriptionTransaction.getCreatedAt();
                    long differenceInTime = currentDate.getTime() - createdAt.getTime();
                    long differenceInDays = (differenceInTime / (1000 * 60 * 60 * 24));
                    if (differenceInDays <= 365) {
                        profileDetailsResponse.setIsActiveSubscription("true");
                    } else profileDetailsResponse.setIsActiveSubscription("false");
                } else profileDetailsResponse.setIsActiveSubscription("false");

                BrokerCustomerDetails brokerCustomerDetails =
                        brokerCustomerDetailsRepo.findBrokerCustomer(jwtHelper.getUserId());
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker customer details",
                                AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                                jwtHelper.getUserId(),
                                null));

                if (brokerCustomerDetails != null) {
                    profileDetailsResponse.setFreeBrokerAccountStatus(brokerCustomerDetails.getStatus());
                } else {
                    profileDetailsResponse.setFreeBrokerAccountStatus(null);
                }
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "get profile details",
                                AlgoticUtils.objectToJsonString(profileDetailsResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(profileDetailsResponse, HttpStatus.OK);
            }

            CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> activeInactiveUsers(String userId, String type) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("customer active inactive", "customer is active or inactive", userId, null));
            GlobalMessageResponse activeInactiveResponse = new GlobalMessageResponse();
            Users customer = usersRepo.findByRole(userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("active inactive user", AlgoticUtils.objectToJsonString(customer), userId, null));
            if (customer != null) {
                if (AlgoticStatus.ACTIVE.name().equalsIgnoreCase(type)) {
                    customer.setStatus(AlgoticStatus.ACTIVE.name());
                    usersRepo.save(customer);
                    activeInactiveResponse.setMessage("User is active");
                } else if (AlgoticStatus.INACTIVE.name().equalsIgnoreCase(type)) {
                    customer.setStatus(AlgoticStatus.INACTIVE.name());
                    usersRepo.save(customer);
                    activeInactiveResponse.setMessage("User is inactive");

                } else if (AlgoticStatus.BLOCKED.name().equalsIgnoreCase(type)) {
                    customer.setStatus(AlgoticStatus.BLOCKED.name());
                    usersRepo.save(customer);
                    activeInactiveResponse.setMessage("User is blocked");

                } else {
                    BusinessErrorCode errorCode = BusinessErrorCode.STATUS_NOT_EXISTS;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }

                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Active or inactive user",
                                AlgoticUtils.objectToJsonString(activeInactiveResponse),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(activeInactiveResponse, HttpStatus.OK);
            }
            CommonErrorCode errorCode = CommonErrorCode.ADMIN_STATUS_CANNOT_CHANGE;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);

        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<InActiveUsersResponse> inactiveUsers(int limit, int offset, String sortBy, String sortOrder) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("User Inactivity age", "Get users those are not active from past 7 days", null, null));

            int limitValue = limit;
            int offsetValue = offset;
            if (limit == 0 && offset == 0) {
                limitValue = 5;
            } else if (limit < 0 || offset < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            List<InActiveUsers> users = activeInactiveCustomerRepo.getInactiveUsers();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Inactive user details",
                            AlgoticUtils.objectToJsonString(users),
                            jwtHelper.getUserId(),
                            null));
            if (sortBy == null) {
                sortBy = "";
            } else {
                sortBy = sortBy;
            }

            if (sortOrder == null) {
                sortOrder = "";
            } else {
                sortOrder = sortOrder;
            }
            Comparator<InActiveUsers> comparator = getSortComparator(sortBy);
            comparator = getSortOrdered(sortOrder, comparator);
            users = users.stream().sorted(comparator).collect(Collectors.toList());

            InActiveUsersResponse response = new InActiveUsersResponse();
            response.setTotal(users.size());
            response.setResult(
                    users.stream().skip(offsetValue).limit(limitValue).toList());
            if (users.isEmpty()) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Users Inactivity age ",
                            AlgoticUtils.objectToJsonString(response),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    private Comparator<InActiveUsers> getSortComparator(String sortBy) {
        switch (sortBy) {
            case "name":
                return Comparator.comparing(InActiveUsers::getName);

            case "id":
                return Comparator.comparing(InActiveUsers::getId);

            case "type":
                return Comparator.comparing(InActiveUsers::getType);

            case "phoneNumber":
                return Comparator.comparing(InActiveUsers::getPhoneNumber);

            case "subscriptionId":
                return Comparator.comparing(InActiveUsers::getSubscriptionId);

            default:
                return Comparator.comparing(InActiveUsers::getInactiveDays);
        }
    }

    private Comparator<InActiveUsers> getSortOrdered(String sortOrder, Comparator<InActiveUsers> comparator) {
        switch (sortOrder) {
            case "desc":
                return comparator.reversed();
            default:
                return comparator;
        }
    }

    @Override
    public ResponseEntity<RenewalHistoryResponse> renewalHistory(String userId) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Renewal History ", "Renewal History of users", jwtHelper.getUserId(), null));
            RenewalHistoryResponse renewalHistoryresponse = new RenewalHistoryResponse();
            String customers = jwtHelper.getUserId();
            if (customers.equals(userId)) {
                SubscriptionTransactions subscriptionTransactions =
                        subscriptionTransactionsRepo.getSubscription(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Subscription transactions",
                                AlgoticUtils.objectToJsonString(subscriptionTransactions),
                                jwtHelper.getUserId(),
                                null));
                if (subscriptionTransactions != null) {
                    List<RenewalResponse> renewalResponsesList = new ArrayList<>();
                    List<SubscriptionTransactions> subscriptionTransactionsList =
                            subscriptionTransactionsRepo.getHistory(customers);
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    "Subscription transactions list ",
                                    AlgoticUtils.objectToJsonString(subscriptionTransactionsList),
                                    jwtHelper.getUserId(),
                                    null));

                    Date createdAt = subscriptionTransactions.getCreatedAt();
                    Date currentDate = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(createdAt);
                    cal.add(Calendar.DAY_OF_MONTH, 365);
                    Date endDate = cal.getTime();

                    SimpleDateFormat parser = new SimpleDateFormat("dd MMM yy");
                    String date = parser.format(createdAt);
                    String dateEnd = parser.format(endDate);
                    long differenceInTime = endDate.getTime() - currentDate.getTime();
                    long daysLeft = (differenceInTime / (1000 * 60 * 60 * 24));

                    for (SubscriptionTransactions subscriptionTransaction : subscriptionTransactionsList) {
                        RenewalResponse renewalResponse = new RenewalResponse();
                        renewalResponse.setAmount(subscriptionTransaction.getAmount());
                        renewalResponse.setDate(date);
                        renewalResponse.setInvoice(null);
                        renewalResponse.setSubscriptionTransactionId(subscriptionTransaction.getId());
                        renewalResponsesList.add(renewalResponse);
                    }

                    Brokers brokers = brokersRepo.findBrokerName(subscriptionTransactions.getBrokerId());
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    "Get Broker name By Id",
                                    AlgoticUtils.objectToJsonString(brokers),
                                    jwtHelper.getUserId(),
                                    null));

                    renewalHistoryresponse.setResult(renewalResponsesList);
                    renewalHistoryresponse.setStartDate(date);
                    renewalHistoryresponse.setEndDate(dateEnd);
                    if (brokers == null) {
                        renewalHistoryresponse.setBroker(null);
                    } else {
                        renewalHistoryresponse.setBroker(brokers.getName());
                    }
                    renewalHistoryresponse.setNoOfDaysLeft(daysLeft);
                }

            } else {
                CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Renewal History of user",
                            AlgoticUtils.objectToJsonString(renewalHistoryresponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(renewalHistoryresponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<byte[]> userInvoice(Integer subscriptionId) {
        String user = jwtHelper.getUserId();

        try {
            log.info(logConfig.getLogHandler().getInfoLog("Pdf for user", "User pdf", jwtHelper.getUserId(), null));
            SubscriptionTransactions subscriptionTransactions =
                    subscriptionTransactionsRepo.getTransactionYear(user, subscriptionId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Subscription Transactions",
                            AlgoticUtils.objectToJsonString(subscriptionTransactions),
                            jwtHelper.getUserId(),
                            null));
            if (subscriptionTransactions == null) {
                BusinessErrorCode errorCode = BusinessErrorCode.INVALID_SUBSCRIPTION_ID;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            if (!user.equals(subscriptionTransactions.getUserId())) {
                BusinessErrorCode errorCode = BusinessErrorCode.SUBSCRIBER_NOT_EXIST;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "User pdf of every year",
                            AlgoticUtils.objectToJsonString("Pdf of a user of the year"),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return pdfCreate(subscriptionTransactions.getUserId(), subscriptionTransactions);

        } catch (AlgoticException e) {
            throw new AlgoticException(e.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<byte[]> pdfCreate(String userId, SubscriptionTransactions subscriptionTransactions) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("Pdf", "pdf generate", jwtHelper.getUserId(), null));
            if (userId != null) {
                String rootPath = "invoices";
                File dir = new File(rootPath + File.separator + userId);
                if (!(dir.isDirectory() && dir.exists())) {
                    dir.mkdir();
                }

                GSTDetails gsTdetails = gstDetailsRepo.findByUserId(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Get Gst details",
                                AlgoticUtils.objectToJsonString(gsTdetails),
                                jwtHelper.getUserId(),
                                null));
                Users users = usersRepo.findByID(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Get User By Id", AlgoticUtils.objectToJsonString(users), jwtHelper.getUserId(), null));
                if (users == null) {
                    CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                if (subscriptionTransactions == null) {
                    subscriptionTransactions = subscriptionTransactionsRepo.getSubscription(userId);
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    "Subscription Transactions",
                                    AlgoticUtils.objectToJsonString(subscriptionTransactions),
                                    jwtHelper.getUserId(),
                                    null));
                }

                SimpleDateFormat sdfYYYY = new SimpleDateFormat("yyyy");
                String subscriptionYear = sdfYYYY.format(subscriptionTransactions.getCreatedAt());
                String existingFilePath = dir.getAbsolutePath() + File.separator + subscriptionYear + "_invoice.pdf";
                if (new File(existingFilePath).exists()) {
                    log.info(logConfig.getLogHandler().getInfoLog("get Pdf", "Pdf", jwtHelper.getUserId(), null));
                    return ResponseEntity.ok().body(Files.readAllBytes(Path.of(existingFilePath)));
                }
                File file =
                        new File(getClass().getResource("/static/Invoice.html").toURI());
                String data = Files.readString(Paths.get(file.toURI()));
                data = data.replace("{customerName}", users.getFirstName() + " " + users.getLastName());
                if (gsTdetails == null) {
                    data = data.replace("{address}", " ");
                } else {
                    data = data.replace("{address}", gsTdetails.getAddress());
                }

                data = data.replace("{email}", users.getEmail());
                data = data.replace("{phoneNumber}", users.getPhoneNumber());
                data = data.replace(
                        "{invoiceNumber}", subscriptionTransactions.getId().toString());
                data = data.replace("{orderNumber}", subscriptionTransactions.getTransactionId());
                SimpleDateFormat parser = new SimpleDateFormat("dd MMM yy");
                String date = parser.format(subscriptionTransactions.getCreatedAt());
                data = data.replace("{invoiceDate}", date);
                data = data.replace(
                        "{amount}", subscriptionTransactions.getAmount().toString());
                data = data.replace("{product}", "Test");
                data = data.replace("{duration}", "12");
                data = data.replace(
                        "{amount}", subscriptionTransactions.getAmount().toString());
                Double subtotal = subscriptionTransactions.getAmount() - 500;
                data = data.replace("{subtotal}", subtotal.toString());

                Double tax = subscriptionTransactions.getAmount() * 0.1;
                data = data.replace("{tax}", tax.toString());
                data = data.replace("{totalAmount}", String.valueOf(subtotal + tax));

                Path path = Paths.get(userId + "_invoice.html");
                Files.writeString(path, data, StandardCharsets.UTF_8);
                File invoiceFile = new File(path.toUri());
                Document doc = Jsoup.parse(invoiceFile, "UTF-8");

                doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
                String outputPath = dir.getAbsoluteFile() + File.separator + subscriptionYear + "_invoice.pdf";
                try (OutputStream os = new FileOutputStream(outputPath)) {
                    ITextRenderer renderer = new ITextRenderer();
                    renderer.setDocumentFromString(doc.html());
                    renderer.layout();
                    renderer.createPDF(os);
                }
                File outputFile = new File(outputPath);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Pdf Created ",
                                AlgoticUtils.objectToJsonString("pdf Created"),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return ResponseEntity.ok().body(Files.readAllBytes(Path.of(outputFile.getPath())));

            } else {
                CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<CustomerManagementResponse> allUsers(
            String name,
            String userId,
            String userType,
            Date lastActiveStartDate,
            Date lastActiveEndDate,
            Date renewalStartDate,
            Date renewalEndDate,
            String status,
            String paymentStatus,
            Integer limit,
            Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Customer Management",
                            "Customer managemnet with the filters applied",
                            jwtHelper.getUserId(),
                            null));
            int limitValue = limit == null ? 5 : limit;
            int offsetValue = offset == null ? 0 : offset;
            if (name == null) {
                name = "";
            }
            if (userId == null) {
                userId = "";
            }
            if (userType == null) {
                userType = "";
            }
            if (status == null) {
                status = "";
            }
            if (paymentStatus == null) {
                paymentStatus = "";
            } else if (limitValue < 0 || offsetValue < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            List<CustomerManagements> users = customerRepo.getUsers(
                    name,
                    userId,
                    userType,
                    lastActiveStartDate,
                    lastActiveEndDate,
                    renewalStartDate,
                    renewalEndDate,
                    status,
                    paymentStatus,
                    limitValue,
                    offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Users List", AlgoticUtils.objectToJsonString(users), jwtHelper.getUserId(), null));

            Object[] userCount = customerRepo.getUsersCount(
                    name,
                    userId,
                    userType,
                    lastActiveStartDate,
                    lastActiveEndDate,
                    renewalStartDate,
                    renewalEndDate,
                    status,
                    paymentStatus);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Count of users", AlgoticUtils.objectToJsonString(userCount), jwtHelper.getUserId(), null));
            CustomerManagementResponse response = new CustomerManagementResponse();
            response.setTotal(Integer.parseInt(userCount[0].toString()));
            response.setResult(users);

            if (users.isEmpty()) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Response of users",
                            AlgoticUtils.objectToJsonString(response),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> deleteUser(String id) {
        try {
            log.info(logConfig.getLogHandler().getInfoLog("delete user", "delete user by its id", id, null));
            GlobalMessageResponse globalMessageResponse = new GlobalMessageResponse();
            Users users = usersRepo.findByIdAndIsDeleted(id, false);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Delete a particular user by its id", AlgoticUtils.objectToJsonString(users), id, null));
            if (users != null) {
                users.setStatus(AlgoticStatus.INACTIVE.name());
                users.setIsDeleted(true);
                Users user = usersRepo.save(users);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Marked user as delete in database",
                                AlgoticUtils.objectToJsonString(user),
                                jwtHelper.getUserId(),
                                null));
                globalMessageResponse.setMessage("User deleted");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "delete a user ",
                                AlgoticUtils.objectToJsonString(globalMessageResponse),
                                id,
                                String.valueOf(HttpStatus.OK.value())));

                return new ResponseEntity<>(globalMessageResponse, HttpStatus.OK);
            }
            CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<List<UserActivities>> userCount(ActiveUsersHistoryRequest activeUsersHistoryRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "User Activity Count",
                            AlgoticUtils.objectToJsonString(activeUsersHistoryRequest),
                            null,
                            null));
            List<UserActivities> usersActivity = userActivityRepo.getUserActivity(activeUsersHistoryRequest.getDays());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("users Activity", AlgoticUtils.objectToJsonString(usersActivity), null, null));
            if (usersActivity.isEmpty()) {
                CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                log.error(logConfig
                        .getLogHandler()
                        .getErrorLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Active user history",
                            AlgoticUtils.objectToJsonString(usersActivity),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(usersActivity, HttpStatus.OK);

        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> accountDisconnect(String userId) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Account Disconnect", "Get the Account disconnect", jwtHelper.getUserId(), null));
            GlobalMessageResponse response = new GlobalMessageResponse();
            String customers = jwtHelper.getUserId();
            if (customers.equals(userId)) {
                BrokerCustomerDetails brokerCustomerDetails = brokerCustomerDetailsRepo.findBrokerUserById(customers);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker Customer Details",
                                AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                                jwtHelper.getUserId(),
                                null));
                brokerCustomerDetails.setIsActive(false);
                BrokerCustomerDetails customerDetails = brokerCustomerDetailsRepo.save(brokerCustomerDetails);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker Customer Details saved",
                                AlgoticUtils.objectToJsonString(customerDetails),
                                jwtHelper.getUserId(),
                                null));
                BrokerSessionDetails brokerSessionDetails = brokerSessionDetailsRepo.getSessionId(customers);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker Session Details",
                                AlgoticUtils.objectToJsonString(brokerSessionDetails),
                                jwtHelper.getUserId(),
                                null));
                brokerSessionDetails.setIsActive(false);
                BrokerSessionDetails sessionDetails = brokerSessionDetailsRepo.save(brokerSessionDetails);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker Session Details Save",
                                AlgoticUtils.objectToJsonString(sessionDetails),
                                jwtHelper.getUserId(),
                                null));
                response.setMessage("Account is disconnected");
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "customer account disconnect",
                                AlgoticUtils.objectToJsonString(response),
                                jwtHelper.getUserId(),
                                String.valueOf(HttpStatus.OK.value())));
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            CommonErrorCode errorCode = CommonErrorCode.CUSTOMER_NOT_EXISTS;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<RegistrationResponse> registration(Integer limit, Integer offset) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Registration", "Registration Reports", jwtHelper.getUserId(), null));
            int limitValue = limit == null ? 5 : limit;
            int offsetValue = offset == null ? 0 : offset;
            if (limitValue < 0 || offsetValue < 0) {
                CommonErrorCode errorCode = CommonErrorCode.LIMIT;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            List<RegistrationDataResponse> registrationDataResponsesList = new ArrayList<>();
            RegistrationResponse registrationResponse = new RegistrationResponse();
            Integer count = usersRepo.getUsersCount();
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Registration Count", AlgoticUtils.objectToJsonString(count), jwtHelper.getUserId(), null));
            registrationResponse.setTotal(count);
            List<Users> userData = usersRepo.findUsers(limitValue, offsetValue);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("User Data", AlgoticUtils.objectToJsonString(userData), jwtHelper.getUserId(), null));
            for (Users users : userData) {
                Date lastActivedate = users.getModifiedAt();
                String stage = users.getStage();
                Date currentDate = new Date();
                if (lastActivedate == null) {
                    lastActivedate = users.getCreatedAt();
                }
                long differenceInTime = currentDate.getTime() - lastActivedate.getTime();
                long daysLeft = (differenceInTime / (1000 * 60 * 60 * 24));
                if (users != null) {
                    RegistrationDataResponse response = new RegistrationDataResponse();
                    if (StageType.CREATED.name().equals(stage)) {
                        response.setName(users.getFirstName() + " " + users.getLastName());
                        response.setEmail(users.getEmail());
                        response.setPhoneNumber(users.getPhoneNumber());
                        response.setMessage("Sign Up done");
                        response.setDays(daysLeft);
                    } else if (StageType.SUBSCRIBE.name().equals(stage)) {
                        response.setName(users.getFirstName() + " " + users.getLastName());
                        response.setEmail(users.getEmail());
                        response.setPhoneNumber(users.getPhoneNumber());
                        response.setDays(daysLeft);
                        response.setMessage("Subscription done");
                    } else if (StageType.BROKERCONNECTED.name().equals(stage)) {
                        response.setName(users.getFirstName() + " " + users.getLastName());
                        response.setEmail(users.getEmail());
                        response.setPhoneNumber(users.getPhoneNumber());
                        response.setDays(daysLeft);
                        response.setMessage("Broker connection done");
                    } else {
                        response.setName(users.getFirstName() + " " + users.getLastName());
                        response.setEmail(users.getEmail());
                        response.setPhoneNumber(users.getPhoneNumber());
                        response.setDays(null);
                        response.setMessage("Stage is not available");
                    }
                    registrationDataResponsesList.add(response);
                } else {
                    CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                }
                registrationResponse.setResult(registrationDataResponsesList);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Customer registration",
                            AlgoticUtils.objectToJsonString(registrationResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(registrationResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> adminStatus(String userId) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Admin Status", "Admin can change the  status of user", jwtHelper.getUserId(), null));
            BrokerCustomerDetails brokerCustomerDetails = brokerCustomerDetailsRepo.findBrokerCustomer(userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker Customer Details",
                            AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                            jwtHelper.getUserId(),
                            null));
            if (brokerCustomerDetails.getStatus().equalsIgnoreCase(AlgoticStatus.CREATED.name())) {
                brokerCustomerDetails.setStatus(AlgoticStatus.ACTIVE.name());
                BrokerCustomerDetails customerDetails = brokerCustomerDetailsRepo.save(brokerCustomerDetails);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker Customer Details save",
                                AlgoticUtils.objectToJsonString(customerDetails),
                                jwtHelper.getUserId(),
                                null));
            } else {
                BusinessErrorCode errorCode = BusinessErrorCode.STATUS_ALREADY_CREATED;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Admin can change status of a customer",
                            AlgoticUtils.objectToJsonString(new GlobalMessageResponse()),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> brokerStatus(BrokerCustomerRequest customerBrokerRequest) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker Status",
                            AlgoticUtils.objectToJsonString(customerBrokerRequest),
                            jwtHelper.getUserId(),
                            null));
            BrokerCustomerDetails customerDetails = brokerCustomerDetailsRepo.findBrokerCustomer(jwtHelper.getUserId());
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker Customer Details",
                            AlgoticUtils.objectToJsonString(customerDetails),
                            jwtHelper.getUserId(),
                            null));
            if (customerDetails == null) {
                BrokerCustomerDetails brokerCustomerDetails = new BrokerCustomerDetails();
                brokerCustomerDetails.setStatus(AlgoticStatus.CREATED.name());
                brokerCustomerDetails.setBrokerId(customerBrokerRequest.getBrokerId());
                brokerCustomerDetails.setUserId(jwtHelper.getUserId());
                brokerCustomerDetails.setCreatedAt(new Date());
                brokerCustomerDetails.setModifiedAt(new Date());
                BrokerCustomerDetails details = brokerCustomerDetailsRepo.save(brokerCustomerDetails);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                "Broker Customer Details saved",
                                AlgoticUtils.objectToJsonString(details),
                                jwtHelper.getUserId(),
                                null));
            } else {
                BusinessErrorCode errorCode = BusinessErrorCode.BROKER_REQUEST_IN_PROGRESS;
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog(
                                errorCode.getErrorCode(),
                                errorCode.getErrorMessage(),
                                jwtHelper.getUserId(),
                                String.valueOf(errorCode.getHttpStatus().value())));
                throw new AlgoticException(errorCode);
            }
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Broker status saved",
                            AlgoticUtils.objectToJsonString(new GlobalMessageResponse()),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception e) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    @Override
    public String getMotilalToken(String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Method start -> getMotilalToken", "Getting motilal oswal token", userId, null));
        BrokerCustomerDetails brokerCustomerDetails = Optional.ofNullable(
                        brokerCustomerDetailsRepo.findBrokerUserById(userId))
                .orElseThrow(() -> {
                    BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                });
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "brokerCustomer details",
                        AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                        userId,
                        null));
        BrokerSessionDetails brokerSessionDetails = brokerSessionDetailsRepo
                .getSessionIdByBrokerCusId(brokerCustomerDetails.getId())
                .orElseThrow(() -> {
                    BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                });
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker Session details", AlgoticUtils.objectToJsonString(brokerSessionDetails), userId, null));

        String sessionId = brokerSessionDetails.getSessionId();

        if (sessionId == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        log.info("Successfully get the motilal token");
        return sessionId;
    }

    /**
     * @param userId
     * @return Broker specific session token
     */
    @Override
    public String getSessionToken(String userId, BrokerEnum brokerName) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Method start -> getSessionToken", "Getting broker specific session token", userId, null));
        BrokerCustomerDetails brokerCustomerDetails = Optional.ofNullable(
                        brokerCustomerDetailsRepo.findBrokerUserById(userId))
                .orElseThrow(() -> {
                    BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                });

        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "brokerCustomer details",
                        AlgoticUtils.objectToJsonString(brokerCustomerDetails),
                        userId,
                        null));
        if (!brokerName.getBrokerId().equals(brokerCustomerDetails.getBrokerId())) {
            throw new AlgoticException(CommonErrorCode.BROKER_CUSTOMER_NOT_LINKED);
        }
        BrokerSessionDetails brokerSessionDetails = brokerSessionDetailsRepo
                .getSessionIdByBrokerCusId(brokerCustomerDetails.getId())
                .orElseThrow(() -> {
                    BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog(
                                    errorCode.getErrorCode(),
                                    errorCode.getErrorMessage(),
                                    jwtHelper.getUserId(),
                                    String.valueOf(errorCode.getHttpStatus().value())));
                    throw new AlgoticException(errorCode);
                });
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker Session details", AlgoticUtils.objectToJsonString(brokerSessionDetails), userId, null));

        String sessionId = brokerSessionDetails.getSessionId();

        if (sessionId == null) {
            BusinessErrorCode errorCode = BusinessErrorCode.BROKER_SESSION_EXPIRED;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
        log.info("Successfully get the broker {} token", brokerName);
        return sessionId;
    }
}
