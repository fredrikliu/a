/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.examples.alibaba.cloud.cloud;

import com.alibaba.nacos.common.utils.JacksonUtils;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SpringBootApplication
public class ProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProviderApplication.class, args);
	}

	@Component
	public class TimeCostBeanPostProcessor implements BeanPostProcessor {

		Map<String, Long> costMap = Maps.newConcurrentMap();

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			costMap.put(beanName, System.currentTimeMillis());
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			Long start = costMap.get(beanName);
			long cost = System.currentTimeMillis() - start;
			costMap.put(beanName, cost);
			System.out.println("class: " + bean.getClass().getName()
					+ "\tbean: "+ beanName
					+ "\ttime: "+ cost);

			return bean;
		}
	}


	@RestController
	@RefreshScope
	public static class EchoController {

		private Registration registration;

		@Value("${server.port}")
		private int port;

		@Value("${custom.config:none}")
		private String customConfig;

		@GetMapping("/custom/config")
		public ResponseEntity<String> getCustomConfig() {
			return new ResponseEntity<>(String.valueOf(customConfig), HttpStatus.OK);
		}

		public EchoController(Registration registration) {
			this.registration = registration;
		}

		@GetMapping("/echo/{string}")
		public String echo(@PathVariable String string) {
			String sb = "Hello, I'm provider[" + port + "], receive msg : "
					+ string
					+ "my metadata : "
					+ JacksonUtils.toJson(registration.getMetadata());
			return sb;
		}

	}

}
