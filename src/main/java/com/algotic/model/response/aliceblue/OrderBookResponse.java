package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderBookResponse {
    @JsonProperty("Prc")
    private String price;

    @JsonProperty("RequestID")
    public String requestID;

    @JsonProperty("Cancelqty")
    public Integer cancelQuantity;

    @JsonProperty("discQtyPerc")
    private String discloseQuantitypercentage;

    @JsonProperty("customText")
    public String customText;

    @JsonProperty("Mktpro")
    private String marketProtection;

    @JsonProperty("defmktproval")
    private String defaultMarketProtection;

    @JsonProperty("optionType")
    public String optionType;

    @JsonProperty("usecs")
    private String uSeconds;

    @JsonProperty("mpro")
    private String marketProtectionFlag;

    @JsonProperty("Qty")
    public Integer quantity;

    @JsonProperty("ordergenerationtype")
    private String orderGenerationType;

    @JsonProperty("Unfilledsize")
    public Integer unfilledSize;

    @JsonProperty("orderAuthStatus")
    public String orderAuthStatus;

    @JsonProperty("Usercomments")
    public String userComments;

    @JsonProperty("ticksize")
    private String tickerSize;

    @JsonProperty("Prctype")
    public String priceType;

    @JsonProperty("Status")
    public String status;

    @JsonProperty("Minqty")
    public Integer minimumQuantity;

    @JsonProperty("orderCriteria")
    public String orderCriteria;

    @JsonProperty("Exseg")
    public String exchangeSegment;

    @JsonProperty("Sym")
    private String scripToken;

    @JsonProperty("multiplier")
    public String multiplier;

    @JsonProperty("ExchOrdID")
    public String exchangeOrderID;

    @JsonProperty("ExchConfrmtime")
    public String exchangeconfirmationtime;

    @JsonProperty("Pcode")
    public String productCode;

    @JsonProperty("SyomOrderId")
    public String symbolOrderId;

    @JsonProperty("Dscqty")
    public Integer discloseQuantity;

    @JsonProperty("Exchange")
    public String exchange;

    @JsonProperty("Ordvaldate")
    public String orderValidateDate;

    @JsonProperty("accountId")
    public String accountId;

    @JsonProperty("exchangeuserinfo")
    public String exchangeUserInformation;

    @JsonProperty("Avgprc")
    public String averagePrice;

    @JsonProperty("Trgprc")
    public String triggerPrice;

    @JsonProperty("Trantype")
    public String transactionType;

    @JsonProperty("bqty")
    public String buyQuantity;

    @JsonProperty("Trsym")
    public String tradingSymbol;

    @JsonProperty("Fillshares")
    public Integer fillShares;

    @JsonProperty("AlgoCategory")
    public String algoCategory;

    @JsonProperty("sipindicator")
    public String sipIndicator;

    @JsonProperty("strikePrice")
    public String strikePrice;

    @JsonProperty("reporttype")
    public String reportType;

    @JsonProperty("AlgoID")
    public String algoID;

    @JsonProperty("noMktPro")
    private String noMarketProtectionFlag;

    @JsonProperty("BrokerClient")
    public String brokerClient;

    @JsonProperty("OrderUserMessage")
    public String orderUserMessage;

    @JsonProperty("decprec")
    private String decimalPrecision;

    @JsonProperty("ExpDate")
    public String expiryDate;

    @JsonProperty("COPercentage")
    public double cOPercentage;

    @JsonProperty("marketprotectionpercentage")
    public String marketProtectionPercentage;

    @JsonProperty("Nstordno")
    public String nestOrderNumber;

    @JsonProperty("ExpSsbDate")
    public String expiryDateOfScrip;

    @JsonProperty("OrderedTime")
    public String orderedTime;

    @JsonProperty("RejReason")
    public String rejectionReason;

    @JsonProperty("modifiedBy")
    public String modifiedBy;

    @JsonProperty("Scripname")
    public String scripName;

    @JsonProperty("stat")
    public String stat;

    @JsonProperty("orderentrytime")
    public String orderEntryTime;

    @JsonProperty("PriceDenomenator")
    public String priceDenominator;

    @JsonProperty("panNo")
    public String panNumber;

    @JsonProperty("RefLmtPrice")
    public double referenceLimitPrice;

    @JsonProperty("PriceNumerator")
    public String priceNumerator;

    @JsonProperty("token")
    public String token;

    @JsonProperty("ordersource")
    public String orderSource;

    @JsonProperty("Validity")
    public String validity;

    @JsonProperty("GeneralDenomenator")
    public String generalDenominator;

    @JsonProperty("series")
    public String series;

    @JsonProperty("InstName")
    public String instrumentName;

    @JsonProperty("GeneralNumerator")
    public String generalNumerator;

    @JsonProperty("user")
    public String user;

    @JsonProperty("remarks")
    public String remarks;

    @JsonProperty("iSinceBOE")
    private Integer secondsSinceBoe;
}
