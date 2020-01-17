package ca.jrvs.apps.trading.controller;

import ca.jrvs.apps.trading.model.TraderAccountView;
import ca.jrvs.apps.trading.model.domain.Account;
import ca.jrvs.apps.trading.model.domain.Trader;
import ca.jrvs.apps.trading.service.TraderAccountService;
import ca.jrvs.apps.trading.utils.ResponseExceptionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/trader")
@Api(value = "Trader", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TraderAccountController {

  private TraderAccountService traderAccountService;

  @Autowired
  public TraderAccountController(TraderAccountService traderAccountService) {
    this.traderAccountService = traderAccountService;
  }

  /**
   * Creates a new account using request parameters as data. Sample Usage: POST
   * /trader/create?firstname=First&lastname=Last&email=flast@email.org&birthdate=1994-05-11
   *
   * @param firstName the trader's first name
   * @param lastName  the trader's last name
   * @param email     the trader's email address
   * @param country   the trader's home country
   * @param dob       the trader's date of birth
   * @return The trader's profile and account info
   */
  @ApiOperation(value = "Create a new Trader and account for them",
      notes =
          "The email used to create the account must not already be in use. Trader ID and Account ID"
              + " are auto-generated and should be identical. Each Trader may have only one account.")
  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public TraderAccountView createNewTraderAccount(@RequestParam("firstname") String firstName,
      @RequestParam("lastname") String lastName, @RequestParam("email") String email,
      @RequestParam("country") String country,
      @RequestParam("birthdate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dob) {
    Trader newTrader = new Trader();
    newTrader.setFirstName(firstName);
    newTrader.setLastName(lastName);
    newTrader.setCountry(country);
    newTrader.setEmail(email);
    newTrader.setDob(Date.valueOf(dob));
    try {
      return traderAccountService.createNewTraderAccount(newTrader);
    } catch (Exception ex) {
      throw ResponseExceptionUtils.getResponseStatusException(ex);
    }
  }

  /**
   * Create a new Trader and Account using a pre-built Trader object sent by the client as JSON
   * Sample Usage: POST /trader/create/prebuilt BODY: {'id':0,'firstName'='Fname', ...}
   *
   * @param newTrader The Trader sent by the client, de-serialized as a Trader
   * @return a TraderAccountView representing the new account that's been created
   */
  @ApiOperation(value = "Create a new Trader and account via JSON document/DTO",
      notes =
          "The Trader and Account will only be created if the given email is not already in use."
              + "The Trader's ID and their Account ID are auto-generated by the DB and should be equal")
  @PostMapping("/create/prebuilt")
  @ResponseBody
  @ResponseStatus(HttpStatus.CREATED)
  public TraderAccountView createNewTraderAccount(@RequestBody Trader newTrader) {
    try {
      return traderAccountService.createNewTraderAccount(newTrader);
    } catch (Exception ex) {
      throw ResponseExceptionUtils.getResponseStatusException(ex);
    }
  }

  /**
   * Deletes the account of the specified trader. May only be used if they have no finds and no
   * outstanding orders. Sample Usage: DELETE /trader/delete/14576
   *
   * @param traderId The ID of the trader to delete.
   */
  @ApiOperation(value = "Delete a trader and their corresponding account.",
      notes =
          "This assumes that the trader and their account have the same ID. The delete may only go "
              + "through if the trader has exactly 0 money in their account and they have no pending orders"
              + "tied to their account.")
  @DeleteMapping("/delete/{traderId}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteTraderAccount(@PathVariable int traderId) {
    try {
      traderAccountService.deleteTraderById(traderId);
    } catch (Exception ex) {
      throw ResponseExceptionUtils.getResponseStatusException(ex);
    }
  }

  /**
   * Deposit amount into the specified trader's account. Sample usage: PUT
   * /trader/deposit/15987?amount=1745.23
   *
   * @param traderId The ID of the trader making the deposit
   * @param amount   The amount of money to add to their account
   * @return The user's updated account
   */
  @PutMapping("/deposit/{traderId}")
  @ResponseBody
  public Account depositFunds(@PathVariable int traderId, @RequestParam("amount") double amount) {
    new DecimalFormat().setRoundingMode(RoundingMode.DOWN);
    amount =
        Math.floor(amount * 100) / 100; // Try truncating to two decimals, fractional cents is bad
    try {
      return traderAccountService.deposit(traderId, amount);
    } catch (Exception ex) {
      throw ResponseExceptionUtils.getResponseStatusException(ex);
    }
  }

  /**
   * Withdraws amount from the specified Trader's account. Sample usage: PUT
   * /trader/withdraw/15987?amount=101.01
   *
   * @param traderId The ID of the trader account to withdraw from
   * @param amount   The amount of money to withdraw
   * @return The updated account info
   */
  @PutMapping("/withdraw/{traderId}")
  @ResponseBody
  public Account withdrawFunds(@PathVariable int traderId, @RequestParam("amount") double amount) {
    new DecimalFormat().setRoundingMode(RoundingMode.DOWN);
    amount =
        Math.floor(amount * 100) / 100; // Try truncating to two decimals, fractional cents is bad
    try {
      return traderAccountService.withdraw(traderId, amount);
    } catch (Exception ex) {
      throw ResponseExceptionUtils.getResponseStatusException(ex);
    }
  }
}
