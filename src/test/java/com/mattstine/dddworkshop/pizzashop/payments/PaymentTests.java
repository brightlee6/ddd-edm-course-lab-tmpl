package com.mattstine.dddworkshop.pizzashop.payments;

import com.mattstine.dddworkshop.pizzashop.infrastructure.Amount;
import com.mattstine.dddworkshop.pizzashop.infrastructure.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.Topic;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Matt Stine
 */
public class PaymentTests {

	private EventLog eventLog;
	private PaymentProcessor paymentProcessor;
	private Payment payment;

	@Before
	public void setUp() {
		paymentProcessor = mock(PaymentProcessor.class);
		eventLog = mock(EventLog.class);
		PaymentRef ref = new PaymentRef();
		payment = Payment.builder()
				.amount(Amount.of(15, 0))
				.paymentProcessor(paymentProcessor)
				.eventLog(eventLog)
				.ref(ref)
				.build();
	}

	@Test
	public void new_payment_is_new() {
		assertThat(payment.isNew());
	}

	@Test
	public void should_request_payment_from_processor() {
		payment.request();
		assertThat(payment.isRequested()).isTrue();
		verify(paymentProcessor).request(payment);
	}

	@Test
	public void payment_request_should_fire_event() {
		payment.request();
		verify(eventLog).publish(eq(new Topic("payments")), isA(PaymentRequestedEvent.class));
	}

	@Test
	public void should_reflect_successful_payment() {
		payment.request();
		payment.markSuccessful();
		assertThat(payment.isSuccessful()).isTrue();
	}

	@Test
	public void payment_success_should_fire_event() {
		payment.request();
		verify(eventLog).publish(eq(new Topic("payments")), isA(PaymentRequestedEvent.class));
		payment.markSuccessful();
		verify(eventLog).publish(eq(new Topic("payments")), isA(PaymentSuccessfulEvent.class));
	}

	@Test
	public void should_reflect_failed_payment() {
		payment.request();
		payment.markFailed();
		assertThat(payment.isFailed()).isTrue();
	}

	@Test
	public void payment_failure_should_fire_event() {
		payment.request();
		verify(eventLog).publish(eq(new Topic("payments")), isA(PaymentRequestedEvent.class));
		payment.markFailed();
		verify(eventLog).publish(eq(new Topic("payments")), isA(PaymentFailedEvent.class));
	}

	@Test
	public void can_only_request_from_new() {
		payment.request();
		payment.markSuccessful();
		assertThatIllegalStateException().isThrownBy(payment::request);
	}

	@Test
	public void can_only_mark_requested_payment_as_successful() {
		assertThatIllegalStateException().isThrownBy(payment::markSuccessful);
	}

	@Test
	public void can_only_mark_requested_payment_as_failed() {
		assertThatIllegalStateException().isThrownBy(payment::markFailed);
	}
}
