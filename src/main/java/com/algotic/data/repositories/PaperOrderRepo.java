package com.algotic.data.repositories;

import com.algotic.data.entities.PaperOrders;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaperOrderRepo extends JpaRepository<PaperOrders, String> {
    @Query(
            value =
                    "select * from PaperOrder o where o.UserId=:userId and date(o.CreatedAt)=curdate() and o.Status=:status",
            nativeQuery = true)
    List<PaperOrders> getOrders(String userId, String status);

    @Query(
            value = "Select  SUM(CASE WHEN TransactionType = 'BUY' THEN Quantity\n" + "             ELSE -Quantity\n"
                    + "        END) AS TotalQuantity  from PaperOrder where UserId=:userId \n"
                    + "        and ProductCode='CNC' and TradingSymbol=:tradingSymbol and\n"
                    + "    TradeType = 'Paper' and Status='Complete' and  DATE(CreatedAt) = CURDATE() \n"
                    + "GROUP BY \n"
                    + "    TradingSymbol, UserId, Token, ProductCode, Exchange",
            nativeQuery = true)
    Integer paperOrderQty(String userId, String tradingSymbol);

    @Query(
            value =
                    "select * from PaperOrder o where o.Token=:token and o.Status='Complete' and date(o.CreatedAt)=curdate() and UserId=:userId limit 1",
            nativeQuery = true)
    PaperOrders findDatabyToken(String token, String userId);

    @Query(
            value =
                    "select * from PaperOrder o where o.Token=:token and o.Status='Complete' and UserId=:userId limit 1",
            nativeQuery = true)
    PaperOrders findDatabyNormal(String token, String userId);

    @Query(
            value =
                    "Select sum(Quantity) as Quantity from PaperOrder o where o.Token=:token and po.Status='Complete' and o.TransactionType=:transactionType and PriceType=:priceType and date(o.CreatedAt)=curdate() limit 1",
            nativeQuery = true)
    Integer getQty(String token, String transactionType, String priceType);

    @Query(
            value =
                    "select count(Token) from PaperOrder o where o.Token=:token and o.Status='Complete' and o.PriceType=:priceType and o.TransactionType=:transactionType and date(o.CreatedAt)=curdate()",
            nativeQuery = true)
    Integer countOfToken(String token, String priceType, String transactionType);

    @Query(
            value =
                    "select sum(Quantity) from PaperOrder o where o.Token=:token and o.IsHolding <> true and o.Status='Complete' and o.UserId=:userId and o.PriceType IN('MKT','MARKET') and o.TransactionType=:transactionType and ProductCode=:productCode and date(o.CreatedAt)=curdate()",
            nativeQuery = true)
    Integer countOfTokenProductCode(String token, String transactionType, String productCode, String userId);

    @Query(
            value =
                    "select sum(Quantity) from PaperOrder o where o.Token=:token and o.IsHolding <> true and o.Status='Complete' and o.UserId=:userId and o.PriceType IN('MKT','MARKET') and o.TransactionType=:transactionType and ProductCode=:productCode ",
            nativeQuery = true)
    Integer countOfTokenNormal(String token, String transactionType, String productCode, String userId);

    @Query(
            value =
                    "select DISTINCT(po.Token) from PaperOrder po where date(po.CreatedAt) =date(CURRENT_DATE()) and po.UserId=:userId and po.ProductCode ='MIS' ",
            nativeQuery = true)
    List<String> findByMISTokens(String userId);

    @Query(
            value = "select DISTINCT(po.UserId) from PaperOrder po where date(po.CreatedAt) =date(CURRENT_DATE())",
            nativeQuery = true)
    List<String> findUserid();

    @Query(
            value =
                    "select DISTINCT(po.UserId) from PaperOrder po where date(po.CreatedAt) =date(CURRENT_DATE()) and po.TradeType='PAPER' and po.ProductCode='CNC'",
            nativeQuery = true)
    List<String> findHoldingUserid();

    @Query(
            value =
                    "select DISTINCT po.Token from PaperOrder po where date(po.CreatedAt) =date(CURRENT_DATE()) and po.TransactionType in ('BUY','SELL') and po.Status='COMPLETE' and po.UserId=:userId",
            nativeQuery = true)
    List<String> getAllTokensForBuyAndSell(String userId);

    @Query(
            value =
                    "select DISTINCT(po.ProductCode) from PaperOrder po where po.Token=:token and date(po.CreatedAt) =date(CURRENT_DATE()) and po.TransactionType in ('BUY','SELL')and po.Status='COMPLETE' and po.UserId=:userId",
            nativeQuery = true)
    List<String> findProductCodeByUserIdAndToken(String userId, String token);

    @Query(
            value = "select * from PaperOrder o where o.UserId=:userId and date(o.CreatedAt)=curdate()",
            nativeQuery = true)
    List<PaperOrders> findAllData(String userId);

    @Query(
            value =
                    "Select sum(Quantity) as Quantity from PaperOrder o where  o.TransactionType='BUY' and o.ProductCode='CNC' and date(o.CreatedAt)=curdate() and o.TradingSymbol=:tradingsymbol and userId=:userId group by TradingSymbol",
            nativeQuery = true)
    Integer quantity(String tradingsymbol, String userId);

    @Query(value = "select * from PaperOrder o where o.Expiry=curdate()", nativeQuery = true)
    List<PaperOrders> instrumentExpiryDate();

    @Query(
            value =
                    "select * from PaperOrder o where o.Exchange=:exchange and o.Expiry=curdate() and o.ProductCode!='MIS'",
            nativeQuery = true)
    List<PaperOrders> instrumentData(String exchange);

    @Query(
            value =
                    "select * from PaperOrder o where o.Token=:token and o.Status='Complete' and o.UserId=:userId and o.Expiry=curdate() limit 1",
            nativeQuery = true)
    PaperOrders findInstrumentByExchange(String token, String userId);

    @Query(
            value =
                    "select sum(Quantity) from PaperOrder o where o.Token=:token and o.Status='Complete' and o.UserId=:userId and o.PriceType IN('MKT','MARKET') and o.TransactionType=:transactionType and o.ProductCode=:productCode and o.Expiry=curdate()",
            nativeQuery = true)
    Integer countOfInstrumentTokenProductCode(String token, String transactionType, String productCode, String userId);

    @Query(
            value =
                    "select count(Token) from PaperOrder o where o.Token=:token and o.Status='Complete' and o.PriceType IN('MKT','MARKET') and o.TransactionType=:transactionType and o.Expiry=curdate()",
            nativeQuery = true)
    Integer countOfInstrumentToken(String token, String transactionType);

    @Query(
            value =
                    "Select sum(Quantity) as Quantity from PaperOrder o where o.Token=:token and po.Status='Complete' and o.TransactionType=:transactionType and o.PriceType IN('MKT','MARKET') and o.Expiry=curdate() limit 1",
            nativeQuery = true)
    Integer getInstrumentQty(String token, String transactionType);

    @Query(
            value =
                    "select * from PaperOrder o where o.Token=:token and o.Status='Complete' and o.UserId=:userId and o.Expiry=curdate() limit 1",
            nativeQuery = true)
    PaperOrders findDatabyInstrumentToken(String token, String userId);
}
