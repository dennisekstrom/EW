package trading;

import com.fxcore2.Constants;
import com.fxcore2.O2GLoginRules;
import com.fxcore2.O2GPermissionChecker;
import com.fxcore2.O2GPermissionStatus;
import com.fxcore2.O2GRequest;
import com.fxcore2.O2GRequestFactory;
import com.fxcore2.O2GRequestParamsEnum;
import com.fxcore2.O2GSession;
import com.fxcore2.O2GTradeRow;
import com.fxcore2.O2GValueMap;

/**
 * Class for creating live market orders of different kinds.
 * 
 * @author Tobias W
 * 
 */
public class OrderController {

	private final O2GSession mSession;

	public OrderController(O2GSession session) {
		mSession = session;
	}

	/**
	 * Open position
	 * 
	 * @param accountID
	 * @param offerID
	 * @param buySell
	 * @param amount
	 * @param responseListener
	 * @return
	 * @throws InterruptedException
	 */
	public boolean openPosition(String accountID, String offerID,
			String buySell, int amount, ResponseListener responseListener)
			throws InterruptedException {
		boolean result;
		O2GRequestFactory requestFactory = mSession.getRequestFactory();
		if (requestFactory == null) {
			System.out.println("Cannot create request factory");
			return false;
		}
		O2GValueMap valuemap = requestFactory.createValueMap();
		valuemap.setString(O2GRequestParamsEnum.COMMAND,
				Constants.Commands.CreateOrder);
		valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
				Constants.Orders.TrueMarketOpen);
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID, accountID);
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, offerID);
		valuemap.setString(O2GRequestParamsEnum.BUY_SELL, buySell);
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, amount);
		valuemap.setString(O2GRequestParamsEnum.CUSTOM_ID, "OpenMarketOrder");
		O2GRequest request = requestFactory.createOrderRequest(valuemap);
		if (request != null) {
			responseListener.setRequestID(request.getRequestId());
			mSession.sendRequest(request);
			// responseListener.waitEvents();
			// Call to PositionController is done in ResponseListener

			// Thread.sleep(1000);
			// position opened successfully
			result = true;
		} else {
			System.out.println(requestFactory.getLastError());
			result = false;
		}

		return result;
	}

	/**
	 * Close position.
	 * 
	 * @param tradeRow
	 * @param tableListener
	 * @param instrument
	 * @return
	 * @throws InterruptedException
	 */

	public boolean closePosition(O2GTradeRow tradeRow,
			TableListener tableListener, String instrument)
			throws InterruptedException {
		boolean result;
		O2GRequestFactory requestFactory = mSession.getRequestFactory();
		if (requestFactory == null) {
			System.out.println("Cannot create request factory");
			return false;
		}
		O2GLoginRules loginRules = mSession.getLoginRules();
		O2GPermissionChecker permissionChecker = loginRules
				.getPermissionChecker();
		O2GValueMap valuemap = requestFactory.createValueMap();
		valuemap.setString(O2GRequestParamsEnum.COMMAND, "CreateOrder");
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
				tradeRow.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, tradeRow.getOfferID());
		valuemap.setString(O2GRequestParamsEnum.BUY_SELL, tradeRow.getBuySell()
				.equals(Constants.Buy) ? Constants.Sell : Constants.Buy);
		valuemap.setString(O2GRequestParamsEnum.CUSTOM_ID, "CloseMarketOrder");
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, tradeRow.getAmount());
		if (permissionChecker.canCreateMarketCloseOrder(instrument) != O2GPermissionStatus.PERMISSION_ENABLED) {
			valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
					Constants.Orders.TrueMarketOpen);
		} else {
			valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
					Constants.Orders.TrueMarketClose);
			valuemap.setString(O2GRequestParamsEnum.TRADE_ID,
					tradeRow.getTradeID());
		}
		O2GRequest request = requestFactory.createOrderRequest(valuemap);
		if (request != null) {
			tableListener.setRequestID(request.getRequestId());
			mSession.sendRequest(request);
			tableListener.waitEvents();
			// close position was succcessful

			result = true;
		} else {
			System.out.println(requestFactory.getLastError());
			result = false;
		}
		return result;
	}

	/**
	 * Attach a limit to an already existing trade.
	 * 
	 * @param trade
	 * @param limit
	 */
	public void attachLimit(O2GTradeRow trade, double limit) {

		O2GRequestFactory factory = mSession.getRequestFactory();

		if (factory == null)
			return;
		O2GValueMap valuemap = factory.createValueMap();

		valuemap.setString(O2GRequestParamsEnum.COMMAND,
				Constants.Commands.CreateOrder);
		valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
				Constants.Orders.Limit);
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
				trade.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, trade.getOfferID());
		valuemap.setString(O2GRequestParamsEnum.TRADE_ID, trade.getTradeID());
		if (trade.getBuySell().equals("B"))
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, "S");
		else
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, "B");
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, trade.getAmount());
		valuemap.setDouble(O2GRequestParamsEnum.RATE, limit);
		// valuemap.setDouble(O2GRequestParamsEnum.RATE_LIMIT, limit);
		valuemap.setString(O2GRequestParamsEnum.CUSTOM_ID, "LimitOrder");
		O2GRequest request = factory.createOrderRequest(valuemap);
		mSession.sendRequest(request);

	}

	/**
	 * Attach a stop loss to an already existing trade.
	 * 
	 * @param trade
	 * @param stopLoss
	 * @return orderID
	 */
	public void attachStop(O2GTradeRow trade, double stopLoss) {

		O2GRequestFactory factory = mSession.getRequestFactory();

		if (factory == null)
			return;

		O2GValueMap valuemap = factory.createValueMap();

		valuemap.setString(O2GRequestParamsEnum.COMMAND,
				Constants.Commands.CreateOrder);
		valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE,
				Constants.Orders.Stop);
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
				trade.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, trade.getOfferID());
		valuemap.setString(O2GRequestParamsEnum.TRADE_ID, trade.getTradeID());
		if (trade.getBuySell().equals("B"))
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, "S");
		else
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, "B");
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, trade.getAmount());
		valuemap.setDouble(O2GRequestParamsEnum.RATE, stopLoss);
		valuemap.setString(O2GRequestParamsEnum.CUSTOM_ID, "StopOrder");
		O2GRequest request = factory.createOrderRequest(valuemap);
		mSession.sendRequest(request);

	}

	/**
	 * Changes the current stop loss on an existing trade.
	 * 
	 * @param trade
	 * @param newStopLoss
	 */
	public void editStop(O2GTradeRow trade, double stopLoss, String orderID) {

		O2GRequestFactory factory = mSession.getRequestFactory();

		if (factory == null)
			return;

		O2GValueMap valuemap = factory.createValueMap();

		valuemap.setString(O2GRequestParamsEnum.COMMAND,
				Constants.Commands.EditOrder);

		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
				trade.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.ORDER_ID, orderID);
		valuemap.setDouble(O2GRequestParamsEnum.RATE, stopLoss);

		O2GRequest request = factory.createOrderRequest(valuemap);
		mSession.sendRequest(request);

	}

	/**
	 * Changes the current limit on an existing trade. *
	 * 
	 * @param trade
	 * @param newLimit
	 */
	public void editLimit(O2GTradeRow trade, double limit, String orderID) {

		O2GRequestFactory factory = mSession.getRequestFactory();

		if (factory == null)
			return;

		O2GValueMap valuemap = factory.createValueMap();

		valuemap.setString(O2GRequestParamsEnum.COMMAND,
				Constants.Commands.EditOrder);

		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID,
				trade.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.ORDER_ID, orderID);
		valuemap.setDouble(O2GRequestParamsEnum.RATE, limit);

		O2GRequest request = factory.createOrderRequest(valuemap);
		mSession.sendRequest(request);

	}

}
