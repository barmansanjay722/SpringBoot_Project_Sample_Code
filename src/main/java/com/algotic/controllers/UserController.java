package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.data.entities.UserActivities;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.*;
import com.algotic.model.response.*;
import com.algotic.services.EmailService;
import com.algotic.services.UserService;
import com.algotic.utils.AlgoticUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtHelper jwtHelper;

    @CrossOrigin
    @PostMapping(
            value = "/customer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateCustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest createCustomerRequest, BindingResult errors) {

        if (errors.hasErrors()) {
            CommonErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            errors.getAllErrors().toString(),
                            errorCode.getHttpStatus().toString()));

            AlgoticException exception = new AlgoticException(errorCode);

            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "new customer details", AlgoticUtils.objectToJsonString(createCustomerRequest), null, null));
        return userService.createCustomer(createCustomerRequest);
    }

    @CrossOrigin
    @GetMapping("/customer/profile")
    public ResponseEntity<ProfileDetailsResponse> getProfileDetails() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("get customer details", "Get the details of the customer", null, null));
        return userService.getProfileDetails();
    }

    @CrossOrigin
    @PutMapping("/customer/{id}")
    public ResponseEntity<GlobalMessageResponse> updateCustomerDetails(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest updateCustomerRequest,
            BindingResult errors) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Update Customer Details", AlgoticUtils.objectToJsonString(updateCustomerRequest), null, null));
        return userService.updateCustomer(id, updateCustomerRequest);
    }

    @CrossOrigin
    @PutMapping("/customer/email/verify")
    public ResponseEntity<GlobalMessageResponse> verification(
            @Valid @RequestBody EmailRequest emailRequest, BindingResult errors) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Email updated", AlgoticUtils.objectToJsonString(emailRequest), null, null));
        return userService.verification(emailRequest);
    }

    @CrossOrigin
    @GetMapping("/customer/email/verify/{verificationCode}")
    public ResponseEntity<GlobalMessageResponse> getVerified(@PathVariable String verificationCode) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Get Verified", "If verification code matches then the email update ", null, null));
        return userService.getVerified(verificationCode);
    }

    @CrossOrigin
    @PostMapping("/customer/gst")
    public ResponseEntity<GlobalMessageResponse> saveGstDetails(
            @Valid @RequestBody GstDetailsRequest gstDetailsRequest, BindingResult errors) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("customer gst details", AlgoticUtils.objectToJsonString(gstDetailsRequest), null, null));
        return userService.saveGstDetails(gstDetailsRequest);
    }

    @CrossOrigin
    @GetMapping("/customer/gst/{userId}")
    public ResponseEntity<GstResponse> getGst(@PathVariable String userId) {
        log.info(logConfig.getLogHandler().getInfoLog("Gst Details ", "Gst details of customer", null, null));
        return userService.getGstDetails(userId);
    }

    @CrossOrigin
    @PostMapping("/customer/{userId}/{type}")
    public ResponseEntity<GlobalMessageResponse> getActiveInactiveUsers(
            @PathVariable String userId, @PathVariable String type) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Active/Inactive customer", "Active and in active the customer by id", null, null));
        return userService.activeInactiveUsers(userId, type);
    }

    @CrossOrigin
    @GetMapping("/customer/inactiveUser")
    public ResponseEntity<InActiveUsersResponse> inactive(
            @Nullable @RequestParam int limit,
            @Nullable @RequestParam int offset,
            @Nullable @RequestParam String sortBy,
            @Nullable @RequestParam String sortOrder) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Inactive users  ", "Get details of users which are inactive ", null, null));
        return userService.inactiveUsers(limit, offset, sortBy, sortOrder);
    }

    @CrossOrigin
    @DeleteMapping("/customer/{id}")
    public ResponseEntity<GlobalMessageResponse> deleteUser(@PathVariable String id) {
        log.info(logConfig.getLogHandler().getInfoLog("Delete user  ", "Delete user by id ", null, null));
        return userService.deleteUser(id);
    }

    @CrossOrigin
    @PostMapping("/customer/activeUser/history")
    public ResponseEntity<List<UserActivities>> adminGraph(
            @Valid @RequestBody ActiveUsersHistoryRequest activeUsersHistoryRequest, BindingResult errors) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Active user history", AlgoticUtils.objectToJsonString(activeUsersHistoryRequest), null, null));
        return userService.userCount(activeUsersHistoryRequest);
    }

    @CrossOrigin
    @GetMapping("/customer/management")
    public ResponseEntity<CustomerManagementResponse> allUsers(
            @Nullable @RequestParam String name,
            @Nullable @RequestParam String userId,
            @Nullable @RequestParam String userType,
            @Nullable @RequestParam("lastActiveStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date lastActiveStartDate,
            @Nullable @RequestParam("lastActiveEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date lastActiveEndDate,
            @Nullable @RequestParam("renewalStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date renewalStartDate,
            @Nullable @RequestParam("renewalEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date renewalEndDate,
            @Nullable @RequestParam String status,
            @Nullable @RequestParam String paymentStatus,
            @Nullable @RequestParam Integer limit,
            @Nullable @RequestParam Integer offset) {
        log.info(logConfig.getLogHandler().getInfoLog("all users  ", "All details of users ", null, null));
        return userService.allUsers(
                name,
                userId,
                userType,
                lastActiveStartDate,
                lastActiveEndDate,
                renewalStartDate,
                renewalEndDate,
                status,
                paymentStatus,
                limit,
                offset);
    }

    @CrossOrigin
    @GetMapping("/customer/{userId}/subscription")
    public ResponseEntity<RenewalHistoryResponse> renewalHistory(@PathVariable String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Renewal history  ", "Get details of renewal history of user ", null, null));
        return userService.renewalHistory(userId);
    }

    @CrossOrigin
    @PostMapping("/customer/{userId}/account/disconnect")
    public ResponseEntity<GlobalMessageResponse> disconnect(@PathVariable String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("disconnect account  ", "disconnect a customer account  ", null, null));
        return userService.accountDisconnect(userId);
    }

    @CrossOrigin
    @GetMapping(value = "/customer/{userId}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfCreate(@PathVariable String userId) {
        log.info(logConfig.getLogHandler().getInfoLog("Pdf Create ", "Get details Of Pdf ", null, null));
        return userService.pdfCreate(userId, null);
    }

    @CrossOrigin
    @PostMapping("/customer/{userId}/approve")
    public ResponseEntity<GlobalMessageResponse> admin(@PathVariable String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("admin status", "admin can change status of a customer ", null, null));
        return userService.adminStatus(userId);
    }

    @CrossOrigin
    @GetMapping(value = "/customer/invoice/{subscriptionId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> userInvoice(@PathVariable Integer subscriptionId) {
        log.info(logConfig.getLogHandler().getInfoLog("User Pdf", "Pdf of user for this year", null, null));
        return userService.userInvoice(subscriptionId);
    }

    @CrossOrigin
    @PostMapping("customer/broker/created")
    public ResponseEntity<GlobalMessageResponse> brokerStatus(
            @Valid @RequestBody BrokerCustomerRequest customerBrokerRequest, BindingResult errors) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig.getLogHandler().getInfoLog("broker status created", "broker status created ", null, null));
        return userService.brokerStatus(customerBrokerRequest);
    }
}
