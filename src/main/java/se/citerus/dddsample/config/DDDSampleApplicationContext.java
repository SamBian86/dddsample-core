package se.citerus.dddsample.config;

import com.pathfinder.api.GraphTraversalService;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.PlatformTransactionManager;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.application.BookingService;
import se.citerus.dddsample.application.CargoInspectionService;
import se.citerus.dddsample.application.HandlingEventService;
import se.citerus.dddsample.application.impl.BookingServiceImpl;
import se.citerus.dddsample.application.impl.CargoInspectionServiceImpl;
import se.citerus.dddsample.application.impl.HandlingEventServiceImpl;
import se.citerus.dddsample.application.util.SampleDataGenerator;
import se.citerus.dddsample.domain.model.cargo.CargoRepository;
import se.citerus.dddsample.domain.model.handling.HandlingEventFactory;
import se.citerus.dddsample.domain.model.handling.HandlingEventRepository;
import se.citerus.dddsample.domain.model.location.LocationRepository;
import se.citerus.dddsample.domain.model.voyage.VoyageRepository;
import se.citerus.dddsample.domain.service.RoutingService;
import se.citerus.dddsample.infrastructure.routing.ExternalRoutingService;

@Configuration
@ImportResource({ "classpath:context-interfaces.xml", "classpath:context-infrastructure-messaging.xml",
		"classpath:context-infrastructure-persistence.xml" })
public class DDDSampleApplicationContext {
	// 货物仓库
	@Autowired
	CargoRepository cargoRepository;

	// 位置仓库
	@Autowired
	LocationRepository locationRepository;
	
	// 航程仓库
	@Autowired
	VoyageRepository voyageRepository;
	
	// 网络遍历服务  通过PathfinderApplicationContext.class中的Bean注入
	@Autowired
	GraphTraversalService graphTraversalService;

	// 路由服务
	@Autowired
	RoutingService routingService;

	// 事件处理工厂
	@Autowired
	HandlingEventFactory handlingEventFactory;

	// 事件处理仓库
	@Autowired
	HandlingEventRepository handlingEventRepository;

	// 应用事件
	@Autowired
	ApplicationEvents applicationEvents;

	// 平台事务管理
	@Autowired
	PlatformTransactionManager platformTransactionManager;
	
	// 会话工厂
	@Autowired
	SessionFactory sessionFactory;

	// 预定服务 Bean
	@Bean
	public BookingService bookingService() {
		return new BookingServiceImpl(cargoRepository, locationRepository, routingService);
	}
	
	// 货物检查服务 Bean
	@Bean
	public CargoInspectionService cargoInspectionService() {
		return new CargoInspectionServiceImpl(applicationEvents, cargoRepository, handlingEventRepository);
	}
	
	// 事件处理服务 Bean
	@Bean
	public HandlingEventService handlingEventService() {
		return new HandlingEventServiceImpl(handlingEventRepository, applicationEvents, handlingEventFactory);
	}

	// 事件处理工厂 Bean
	@Bean
	public HandlingEventFactory handlingEventFactory() {
		return new HandlingEventFactory(cargoRepository, voyageRepository, locationRepository);
	}

	// 路由服务 Bean
	@Bean
	public RoutingService routingService() {
		ExternalRoutingService routingService = new ExternalRoutingService();
		routingService.setGraphTraversalService(graphTraversalService);
		routingService.setLocationRepository(locationRepository);
		routingService.setVoyageRepository(voyageRepository);
		return routingService;
	}

	// 样本数据生成 Bean
	@Bean 
	public SampleDataGenerator sampleDataGenerator() {
		return new SampleDataGenerator(platformTransactionManager, sessionFactory, cargoRepository, voyageRepository,
				locationRepository, handlingEventRepository);
	}
}
