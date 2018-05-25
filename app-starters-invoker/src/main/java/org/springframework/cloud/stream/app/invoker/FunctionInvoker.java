//package org.springframework.cloud.stream.app.invoker;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.stream.annotation.EnableBinding;
//import org.springframework.cloud.stream.messaging.Processor;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.messaging.Message;
//
//import java.util.function.Function;
//
///**
// * @author Soby Chacko
// */
//@EnableBinding(Processor.class)
//public class FunctionInvoker {
//
//	@Autowired(required = false)
//	Function<Message<?>,?> functionToInvoke;
//
//	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
//	public Object invokeProcessor(Message<?> message) {
//		return functionToInvoke != null ? functionToInvoke.apply(message) : null;
//	}
//
//}
