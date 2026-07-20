package com.nikeshparihar.smartcart.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Subscribes to Postgres changes via Supabase Realtime and invokes [onChange] for each event.
 * Kept out of [com.nikeshparihar.smartcart.viewmodel.ShoppingListViewModel] so the ViewModel file
 * does not depend on Realtime inline extensions (helps IDE / analysis).
 */
object SupabaseRealtimeSync {

    fun startPostgresChangeListener(
        client: SupabaseClient,
        scope: CoroutineScope,
        onChange: () -> Unit,
    ) {
        val realtimeChannel = client.channel("db-changes")
        val changes: Flow<PostgresAction> =
            realtimeChannel.postgresChangeFlow<PostgresAction>("public")

        changes.onEach { onChange() }.launchIn(scope)

        scope.launch {
            realtimeChannel.subscribe()
        }
    }
}
