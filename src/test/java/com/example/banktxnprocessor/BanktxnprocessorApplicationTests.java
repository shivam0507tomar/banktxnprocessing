package com.example.banktxnprocessor;

import com.example.banktxnprocessor.exception.AccountAlreadyExistsException;
import com.example.banktxnprocessor.exception.AccountNotFoundException;
import com.example.banktxnprocessor.exception.InsufficientFundsException;
import com.example.banktxnprocessor.exception.InvalidAmountException;
import com.example.banktxnprocessor.repository.AccountRepository;
import com.example.banktxnprocessor.service.BankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BanktxnprocessorApplicationTests {
	private BankingService service;

	@BeforeEach
	void setUp() {
		service = new BankingService(new AccountRepository());
		service.createAccount("A1");
		service.createAccount("A2");
	}
	private BigDecimal money(String value) {
		return new BigDecimal(value);
	}
	@Test
	void depositIncreasesBalanceAndRecordsTransaction() {
		service.deposit("A1", money("100.00"));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("100.00")));
		assertEquals(1, service.getHistory("A1").size());
	}
	@Test
	void withdrawalReducesBalance() {
		service.deposit("A1", money("100.00"));

		service.withdraw("A1", money("30.00"));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("70.00")));
	}

	@Test
	void overdraftIsRejectedWithoutChangingBalanceOrLedger() {
		service.deposit("A1", money("50.00"));
		int entriesBefore = service.getHistory("A1").size();

		assertThrows(InsufficientFundsException.class,
				() -> service.withdraw("A1", money("60.00")));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("50.00")));
		assertEquals(entriesBefore, service.getHistory("A1").size());
	}

	@Test
	void zeroDepositIsRejected() {
		assertThrows(InvalidAmountException.class,
				() -> service.deposit("A1", money("0")));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("0")));
		assertTrue(service.getHistory("A1").isEmpty());
	}

	@Test
	void transferUpdatesBothAccountsAndCreatesLinkedEntries() {
		service.deposit("A1", money("100.00"));

		service.transfer("A1", "A2", money("40.00"));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("60.00")));
		assertEquals(0,
				service.getBalance("A2")
						.compareTo(money("40.00")));

		var debit = service.getHistory("A1").get(1);
		var credit = service.getHistory("A2").get(0);

		assertEquals(debit.transferId(), credit.transferId());
		assertEquals(debit.timestamp(), credit.timestamp());
	}

	@Test
	void transferWithInsufficientFundsChangesNeitherAccount() {
		service.deposit("A1", money("20.00"));

		assertThrows(InsufficientFundsException.class,
				() -> service.transfer("A1", "A2", money("30.00")));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("20.00")));
		assertEquals(0,
				service.getBalance("A2")
						.compareTo(money("0")));
		assertEquals(1, service.getHistory("A1").size());
		assertTrue(service.getHistory("A2").isEmpty());
	}

	@Test
	void transferToSameAccountIsRejected() {
		service.deposit("A1", money("100.00"));

		assertThrows(IllegalArgumentException.class,
				() -> service.transfer("A1", "A1", money("10.00")));

		assertEquals(0,
				service.getBalance("A1")
						.compareTo(money("100.00")));
		assertEquals(1, service.getHistory("A1").size());
	}

	@Test
	void duplicateAccountIdIsRejected() {
		assertThrows(
				AccountAlreadyExistsException.class,
				() -> service.createAccount("A1"));
	}
	@Test
	void missingAccountIsRejected() {
		assertThrows(
				AccountNotFoundException.class,
				() -> service.getBalance("UNKNOWN"));
	}

	@Test
	void negativeDepositIsRejected() {
		assertThrows(
				InvalidAmountException.class,
				() -> service.deposit("A1", money("-10.00")));

		assertEquals(0,
				service.getBalance("A1").compareTo(money("0.00")));

		assertTrue(service.getHistory("A1").isEmpty());
	}

	@Test
	void missingDestinationDoesNotChangeSourceAccount() {
		service.deposit("A1", money("100.00"));

		assertThrows(
				AccountNotFoundException.class,
				() -> service.transfer(
						"A1", "UNKNOWN", money("25.00")));

		assertEquals(0,
				service.getBalance("A1").compareTo(money("100.00")));

		assertEquals(1, service.getHistory("A1").size());
	}

	@Test
	void amountWithMoreThanTwoDecimalPlacesIsRejected() {
		assertThrows(
				InvalidAmountException.class,
				() -> service.deposit("A1", money("10.999")));

		assertEquals(0,
				service.getBalance("A1").compareTo(money("0.00")));

		assertTrue(service.getHistory("A1").isEmpty());
	}

}
