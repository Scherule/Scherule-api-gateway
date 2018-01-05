package com.scherule.api.gateway

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.netflix.zuul.filters.RouteLocator
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component


@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
@Configuration
class ApiGatewayApplication {

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun commandLineRunner(routeLocator: RouteLocator): CommandLineRunner =
            CommandLineRunner { args ->
                routeLocator.routes
                        .forEach { route ->
                            LOG.info("${route.id} (${route.location}) ${route.fullPath}")
                        }
            }

    @Component
    class CustomPathZuulFilter : ZuulFilter() {

        companion object {
            val REQUEST_URI_KEY = "requestURI"
        }

        override fun filterType(): String {
            return "pre"
        }

        override fun filterOrder(): Int {
            return PreDecorationFilter.FILTER_ORDER + 1
        }

        override fun shouldFilter(): Boolean {
            return true
        }

        override fun run(): Any? {
            val context = RequestContext.getCurrentContext()
            val request = context.getRequest()
            if(request.requestURI.startsWith("/api/")) {
                val originalRequestPath = context.get(REQUEST_URI_KEY)
                val modifiedRequestPath = "/api/" + originalRequestPath
                context.put(REQUEST_URI_KEY, modifiedRequestPath)
            }
            return null
        }

    }

}

fun main(args: Array<String>) {
    SpringApplication.run(ApiGatewayApplication::class.java, *args)
}
