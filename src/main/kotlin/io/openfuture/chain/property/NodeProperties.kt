package io.openfuture.chain.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Component
@Validated
@ConfigurationProperties(value = "node")
data class NodeProperties(

        /** Node Server Port */
        @field:NotNull
        var port: Int? = null,

        /** Root Nodes List */
        @field:NotEmpty
        @field:Size(min = 8, max = 8)
        var rootNodes: List<String> = emptyList(),

        /** Node Communication Protocol Version */
        @field:NotNull
        var version: String? = null

)