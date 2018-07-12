package com.mattstine.dddworkshop.pizzashop.ordering;

import com.mattstine.dddworkshop.pizzashop.infrastructure.EventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.InProcessEventLog;
import com.mattstine.dddworkshop.pizzashop.infrastructure.Topic;
import com.mattstine.dddworkshop.pizzashop.payments.PaymentRef;
import com.mattstine.dddworkshop.pizzashop.payments.PaymentService;
import com.mattstine.dddworkshop.pizzashop.payments.PaymentSuccessfulEvent;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Matt Stine
 */
public class OrderServiceIntegrationTests {

	private EventLog eventLog;
	private OrderRepository repository;

	@Before
	public void setUp() {
		eventLog = new InProcessEventLog();
		repository = mock(OrderRepository.class);
		new OrderService(eventLog, repository, mock(PaymentService.class));
	}

	@Test
	public void on_successful_payment_mark_paid() {
		OrderRef orderRef = new OrderRef();
		Order order = Order.builder()
				.type(Order.Type.PICKUP)
				.eventLog(eventLog)
				.ref(orderRef)
				.build();
		PaymentRef paymentRef = new PaymentRef();
		order.setPaymentRef(paymentRef);

		when(repository.findByPaymentRef(eq(paymentRef))).thenReturn(order);

		eventLog.publish(new Topic("payments"), new PaymentSuccessfulEvent(paymentRef));

		assertThat(order.isPaid()).isTrue();
	}
}
