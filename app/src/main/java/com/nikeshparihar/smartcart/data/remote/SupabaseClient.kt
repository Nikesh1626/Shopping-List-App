package com.nikeshparihar.smartcart.data.remote

import com.nikeshparihar.smartcart.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseApi {
    // Secrets are loaded from local.properties via BuildConfig — never hardcode here!
    private val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildConfig.SUPABASE_KEY

    val GOOGLE_WEB_CLIENT_ID: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Realtime)
    }
}
