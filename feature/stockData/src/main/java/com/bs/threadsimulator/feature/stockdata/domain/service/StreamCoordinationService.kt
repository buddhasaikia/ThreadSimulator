package com.bs.threadsimulator.feature.stockdata.domain.service

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ChannelConfig
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.feature.stockdata.domain.model.CompanyData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain service for coordinating multi-threaded data collection via channels.
 *
 * [StreamCoordinationService] manages the lifecycle of channel-based communication
 * between multiple concurrent data producers (stock fetch operations) and a single
 * consumer (UI update handler). It handles:
 * - Channel creation and configuration
 * - Queue depth tracking via ThreadMonitor
 * - Undelivered element counting and logging
 *
 * This service abstracts infrastructure concerns (channels, threading) from the ViewModel,
 * allowing the ViewModel to focus on state management and business logic.
 */
@Singleton
class StreamCoordinationService
    @Inject
    constructor(
        private val appDispatchers: AppDispatchers,
        private val channelConfig: ChannelConfig,
        private val threadMonitor: ThreadMonitor,
    ) {
        /**
         * Provides access to ThreadMonitor for observing thread execution metrics.
         * Exposed as a read-only property to maintain clean boundaries.
         */
        val monitor: ThreadMonitor
            get() = threadMonitor

        /**
         * Provides access to AppDispatchers for launching coroutines on appropriate contexts.
         * Exposed as a read-only property to maintain clean boundaries.
         */
        val dispatchers: AppDispatchers
            get() = appDispatchers

        /**
         * Creates a new channel for coordinating data flow between producers and consumer.
         *
         * Channel is configured with:
         * - Capacity: from ChannelConfig
         * - Buffer overflow strategy: DROP_OLDEST (prevents blocking on write)
         * - Undelivered element handler: logs and tracks dropped elements
         *
         * @return A new Channel configured for multi-threaded data coordination
         */
        fun createCoordinationChannel(): Channel<CompanyData> =
            Channel(
                capacity = channelConfig.capacity,
                onBufferOverflow = channelConfig.onBufferOverflow,
                onUndeliveredElement = {
                    Timber.i("Undelivered: %s", it)
                },
            )

        /**
         * Creates a receiver that listens on a channel and processes updates.
         *
         * This flow handles:
         * - Receiving data from the channel
         * - Tracking queue depth via ThreadMonitor
         * - Error handling and logging
         *
         * @param channel The ReceiveChannel to listen on
         * @param onUpdate Callback invoked for each received CompanyData update
         * @param onError Callback invoked when processing fails
         * @return A Flow that processes updates and completes when channel closes
         */
        fun listenToChannel(
            channel: ReceiveChannel<CompanyData>,
            onUpdate: (CompanyData) -> Unit,
            onError: (String, Exception?) -> Unit,
        ): Flow<Unit> =
            flow {
                try {
                    for (companyData in channel) {
                        threadMonitor.decrementQueueDepth()
                        try {
                            onUpdate(companyData)
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to process update: %s", e.message)
                            onError("Failed to process update", e)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Channel processing failed: %s", e.message)
                    onError("Channel processing error", e)
                }
                emit(Unit)
            }

        /**
         * Sends data to the coordination channel with queue depth tracking.
         *
         * Increments queue depth before sending, and decrements on failure to maintain
         * accurate metrics. This ensures the ThreadMonitor accurately reflects pending items.
         *
         * @param channel The Channel to send to
         * @param data The CompanyData to send
         * @throws Exception if channel send fails or is closed
         */
        suspend fun sendWithTracking(
            channel: Channel<CompanyData>,
            data: CompanyData,
        ) {
            threadMonitor.incrementQueueDepth()
            try {
                channel.send(data)
            } catch (e: Exception) {
                threadMonitor.decrementQueueDepth()
                throw e
            }
        }
    }
